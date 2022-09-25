package net.alexandra.atlas.atlas_combat.extensions;

import net.minecraft.world.item.ItemStack;

public interface CustomEnchantment {
	boolean isAcceptibleConditions(ItemStack stack);
	boolean isAcceptibleAnvil(ItemStack stack);
}
