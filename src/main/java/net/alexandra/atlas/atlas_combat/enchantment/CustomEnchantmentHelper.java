package net.alexandra.atlas.atlas_combat.enchantment;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class CustomEnchantmentHelper {
	public static int getChopping(LivingEntity entity) {
		return EnchantmentHelper.getEnchantmentLevel(AtlasCombat.CLEAVING_ENCHANTMENT, entity);
	}

}
