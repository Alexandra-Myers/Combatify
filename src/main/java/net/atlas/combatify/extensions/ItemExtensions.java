package net.atlas.combatify.extensions;

public interface ItemExtensions {

	default void modifyAttributeModifiers() {

	}

	void setStackSize(int stackSize);

	double getChargedAttackBonus();

}
