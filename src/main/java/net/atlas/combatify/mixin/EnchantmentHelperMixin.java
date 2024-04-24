package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ArmourPiercingMode;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.extensions.LivingEntityExtensions;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
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

	@ModifyReturnValue(method = "getSweepingDamageRatio(I)F", at = @At(value = "RETURN"))
	private static float getSweepingDamageRatio(float original, @Local(ordinal = 0, argsOnly = true) int lvl) {
		return Combatify.CONFIG.vanillaSweep() ? original : 0.5F - 0.5F / (float)(lvl + 1);
	}

	@ModifyReturnValue(method = "calculateArmorBreach", at = @At(value = "RETURN"))
	private static float piercerMemories(float original, @Local(ordinal = 0, argsOnly = true) Entity entity, @Local(ordinal = 0, argsOnly = true) float protection) {
		return switch (entity) {
			case LivingEntity ignored when Combatify.CONFIG.armourPiercingMode() == ArmourPiercingMode.APPLY_BEFORE_PERCENTAGE -> protection;
			case LivingEntity livingEntity when Combatify.CONFIG.armourPiercingMode() == ArmourPiercingMode.APPLY_AFTER_PERCENTAGE -> {
				Item item = livingEntity.getItemInHand(InteractionHand.MAIN_HAND).getItem();
				double d = ((ItemExtensions)item).getPiercingLevel();
				d += CustomEnchantmentHelper.getBreach(livingEntity);
				d -= ((LivingEntityExtensions)livingEntity).getPiercingNegation();
				((LivingEntityExtensions)livingEntity).setPiercingNegation(0);
				yield (float) Mth.clamp(protection * (1 - d), 0, 1);
			}
			case LivingEntity livingEntity when Combatify.CONFIG.armourPiercingMode() == ArmourPiercingMode.VANILLA -> {
				Item item = livingEntity.getItemInHand(InteractionHand.MAIN_HAND).getItem();
				double d = ((ItemExtensions)item).getPiercingLevel();
				d -= ((LivingEntityExtensions)livingEntity).getPiercingNegation();
				((LivingEntityExtensions)livingEntity).setPiercingNegation(0);
				yield (float) Mth.clamp(original - d, 0, protection);
			}
			case null, default -> original;
		};
	}
}
