package net.atlas.combatify.mixin;

import net.minecraft.world.effect.AttackDamageMobEffect;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AttackDamageMobEffect.class)
public class AttackDamageMobEffectMixin {

	@Shadow
	@Final
	protected double multiplier;

	@Inject(method = "getAttributeModifierValue", at = @At(value = "RETURN"), cancellable = true)
	public void getAttributeModifierValue(int amplifier, AttributeModifier attributeModifier, CallbackInfoReturnable<Double> cir) {
		cir.setReturnValue(this.multiplier * (double)(amplifier + 1));
	}

}
