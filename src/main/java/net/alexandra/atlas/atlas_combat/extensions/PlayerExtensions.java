package net.alexandra.atlas.atlas_combat.extensions;

import net.minecraft.world.item.Item;

public interface PlayerExtensions {
	boolean isAttackAvailable(float baseTime);

	void resetAttackStrengthTicker(boolean var1);

	default boolean customShieldInteractions(float damage, Item item) {return false;}

	default boolean hasEnabledShieldOnCrouch() {
		return false;
	}
	boolean getMissedAttackRecovery();
	int getAttackStrengthStartValue();
	double getAttackRange();

	double getSquaredAttackRange();

    void attackAir();
}
