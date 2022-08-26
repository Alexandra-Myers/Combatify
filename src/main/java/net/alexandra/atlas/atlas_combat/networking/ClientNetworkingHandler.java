package net.alexandra.atlas.atlas_combat.networking;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.config.ConfigHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import java.util.Collections;

public class ClientNetworkingHandler {

	public ResourceLocation modDetectionNetworkChannel = new ResourceLocation("atlas-combat","networking");

	public int ticksTowait = ConfigHelper.maxWaitForPacketResponse;

	public static boolean receivedAnswer = false;
	public static int ticksElapsed = 0;
	public ClientNetworkingHandler() {

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> receivedAnswer = false);

		ClientPlayNetworking.registerGlobalReceiver(modDetectionNetworkChannel,(client, handler, buf, responseSender) -> {
			if(buf.getBoolean(0)) {
				receivedAnswer = true;
			}
		});

		ClientTickEvents.END.register(client -> {
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
