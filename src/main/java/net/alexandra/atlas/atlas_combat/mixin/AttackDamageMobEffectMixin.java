package net.alexandra.atlas.atlas_combat.mixin;

import net.minecraft.world.effect.AttackDamageMobEffect;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(AttackDamageMobEffect.class)
public class AttackDamageMobEffectMixin {

	/**
	 * @author zOnlyKroks
	 * @reason because
	 */
	@Overwrite
	public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
		return ((amplifier + 1) * 0.2);
	}

}
