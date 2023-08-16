package net.atlas.combat_enhanced.extensions;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public interface IEnchantmentHelper {
	float getDamageBonus(ItemStack level, LivingEntity entity);
	float getKnockbackDebuff(ItemStack level, LivingEntity entity);

	int getFullEnchantmentLevel(Enchantment enchantment, LivingEntity entity);
}
