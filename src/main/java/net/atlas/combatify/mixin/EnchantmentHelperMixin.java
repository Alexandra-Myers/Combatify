package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.extensions.CustomEnchantment;
import net.atlas.combatify.util.CustomEnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("unused")
@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin implements CustomEnchantmentHelper {
	@ModifyExpressionValue(method = "getAvailableEnchantmentResults", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;canEnchant(Lnet/minecraft/world/item/ItemStack;)Z"))
	private static boolean redirectCanEnchant(boolean original, @Local(ordinal = 0) Enchantment currentEnchantment, @Local(ordinal = 0) ItemStack itemStack) {
		return itemStack != null ? original || ((CustomEnchantment)currentEnchantment).isAcceptibleConditions(itemStack) : original;
	}

	@ModifyExpressionValue(method = "getEnchantmentCost", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getEnchantmentValue()I"))
	private static int getEnchantmentValue(int original, @Local(ordinal = 0) ItemStack stack) {
		if(Combatify.CONFIG != null && Combatify.CONFIG.configuredItems.containsKey(stack.getItem())) {
			ConfigurableItemData configurableItemData = Combatify.CONFIG.configuredItems.get(stack.getItem());
			if (configurableItemData.enchantability != null)
				return configurableItemData.enchantability;
			if (configurableItemData.isEnchantable != null && original == 0)
				original = configurableItemData.isEnchantable ? 14 : 0;
		}
		return original;
	}
	@ModifyExpressionValue(method = "selectEnchantment", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getEnchantmentValue()I"))
	private static int getEnchantmentValue1(int original, @Local(ordinal = 0) ItemStack stack) {
		if(Combatify.CONFIG != null && Combatify.CONFIG.configuredItems.containsKey(stack.getItem())) {
			ConfigurableItemData configurableItemData = Combatify.CONFIG.configuredItems.get(stack.getItem());
			if (configurableItemData.enchantability != null)
				return configurableItemData.enchantability;
			if (configurableItemData.isEnchantable != null && original == 0)
				original = configurableItemData.isEnchantable ? 14 : 0;
		}
		return original;
	}
}
