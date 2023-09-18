package net.atlas.combatify.extensions;

import net.minecraft.server.level.ServerPlayer;

public interface IServerGamePacketListener {
	ServerPlayer getPlayer();
}
