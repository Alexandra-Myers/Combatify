package net.atlas.combatify.networking;

import commonnetwork.networking.data.PacketContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import static net.atlas.combatify.Combatify.id;
import static net.atlas.combatify.config.ConfigSynchronizer.*;

public class C2SConfigPacket {
	public static final ResourceLocation CHANNEL = id("item_config");
	public static FriendlyByteBuf buf;

	public C2SConfigPacket() {
	}

	public static C2SConfigPacket decode(FriendlyByteBuf buf) {
		C2SConfigPacket.buf = buf;
		return new C2SConfigPacket();
	}

	public void encode(FriendlyByteBuf buf) {
		write(buf, 1);
	}

	public static void handle(PacketContext<C2SConfigPacket> ctx) {
		applyServer(ctx.sender(), buf);
	}
}
