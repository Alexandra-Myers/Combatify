package net.alexandra.atlas.atlas_combat.networking;

import net.alexandra.atlas.atlas_combat.extensions.ItemExtensions;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import java.util.List;

import static net.alexandra.atlas.atlas_combat.AtlasCombat.*;

public class ClientNetworkingHandler {
	public ClientNetworkingHandler() {
		ClientPlayNetworking.registerGlobalReceiver(modDetectionNetworkChannel,(client, handler, buf, responseSender) -> {
		});
		ClientPlayConnectionEvents.JOIN.register(modDetectionNetworkChannel,(handler, sender, client) -> {
			if(!ClientPlayNetworking.canSend(modDetectionNetworkChannel)) {
				handler.getConnection().disconnect(Component.literal("Atlas Combat needs to be installed on the server to join with this client"));
			}
			List<Item> items = BuiltInRegistries.ITEM.stream().toList();

			for(Item item : items) {
				((ItemExtensions) item).modifyAttributeModifiers();
			}
		});
	}
}
