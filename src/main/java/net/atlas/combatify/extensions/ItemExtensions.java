package net.atlas.combatify.extensions;

import net.atlas.combatify.util.BlockingType;

public interface ItemExtensions {

	default void combatify$modifyAttributeModifiers() {

	}

	void combatify$setStackSize(int stackSize);

	double combatify$getChargedAttackBonus();

    BlockingType combatify$getBlockingType();
}
