package net.atlas.combatify.networking;

import commonnetwork.networking.data.PacketContext;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.extensions.PlayerExtensions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import static net.atlas.combatify.Combatify.id;

public class ClientboundResponsePacket {
	public static final ResourceLocation CHANNEL = id("detection");

	public ClientboundResponsePacket() {
	}

	public static ClientboundResponsePacket decode(FriendlyByteBuf buf) {
		return new ClientboundResponsePacket();
	}

	public void encode(FriendlyByteBuf buf) {
	}

	public static void handle(PacketContext<ClientboundResponsePacket> ctx) {
		CombatifyClient.shouldDisconnect = false;
	}
}
