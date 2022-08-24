package net.alexandra.atlas.atlas_combat.extensions;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public interface IPlayerGameMode {
	public void swingInAir(Player var1);
}
