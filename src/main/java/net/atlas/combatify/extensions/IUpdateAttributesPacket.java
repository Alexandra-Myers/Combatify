package net.atlas.combatify.extensions;

import net.minecraft.server.level.ServerPlayer;

public interface IUpdateAttributesPacket {
	default void combatify$changeAttributes(ServerPlayer reciever) {
		throw new IllegalStateException("Extension has not been applied");
	}
}
