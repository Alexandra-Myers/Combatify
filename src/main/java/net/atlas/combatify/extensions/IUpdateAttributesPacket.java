package net.atlas.combatify.extensions;

import net.minecraft.server.level.ServerPlayer;

public interface IUpdateAttributesPacket {
	void changeAttributes(ServerPlayer reciever);
}
