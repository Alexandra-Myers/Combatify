package net.atlas.combatify.extensions;

import net.minecraft.world.item.enchantment.Enchantment;

public interface IItemStack {
	int getEnchantmentLevel(Enchantment enchantment);
}
