package net.atlas.combatify.extensions;

import net.minecraft.world.InteractionHand;

public interface PlayerExtensions extends ClientInformationHolder {
	boolean isAttackAvailable(float baseTime);

	void customSwing(InteractionHand interactionHand);

	void resetAttackStrengthTicker(boolean var1);

	boolean getMissedAttackRecovery();

    void attackAir();
}
