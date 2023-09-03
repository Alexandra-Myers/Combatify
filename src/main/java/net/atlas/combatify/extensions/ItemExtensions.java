package net.atlas.combatify.extensions;

public interface ItemExtensions {

	default void modifyAttributeModifiers() {

	}

	void setStackSize(int stackSize);

	default double getChargedAttackBonus() {
		return 1.0F;
	}
}
