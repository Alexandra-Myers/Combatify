package net.atlas.combatify.mixin;

import net.minecraft.world.effect.AttackDamageMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AttackDamageMobEffect.class)
public class AttackDamageMobEffectMixin {
	@Mutable
	@Shadow
	@Final
	protected double multiplier;

	@Inject(method = "<init>", at = @At(value = "RETURN"))
	public void getAttributeModifierValue(MobEffectCategory mobEffectCategory, int i, double d, CallbackInfo ci) {
		if(mobEffectCategory == MobEffectCategory.BENEFICIAL)
			multiplier = 0.2;
		if(mobEffectCategory == MobEffectCategory.HARMFUL)
			multiplier = -0.2;
	}
}
