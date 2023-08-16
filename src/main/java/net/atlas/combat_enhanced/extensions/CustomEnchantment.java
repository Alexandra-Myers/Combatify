package net.atlas.combat_enhanced.extensions;

import net.minecraft.world.item.ItemStack;

public interface CustomEnchantment {
	boolean isAcceptibleConditions(ItemStack stack);
	boolean isAcceptibleAnvil(ItemStack stack);
}
