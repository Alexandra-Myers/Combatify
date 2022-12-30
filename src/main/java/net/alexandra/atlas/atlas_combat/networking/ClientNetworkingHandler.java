package net.alexandra.atlas.atlas_combat.networking;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ClientNetworkingHandler {

	public ResourceLocation modDetectionNetworkChannel = new ResourceLocation("atlas-combat","networking");

	public int ticksTowait = AtlasCombat.CONFIG.maxWaitForPacketResponse();

	public static boolean receivedAnswer = false;
	public static int ticksElapsed = 0;
	public ClientNetworkingHandler() {

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> receivedAnswer = false);

		ClientPlayNetworking.registerGlobalReceiver(modDetectionNetworkChannel,(client, handler, buf, responseSender) -> {
			if(buf.getBoolean(0)) {
				receivedAnswer = true;
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if(Minecraft.getInstance().getConnection() != null && Minecraft.getInstance().player != null && !receivedAnswer) {
				ticksElapsed++;

				if(ticksElapsed >= ticksTowait && !receivedAnswer) {
					Minecraft.getInstance().player.connection.getConnection().disconnect(Component.literal("Mod not present on server!"));
					ticksElapsed = 0;
					receivedAnswer = false;
				}
			}
		});
	}
}
