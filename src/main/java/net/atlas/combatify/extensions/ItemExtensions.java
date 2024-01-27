package net.atlas.combatify.extensions;

import net.atlas.combatify.util.BlockingType;

public interface ItemExtensions {

	default void modifyAttributeModifiers() {

	}

	void setStackSize(int stackSize);

	double getChargedAttackBonus();

    BlockingType getBlockingType();

    double getPiercingLevel();
}
