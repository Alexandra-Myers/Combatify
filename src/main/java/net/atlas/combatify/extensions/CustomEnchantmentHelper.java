package net.atlas.combatify.extensions;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import static net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel;

public interface CustomEnchantmentHelper {
	static float getDamageBonus(ItemStack level, LivingEntity entity) {
		if(entity.getMobType() == MobType.WATER || entity.isInWaterOrRain()) {
			return EnchantmentHelper.getDamageBonus(level, MobType.WATER);
		}
		return EnchantmentHelper.getDamageBonus(level, entity.getMobType());
	}
	static float getKnockbackDebuff(ItemStack level, LivingEntity entity) {
		return getDamageBonus(level, entity) / 2.5F;
	}

	static int getFullEnchantmentLevel(Enchantment enchantment, LivingEntity entity) {
		Iterable<ItemStack> iterable = enchantment.getSlotItems(entity).values();
		int i = 0;

		for(ItemStack itemStack : iterable) {
			int j = getItemEnchantmentLevel(enchantment, itemStack);
			i += j;
		}

		return i;
	}
}
