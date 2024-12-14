package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ArmourPiercingMode;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.extensions.LivingEntityExtensions;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
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
		ConfigurableItemData configurableItemData = MethodHandler.forItem(stack.getItem());
		if (configurableItemData != null) {
			if (configurableItemData.enchantability != null)
				return configurableItemData.enchantability;
			if (configurableItemData.isEnchantable != null && original == 0)
				original = configurableItemData.isEnchantable ? 14 : 0;
		}
		return original;
	}

	@ModifyExpressionValue(method = "selectEnchantment", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getEnchantmentValue()I"))
	private static int getEnchantmentValue1(int original, @Local(ordinal = 0, argsOnly = true) ItemStack stack) {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(stack.getItem());
		if (configurableItemData != null) {
			if (configurableItemData.enchantability != null)
				return configurableItemData.enchantability;
			if (configurableItemData.isEnchantable != null && original == 0)
				original = configurableItemData.isEnchantable ? 14 : 0;
		}
		return original;
	}

	@WrapMethod(method = "modifyArmorEffectiveness")
	private static float piercerMemories(ServerLevel serverLevel, ItemStack itemStack, Entity entity, DamageSource damageSource, float protection, Operation<Float> original) {
		return switch (entity) {
			case LivingEntity ignored when Combatify.CONFIG.armourPiercingMode() == ArmourPiercingMode.APPLY_BEFORE_PERCENTAGE -> protection;
			case LivingEntity livingEntity when Combatify.CONFIG.armourPiercingMode() == ArmourPiercingMode.APPLY_AFTER_PERCENTAGE -> {
				Item item = livingEntity.getItemInHand(InteractionHand.MAIN_HAND).getItem();
				double d = ((ItemExtensions)item).getPiercingLevel();
				d += CustomEnchantmentHelper.getArmorModifier(serverLevel, itemStack, entity, damageSource);
				d -= ((LivingEntityExtensions)livingEntity).getPiercingNegation();
				((LivingEntityExtensions)livingEntity).setPiercingNegation(0);
				yield (float) Mth.clamp(protection * (1 - d), 0, 1);
			}
			case LivingEntity livingEntity when Combatify.CONFIG.armourPiercingMode() == ArmourPiercingMode.VANILLA -> {
				Item item = livingEntity.getItemInHand(InteractionHand.MAIN_HAND).getItem();
				double d = ((ItemExtensions)item).getPiercingLevel();
				d -= ((LivingEntityExtensions)livingEntity).getPiercingNegation();
				((LivingEntityExtensions)livingEntity).setPiercingNegation(0);
				yield (float) Mth.clamp(original.call(serverLevel, itemStack, entity, damageSource, protection) - d, 0, protection);
			}
			case null, default -> original.call(serverLevel, itemStack, entity, damageSource, protection);
		};
	}
}
