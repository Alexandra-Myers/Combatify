package net.atlas.combatify.extensions;

import net.minecraft.world.item.ItemStack;

public interface CustomEnchantment {
	boolean combatify$isAcceptibleConditions(ItemStack stack);
	boolean combatify$isAcceptibleAnvil(ItemStack stack);
}
