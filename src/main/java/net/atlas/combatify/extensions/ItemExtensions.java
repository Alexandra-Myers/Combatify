package net.atlas.combatify.extensions;

import net.atlas.combatify.util.BlockingType;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public interface ItemExtensions {

	default ItemAttributeModifiers modifyAttributeModifiers(ItemAttributeModifiers original) {
		return original;
	}

	void setStackSize(int stackSize);

	double getChargedAttackBonus();

	boolean canSweep();

    BlockingType getBlockingType();

	double getPiercingLevel();
}
