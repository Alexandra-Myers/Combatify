package net.atlas.combatify.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;

import static net.atlas.combatify.Combatify.CONFIG;
import static net.atlas.combatify.Combatify.modDetectionNetworkChannel;

public class NetworkingHandler {

	public NetworkingHandler() {

		ServerPlayNetworking.registerGlobalReceiver(modDetectionNetworkChannel,(server, player, handler, buf, responseSender) -> {
		});
		ServerPlayConnectionEvents.JOIN.register(modDetectionNetworkChannel,(handler, sender, server) -> {
			boolean bl = CONFIG.configOnlyWeapons() || CONFIG.defender() || CONFIG.piercer();
			if(!ServerPlayNetworking.canSend(handler.player, modDetectionNetworkChannel) && bl) {
				handler.player.connection.disconnect(Component.literal("Combatify needs to be installed on the client to join this server!"));
			}
		});
	}
}
