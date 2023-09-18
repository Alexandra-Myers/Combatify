package net.atlas.combatify.networking;

import net.atlas.combatify.extensions.IServerGamePacketListener;
import net.atlas.combatify.extensions.PlayerExtensions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public class ServerboundMissPacket implements Packet<ServerGamePacketListener> {
	@Override
	public void write(FriendlyByteBuf p_131343_) {

	}

	@Override
	public void handle(ServerGamePacketListener p_131342_) {
		((PlayerExtensions)((IServerGamePacketListener)p_131342_).getPlayer()).attackAir();
	}
}
