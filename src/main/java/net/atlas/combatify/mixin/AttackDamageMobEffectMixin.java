package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.effect.AttackDamageMobEffect;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AttackDamageMobEffect.class)
public class AttackDamageMobEffectMixin {
	@ModifyReturnValue(method = "getAttributeModifierValue", at = @At(value = "RETURN"))
	public double getAttributeModifierValue(double original, @Local(ordinal = 0) int amplifier, @Local(ordinal = 0) AttributeModifier attributeModifier) {
		return attributeModifier.getAmount() * (double)(amplifier + 1);
	}
}
