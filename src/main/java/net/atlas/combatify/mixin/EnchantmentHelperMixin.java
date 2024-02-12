package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("unused")
@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
	@ModifyExpressionValue(method = "getEnchantmentCost", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getEnchantmentValue()I"))
	private static int getEnchantmentValue(int original, @Local(ordinal = 0, argsOnly = true) ItemStack stack) {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(stack.getItem())) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(stack.getItem());
			if (configurableItemData.enchantability != null)
				return configurableItemData.enchantability;
			if (configurableItemData.isEnchantable != null && original == 0)
				original = configurableItemData.isEnchantable ? 14 : 0;
		}
		return original;
	}
	@ModifyExpressionValue(method = "selectEnchantment", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getEnchantmentValue()I"))
	private static int getEnchantmentValue1(int original, @Local(ordinal = 0, argsOnly = true) ItemStack stack) {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(stack.getItem())) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(stack.getItem());
			if (configurableItemData.enchantability != null)
				return configurableItemData.enchantability;
			if (configurableItemData.isEnchantable != null && original == 0)
				original = configurableItemData.isEnchantable ? 14 : 0;
		}
		return original;
	}
}
