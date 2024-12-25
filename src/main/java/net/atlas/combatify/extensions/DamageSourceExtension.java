package net.atlas.combatify.extensions;

import net.minecraft.world.damagesource.DamageSource;

public interface DamageSourceExtension {
	default boolean combatify$originatedFromBlockedAttack() {
		throw new IllegalStateException("Extension has not been applied");
	}
	default DamageSource combatify$originatesFromBlockedAttack(boolean origin) {
		throw new IllegalStateException("Extension has not been applied");
	}
}
