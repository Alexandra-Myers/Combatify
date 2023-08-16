package net.atlas.combat_enhanced.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combat_enhanced.extensions.CustomEnchantment;
import net.atlas.combat_enhanced.extensions.IEnchantmentHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel;

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
		return getDamageBonus(level, MobType.WATER)/2.5F;
	}
	@Override
	public int getFullEnchantmentLevel(Enchantment enchantment, LivingEntity entity) {
		Iterable<ItemStack> iterable = enchantment.getSlotItems(entity).values();
		int i = 0;

		for(ItemStack itemStack : iterable) {
			int j = getItemEnchantmentLevel(enchantment, itemStack);
			i += j;
		}

		return i;
	}
	@ModifyExpressionValue(method = "getAvailableEnchantmentResults", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentCategory;canEnchant(Lnet/minecraft/world/item/Item;)Z"))
	private static boolean redirectCanEnchant(boolean original, @Local(ordinal = 0) Enchantment currentEnchantment, @Local(ordinal = 0) ItemStack itemStack) {
		return currentEnchantment instanceof CustomEnchantment customEnchantment && itemStack != null ? customEnchantment.isAcceptibleConditions(itemStack) : original;
	}
}
