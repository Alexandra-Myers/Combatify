package net.atlas.combat_enhanced.extensions;

import net.minecraft.world.item.enchantment.Enchantment;

public interface IItemStack {
	int getEnchantmentLevel(Enchantment enchantment);
}
