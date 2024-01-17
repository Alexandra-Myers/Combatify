package net.atlas.combatify.networking;

import io.wispforest.owo.config.ConfigSynchronizer;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.AtlasConfig;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.config.ItemConfig;
import net.atlas.combatify.extensions.ConfigSyncBase;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.extensions.LivingEntityExtensions;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;

import java.util.List;

import static net.atlas.combatify.Combatify.*;

@SuppressWarnings("unused")
public class ClientNetworkingHandler {
	private ClientNetworkingHandler() {
	}
	public static void init() {
		ClientPlayNetworking.registerGlobalReceiver(NetworkingHandler.AtlasConfigPacket.TYPE, (packet, player, responseSender) -> packet.config().handleExtraSync(packet, player, responseSender));
		ClientPlayNetworking.registerGlobalReceiver(NetworkingHandler.RemainingUseSyncPacket.TYPE, (packet, player, responseSender) -> {
			Entity entity = Minecraft.getInstance().level.getEntity(packet.id());
			if (entity instanceof LivingEntityExtensions livingEntity)
				livingEntity.setUseItemRemaining(packet.ticks());
		});
		ClientPlayConnectionEvents.JOIN.register(modDetectionNetworkChannel,(handler, sender, client) -> {
			if (!ClientPlayNetworking.canSend(modDetectionNetworkChannel)) {
				ITEMS.reloadFromDefault();
				ConfigSynchronizer configSynchronizer = new ConfigSynchronizer();
				((ConfigSyncBase) configSynchronizer).applyDefault();
			}
		});
		ClientLifecycleEvents.CLIENT_STARTED.register(modDetectionNetworkChannel, client -> {
			ITEMS = new ItemConfig();

			List<Item> items = BuiltInRegistries.ITEM.stream().toList();

			for(Item item : items)
				((ItemExtensions) item).modifyAttributeModifiers();
			for(Item item : ITEMS.configuredItems.keySet()) {
				ConfigurableItemData configurableItemData = ITEMS.configuredItems.get(item);
				if (configurableItemData.stackSize != null)
					((ItemExtensions) item).setStackSize(configurableItemData.stackSize);
			}
			Combatify.LOGGER.info("Loaded items config.");
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> AtlasConfig.configs.forEach((resourceLocation, atlasConfig) -> atlasConfig.load()));
	}
}
