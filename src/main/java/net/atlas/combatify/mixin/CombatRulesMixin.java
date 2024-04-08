package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ItemConfig;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CombatRules.class)
public class CombatRulesMixin {
	@ModifyReturnValue(method = "getDamageAfterAbsorb", at = @At("RETURN"))
	private static float changeArmourCalcs(float original, @Local(ordinal = 0, argsOnly = true) float amount, @Local(ordinal = 0, argsOnly = true) DamageSource source, @Local(ordinal = 1, argsOnly = true) float armour, @Local(ordinal = 2, argsOnly = true) float toughness) {
		ItemConfig.Formula calcs = ItemConfig.getArmourCalcs();
		if (calcs != null) {
			original = calcs.armourCalcs(amount, source, armour, toughness);
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
