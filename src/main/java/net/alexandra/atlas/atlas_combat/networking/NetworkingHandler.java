package net.alexandra.atlas.atlas_combat.networking;

import io.netty.buffer.Unpooled;
import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class NetworkingHandler {

	public ResourceLocation modDetectionNetworkChannel = new ResourceLocation("atlas-combat","networking");

	public NetworkingHandler() {
		ServerPlayConnectionEvents.JOIN.register(modDetectionNetworkChannel,(handler, sender, server) -> {
			FriendlyByteBuf packetBuf = new FriendlyByteBuf(Unpooled.buffer());
			packetBuf.writeBoolean(true);
			if(!ServerPlayNetworking.canSend(handler.player, modDetectionNetworkChannel)) {
				handler.player.connection.getConnection().disconnect(Component.literal("Atlas Combat needs to be installed on the client to join this server!"));
			}
			ServerPlayNetworking.send(handler.player, modDetectionNetworkChannel,packetBuf);
		});
	}
}
