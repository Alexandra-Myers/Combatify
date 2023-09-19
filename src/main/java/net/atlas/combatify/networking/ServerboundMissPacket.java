package net.atlas.combatify.networking;

import commonnetwork.networking.data.PacketContext;
import net.atlas.combatify.extensions.PlayerExtensions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import static net.atlas.combatify.Combatify.*;

public class ServerboundMissPacket {
	public static final ResourceLocation CHANNEL = id("attack_miss");

	public ServerboundMissPacket() {
	}

	public static ServerboundMissPacket decode(FriendlyByteBuf buf) {
		return new ServerboundMissPacket();
	}

	public void encode(FriendlyByteBuf buf) {
	}

	public static void handle(PacketContext<ServerboundMissPacket> ctx) {
		((PlayerExtensions)ctx.sender()).attackAir();
	}
}
