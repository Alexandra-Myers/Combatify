package net.alexandra.atlas.atlas_combat.enchantment;

import net.alexandra.atlas.atlas_combat.extensions.IEnchantments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import static net.alexandra.atlas.atlas_combat.enchantment.DefendingEnchantment.DEFENDER;
import static net.alexandra.atlas.atlas_combat.enchantment.PiercingEnchantment.PIERCER;

public class CustomEnchantmentHelper {
	public static int getChopping(LivingEntity entity) {
		Enchantments enchantments = new Enchantments();
		return EnchantmentHelper.getEnchantmentLevel(((IEnchantments)enchantments).getCleavingEnchantment(), entity);
	}
	public static int getPierce(LivingEntity entity) {
		return EnchantmentHelper.getEnchantmentLevel(PIERCER, entity);
	}
	public static int getDefense(LivingEntity entity) {
		return EnchantmentHelper.getEnchantmentLevel(DEFENDER, entity);
	}

}
