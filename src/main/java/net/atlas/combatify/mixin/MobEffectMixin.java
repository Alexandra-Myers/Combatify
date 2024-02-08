package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import net.minecraft.world.effect.HealOrHarmMobEffect;

@Mixin(HealOrHarmMobEffect.class)
public class MobEffectMixin {

	@ModifyConstant(method = "applyEffectTick", constant = @Constant(intValue = 4))
	public int changeInstantHealthTick(int constant) {
		return Combatify.CONFIG.instantHealthBonus();
	}

	@ModifyConstant(method = "applyInstantenousEffect", constant = @Constant(intValue = 4))
	public int changeInstantHealth(int constant) {
		return Combatify.CONFIG.instantHealthBonus();
	}
}
