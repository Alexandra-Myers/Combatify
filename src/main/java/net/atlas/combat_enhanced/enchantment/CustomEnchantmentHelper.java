package net.atlas.combat_enhanced.enchantment;

import net.atlas.combat_enhanced.extensions.IEnchantments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public class CustomEnchantmentHelper {
	public static int getChopping(LivingEntity entity) {
		Enchantments enchantments = new Enchantments();
		return EnchantmentHelper.getEnchantmentLevel(((IEnchantments)enchantments).getCleavingEnchantment(), entity);
	}
	public static int getPierce(LivingEntity entity) {
		return EnchantmentHelper.getEnchantmentLevel(PiercingEnchantment.PIERCER, entity);
	}
	public static int getDefense(LivingEntity entity) {
		return EnchantmentHelper.getEnchantmentLevel(DefendingEnchantment.DEFENDER, entity);
	}

}
