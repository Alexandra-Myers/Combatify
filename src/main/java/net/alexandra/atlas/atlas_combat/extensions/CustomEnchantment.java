package net.alexandra.atlas.atlas_combat.extensions;

import net.minecraft.world.item.ItemStack;

public interface CustomEnchantment {
	boolean isAcceptableConditions(ItemStack stack);
	boolean isAcceptableAnvil(ItemStack stack);
}
