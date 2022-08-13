package net.alexandra.atlas.atlas_combat.extensions;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

public interface IEnchantmentHelper {
	static float getDamageBonus(ItemStack level, LivingEntity entity){
		return entity == null || entity.getMobType() != MobType.WATER && !entity.isInWaterOrRain() ? 0.0F : (float)((IItemStack)(Object)level).getEnchantmentLevel(Enchantments.IMPALING) * 2.5F;
	}
}
