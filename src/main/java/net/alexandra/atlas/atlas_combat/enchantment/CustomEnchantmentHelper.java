package net.alexandra.atlas.atlas_combat.enchantment;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.extensions.IEnchantments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public class CustomEnchantmentHelper {
	public static int getChopping(LivingEntity entity) {
		Enchantments enchantments = new Enchantments();
		return EnchantmentHelper.getEnchantmentLevel(((IEnchantments)enchantments).getCleavingEnchantment(), entity);
	}

}
