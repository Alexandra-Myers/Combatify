package net.atlas.combatify.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;

import static net.atlas.combatify.Combatify.modDetectionNetworkChannel;

public class NetworkingHandler {

	public NetworkingHandler() {

		ServerPlayNetworking.registerGlobalReceiver(modDetectionNetworkChannel,(server, player, handler, buf, responseSender) -> {
		});
		ServerPlayConnectionEvents.JOIN.register(modDetectionNetworkChannel,(handler, sender, server) -> {
			if(!ServerPlayNetworking.canSend(handler.player, modDetectionNetworkChannel)) {
				handler.player.connection.disconnect(Component.literal("Combatify needs to be installed on the client to join this server!"));
			}
		});
	}
}
