package net.atlas.combatify.extensions;

import net.minecraft.world.item.ItemStack;

public interface CustomEnchantment {
	boolean isAcceptibleConditions(ItemStack stack);
	boolean isAcceptibleAnvil(ItemStack stack);
}
