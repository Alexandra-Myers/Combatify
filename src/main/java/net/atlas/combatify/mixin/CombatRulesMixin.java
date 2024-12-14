package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ArmourPiercingMode;
import net.atlas.combatify.config.ItemConfig;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.extensions.LivingEntityExtensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CombatRules.class)
public class CombatRulesMixin {
	@Inject(method = "getDamageAfterAbsorb", at = @At("HEAD"))
	private static void changeArmourAndProtection(LivingEntity attacked, float f, DamageSource damageSource, float g, float h, CallbackInfoReturnable<Float> cir, @Local(ordinal = 1, argsOnly = true) LocalFloatRef armour, @Local(ordinal = 2, argsOnly = true) LocalFloatRef toughness) {
		if (Combatify.CONFIG.armourPiercingMode() == ArmourPiercingMode.APPLY_BEFORE_PERCENTAGE && damageSource.getEntity() instanceof LivingEntity livingEntity) {
			ItemStack held = livingEntity.getItemInHand(InteractionHand.MAIN_HAND);
			Item item = held.getItem();
			double d = ((ItemExtensions)item).getPiercingLevel();
			if (!(attacked.level() instanceof ServerLevel serverLevel)) d += CustomEnchantmentHelper.getBreach(held, livingEntity.getRandom());
			else d += CustomEnchantmentHelper.getArmorModifier(serverLevel, held, attacked, damageSource);
			d -= ((LivingEntityExtensions)livingEntity).getPiercingNegation();
			((LivingEntityExtensions)livingEntity).setPiercingNegation(0);
			armour.set((float) (armour.get() * (1 - d)));
			toughness.set((float) (toughness.get() * (1 - d)));
		}
	}
	@ModifyReturnValue(method = "getDamageAfterAbsorb", at = @At("RETURN"))
	private static float changeArmourCalcs(float original, @Local(ordinal = 0, argsOnly = true) float amount, @Local(ordinal = 0, argsOnly = true) DamageSource source, @Local(ordinal = 1, argsOnly = true) float armour, @Local(ordinal = 2, argsOnly = true) float toughness) {
		ItemConfig.Formula calcs = ItemConfig.getArmourCalcs();
		if (calcs != null) {
			original = calcs.armourCalcs(amount, source, armour, toughness);
			if (Combatify.CONFIG.enableDebugLogging())
				Combatify.LOGGER.info("Damage: " + amount + " Result: " + original);
		}
		return original;
	}
	@ModifyReturnValue(method = "getDamageAfterMagicAbsorb", at = @At("RETURN"))
	private static float changeEnchant(float original, @Local(ordinal = 0, argsOnly = true) float amount, @Local(ordinal = 1, argsOnly = true) float enchantLevel) {
		ItemConfig.Formula calcs = ItemConfig.getArmourCalcs();
		if (calcs != null)
			return calcs.enchantCalcs(amount, enchantLevel);
		return original;
	}
}
