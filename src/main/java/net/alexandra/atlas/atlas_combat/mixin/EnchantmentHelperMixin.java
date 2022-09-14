package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.extensions.IEnchantmentHelper;
import net.alexandra.atlas.atlas_combat.extensions.IItemStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin implements IEnchantmentHelper {
	@Shadow
	public static float getDamageBonus(ItemStack par1, MobType par2) {
		return 0;
	}

	@Override
	public float getDamageBonus(ItemStack level, LivingEntity entity){
		if(entity.getMobType() == MobType.WATER || entity.isInWaterOrRain()) {
			return getDamageBonus(level, MobType.WATER);
		}
		return getDamageBonus(level, entity.getMobType());
	}

	@Override
	public float getKnockbackDebuff(ItemStack level, LivingEntity entity){
		return (getDamageBonus(level, MobType.WATER)/2.5F);
	}
}
