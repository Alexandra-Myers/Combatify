package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.atlas.combatify.Combatify;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobEffect.class)
public class MobEffectMixin {
	@ModifyExpressionValue(method = "applyEffectTick", at = @At(value = "CONSTANT", args = "intValue=4"))
	public int changeInstantHealthTick(int original) {
		return Combatify.CONFIG.instantHealthBonus.get();
	}

	@ModifyExpressionValue(method = "applyInstantenousEffect", at = @At(value = "CONSTANT", args = "intValue=4"))
	public int changeInstantHealth(int original) {
		return Combatify.CONFIG.instantHealthBonus.get();
	}
}
