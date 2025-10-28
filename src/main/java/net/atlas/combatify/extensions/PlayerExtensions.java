package net.atlas.combatify.extensions;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;

public interface PlayerExtensions extends ClientInformationHolder {
	void combatify$resetAttackStrengthTicker(boolean hit, boolean force);

	boolean combatify$isAttackAvailable(float baseTime);

	void combatify$customSwing(InteractionHand interactionHand);

	void combatify$resetAttackStrengthTicker(boolean hit);

	default boolean ctsShieldDisable(float damage, Item item) {
		return false;
	}

	default boolean hasEnabledShieldOnCrouch() {
		return false;
	}
	boolean combatify$getMissedAttackRecovery();

    void combatify$attackAir();
}
