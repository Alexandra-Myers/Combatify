package net.atlas.combatify.extensions;

import net.minecraft.world.InteractionHand;

public interface PlayerExtensions extends ClientInformationHolder {
	default boolean combatify$isAttackAvailable(float baseTime) {
		throw new IllegalStateException("Extension has not been applied");
	}

	default void combatify$customSwing(InteractionHand interactionHand) {
		throw new IllegalStateException("Extension has not been applied");
	}

	default void combatify$resetAttackStrengthTicker(boolean var1) {
		throw new IllegalStateException("Extension has not been applied");
	}

	default boolean combatify$getMissedAttackRecovery() {
		throw new IllegalStateException("Extension has not been applied");
	}

    default void combatify$attackAir() {
		throw new IllegalStateException("Extension has not been applied");
	}
}
