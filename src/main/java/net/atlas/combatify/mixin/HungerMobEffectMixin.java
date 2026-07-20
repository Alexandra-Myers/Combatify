package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.minecraft.world.effect.HungerMobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HungerMobEffect.class)
public class HungerMobEffectMixin {
	@Definition(id = "amplification", local = @Local(type = int.class, argsOnly = true))
	@Expression("@(amplification) + 1")
	@ModifyExpressionValue(method = "applyEffectTick", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
	public int changeHungerExhaustion(int amplification) {
		if (Combatify.isStateVanilla()) return amplification;
		return Combatify.CONFIG.ctsHungerBuff() ? amplification * amplification * 5 : amplification;
	}
}
