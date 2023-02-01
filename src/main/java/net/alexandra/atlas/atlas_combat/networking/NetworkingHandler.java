package net.alexandra.atlas.atlas_combat.networking;

import io.netty.buffer.Unpooled;
import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.extensions.IServerPlayer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class NetworkingHandler {

	public ResourceLocation modDetectionNetworkChannel = new ResourceLocation("atlas-combat","networking");

	public int ticksTowait = AtlasCombat.CONFIG.maxWaitForPacketResponse();
	public static int ticksElapsed = 0;

	public NetworkingHandler() {

		ServerPlayNetworking.registerGlobalReceiver(modDetectionNetworkChannel,(server, player, handler, buf, responseSender) -> {
			if(buf.getBoolean(0)) {
				((IServerPlayer)player).setReceivedAnswer(true);
			}
		});
		ServerTickEvents.END_SERVER_TICK.register(modDetectionNetworkChannel, server -> {
			ticksElapsed++;
			if(ticksElapsed > ticksTowait) {
				server.getPlayerList().getPlayers().forEach(serverPlayer -> {
					boolean bl = ((IServerPlayer) serverPlayer).getReceivedAnswer();
					if (!bl) {
						serverPlayer.connection.getConnection().disconnect(Component.literal("Atlas Combat needs to be installed on the client to join this server!").append(Component.literal("https://github.com/Alexandra-Myers/Atlas-Combat")));
					}
				});
				ticksTowait = 0;
			}
		});

		ServerPlayConnectionEvents.JOIN.register(modDetectionNetworkChannel,(handler, sender, server) -> {
			FriendlyByteBuf packetBuf = new FriendlyByteBuf(Unpooled.buffer());
			packetBuf.writeBoolean(true);
			ServerPlayNetworking.send(handler.player, modDetectionNetworkChannel,packetBuf);
		});
	}
}
