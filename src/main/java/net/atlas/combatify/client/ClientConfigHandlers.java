package net.atlas.combatify.client;

import net.minecraft.network.FriendlyByteBuf;

import static net.atlas.combatify.config.ConfigSynchronizer.applyClient;

public class ClientConfigHandlers {
	public static void generalConfigPacket(FriendlyByteBuf buf) {
		applyClient(buf);
	}
}
