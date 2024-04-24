package net.atlas.combatify.enchantment;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.IEnchantments;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public class CustomEnchantmentHelper {
	public static int getChopping(LivingEntity entity) {
		Enchantments enchantments = new Enchantments();
		return EnchantmentHelper.getEnchantmentLevel(((IEnchantments)enchantments).getCleavingEnchantment(), entity);
	}
	public static double getBreach(LivingEntity entity) {
		return EnchantmentHelper.getEnchantmentLevel(Enchantments.BREACH, entity) * Combatify.CONFIG.breachArmorPiercing();
	}
	public static int getDefense(LivingEntity entity) {
		return EnchantmentHelper.getEnchantmentLevel(DefendingEnchantment.DEFENDER, entity);
	}
	public static float getDamageBonus(ItemStack level, LivingEntity entity) {
		if (Combatify.CONFIG.bedrockImpaling() && (entity.getType().is(EntityTypeTags.SENSITIVE_TO_IMPALING) || entity.isInWaterOrRain())) {
			return EnchantmentHelper.getDamageBonus(level, EntityType.GUARDIAN);
		}
		return EnchantmentHelper.getDamageBonus(level, entity.getType());
	}
}
