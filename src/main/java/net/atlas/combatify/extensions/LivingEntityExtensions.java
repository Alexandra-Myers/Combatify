package net.atlas.combatify.extensions;

import net.minecraft.world.item.ItemCooldowns;

public interface LivingEntityExtensions {
    default double combatify$getPiercingNegation() {
		throw new IllegalStateException("Extension has not been applied");
	}

    default boolean combatify$hasEnabledShieldOnCrouch() {
		throw new IllegalStateException("Extension has not been applied");
	}

    default ItemCooldowns combatify$getFallbackCooldowns() {
		throw new IllegalStateException("Extension has not been applied");
	}

    default void combatify$setPiercingNegation(double negation) {
		throw new IllegalStateException("Extension has not been applied");
	}

    default void combatify$setUseItemRemaining(int useItemRemaining) {
		throw new IllegalStateException("Extension has not been applied");
	}
}
