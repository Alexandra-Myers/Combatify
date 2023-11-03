package net.atlas.combatify.extensions;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;

public interface PlayerExtensions {
	boolean combatify$isAttackAvailable(float baseTime);

	void combatify$customSwing(InteractionHand interactionHand);

	void combatify$resetAttackStrengthTicker(boolean var1);

	default boolean combatify$ctsShieldDisable(float damage, Item item) {
		return false;
	}

	default boolean hasEnabledShieldOnCrouch() {
		return false;
	}
	boolean combatify$getMissedAttackRecovery();

    void combatify$attackAir();

	int combatify$getAttackStrengthStartValue();
}
