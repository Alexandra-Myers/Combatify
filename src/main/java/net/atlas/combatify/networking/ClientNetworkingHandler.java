package net.atlas.combatify.networking;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.config.ItemConfig;
import net.atlas.combatify.extensions.ItemExtensions;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import java.util.List;

import static net.atlas.combatify.Combatify.*;

@SuppressWarnings("unused")
public class ClientNetworkingHandler {
	private ClientNetworkingHandler() {
	}
	public static void init() {
		ClientPlayNetworking.registerGlobalReceiver(NetworkingHandler.ItemConfigPacket.TYPE, (packet, player, responseSender) -> {
			LOGGER.info("Loading config details from buffer.");

			List<Item> items = BuiltInRegistries.ITEM.stream().toList();

			for(Item item : items)
				((ItemExtensions) item).modifyAttributeModifiers();
			for (Item item : ITEMS.configuredItems.keySet()) {
				ConfigurableItemData configurableItemData = ITEMS.configuredItems.get(item);
				if (configurableItemData.stackSize != null)
					((ItemExtensions) item).setStackSize(configurableItemData.stackSize);
			}
		});
		ClientPlayConnectionEvents.JOIN.register(modDetectionNetworkChannel,(handler, sender, client) -> {
			if(!ClientPlayNetworking.canSend(modDetectionNetworkChannel))
				handler.getConnection().disconnect(Component.literal("Combatify needs to be installed on the server to join with this client"));
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
	}
}
