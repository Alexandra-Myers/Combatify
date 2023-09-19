package net.atlas.combatify.networking;

import commonnetwork.networking.data.PacketContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import static net.atlas.combatify.Combatify.*;
import static net.atlas.combatify.config.ConfigSynchronizer.applyClient;
import static net.atlas.combatify.config.ConfigSynchronizer.write;

public class S2CConfigPacket {
	public static final ResourceLocation CHANNEL = id("stc_config");

	public S2CConfigPacket() {
	}

	public static S2CConfigPacket decode(FriendlyByteBuf buf) {
		applyClient(buf);
		return new S2CConfigPacket();
	}

	public void encode(FriendlyByteBuf buf) {
		write(buf, 2);
	}

	public static void handle(PacketContext<S2CConfigPacket> ctx) {

	}
}
