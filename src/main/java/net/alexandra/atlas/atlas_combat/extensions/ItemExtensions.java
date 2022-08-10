package net.alexandra.atlas.atlas_combat.extensions;

import net.minecraft.world.entity.player.Player;

public interface ItemExtensions {

	double getAttackReach(Player player);

	double getAttackSpeed(Player player);

	double getAttackDamage(Player player);


	void setStackSize(int stackSize);

}
