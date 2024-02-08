package net.atlas.combatify.networking;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.AtlasConfig;
import net.atlas.combatify.config.CombatifyBetaConfig;
import net.atlas.combatify.config.ConfigurableItemData;
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
		ClientPlayNetworking.registerGlobalReceiver(NetworkingHandler.AtlasConfigPacket.TYPE, (packet, context) -> packet.config().handleExtraSync(packet, context.player(), context.responseSender()));
		ClientPlayNetworking.registerGlobalReceiver(NetworkingHandler.RemainingUseSyncPacket.TYPE, (packet, player) -> {
			Entity entity = Minecraft.getInstance().level.getEntity(packet.id());
			if (entity instanceof LivingEntityExtensions livingEntity)
				livingEntity.setUseItemRemaining(packet.ticks());
		});
		ClientPlayConnectionEvents.JOIN.register(modDetectionNetworkChannel,(handler, sender, client) -> {
			if (!ClientPlayNetworking.canSend(NetworkingHandler.ServerboundMissPacket.TYPE)) {
				Combatify.CONFIG.reloadFromDefault();
			}
		});
		ClientLifecycleEvents.CLIENT_STARTED.register(modDetectionNetworkChannel, client -> {
			CONFIG = new CombatifyBetaConfig();

			List<Item> items = BuiltInRegistries.ITEM.stream().toList();

			for(Item item : items)
				((ItemExtensions) item).modifyAttributeModifiers();
			for(Item item : Combatify.CONFIG.configuredItems.keySet()) {
				ConfigurableItemData configurableItemData = Combatify.CONFIG.configuredItems.get(item);
				if (configurableItemData.stackSize != null)
					((ItemExtensions) item).setStackSize(configurableItemData.stackSize);
			}
			Combatify.LOGGER.info("Loaded items config.");
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> AtlasConfig.configs.forEach((resourceLocation, atlasConfig) -> atlasConfig.load()));
	}
}
