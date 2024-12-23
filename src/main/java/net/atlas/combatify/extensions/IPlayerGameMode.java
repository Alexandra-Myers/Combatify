package net.atlas.combatify.extensions;

import net.minecraft.world.entity.player.Player;

public interface IPlayerGameMode {
	default void swingInAir(Player var1) {
		throw new IllegalStateException("Extension has not been applied");
	}
}
