package net.alexandra.atlas.atlas_combat.networking;

import io.netty.buffer.Unpooled;
import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.extensions.ItemExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

public class NetworkingHandler {

	public ResourceLocation modDetectionNetworkChannel = new ResourceLocation("atlas-combat","networking");

	public int ticksTowait = AtlasCombat.helper.getInt(AtlasCombat.helper.generalJsonObject,"maxWaitForPacketResponse");

	public static boolean receivedAnswer = false;
	public static int ticksElapsed = 0;

	public NetworkingHandler() {
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> receivedAnswer = false);

		ServerPlayConnectionEvents.JOIN.register(modDetectionNetworkChannel,(handler, sender, server) -> {
			FriendlyByteBuf packetBuf = new FriendlyByteBuf(Unpooled.buffer());
			packetBuf.writeBoolean(true);
			ServerPlayNetworking.send(handler.player, modDetectionNetworkChannel,packetBuf);
		});

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
