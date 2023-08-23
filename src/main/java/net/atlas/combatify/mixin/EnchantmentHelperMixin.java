package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.extensions.CustomEnchantment;
import net.atlas.combatify.util.CustomEnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin implements CustomEnchantmentHelper {
	@SuppressWarnings("unused")
	@ModifyExpressionValue(method = "getAvailableEnchantmentResults", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentCategory;canEnchant(Lnet/minecraft/world/item/Item;)Z"))
	private static boolean redirectCanEnchant(boolean original, @Local(ordinal = 0) Enchantment currentEnchantment, @Local(ordinal = 0) ItemStack itemStack) {
		return currentEnchantment instanceof CustomEnchantment customEnchantment && itemStack != null ? customEnchantment.isAcceptibleConditions(itemStack) : original;
	}
}
