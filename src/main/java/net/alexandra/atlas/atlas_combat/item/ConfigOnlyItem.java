package net.alexandra.atlas.atlas_combat.item;

import net.minecraft.world.item.ItemStack;

public interface ConfigOnlyItem {
	default void destroyWithoutConfig(ItemStack stack) {
	}
}
