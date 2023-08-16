package net.atlas.combat_enhanced.extensions;

import net.minecraft.world.entity.player.Player;

public interface ItemExtensions {

	default void modifyAttributeModifiers() {

	}

	double getAttackReach(Player player);

	double getAttackSpeed(Player player);

	double getAttackDamage(Player player);

	void setStackSize(int stackSize);

}
