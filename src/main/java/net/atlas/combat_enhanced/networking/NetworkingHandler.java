package net.atlas.combat_enhanced.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;

import static net.atlas.combat_enhanced.CombatEnhanced.modDetectionNetworkChannel;

public class NetworkingHandler {

	public NetworkingHandler() {

		ServerPlayNetworking.registerGlobalReceiver(modDetectionNetworkChannel,(server, player, handler, buf, responseSender) -> {
		});
		ServerPlayConnectionEvents.JOIN.register(modDetectionNetworkChannel,(handler, sender, server) -> {
			if(!ServerPlayNetworking.canSend(handler.player, modDetectionNetworkChannel)) {
				handler.player.connection.disconnect(Component.literal("Combat Enhanced needs to be installed on the client to join this server!"));
			}
		});
	}
}
