package net.atlas.combat_enhanced.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.atlas.combat_enhanced.CombatEnhanced;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FoodData.class)
public class FoodDataMixin {

	@Shadow
	private int foodLevel;


	@ModifyConstant(method = "tick", constant = @Constant(intValue = 18))
	public int changeConst(int constant) {
		if(CombatEnhanced.CONFIG.saturationHealing()) return constant;
		return 7;
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=20"))
	public int changeConst2(int original) {
		if(CombatEnhanced.CONFIG.fastHealing())
			return original;
		return 1000000;
	}

	@ModifyConstant(method = "tick", constant = @Constant(intValue = 80,ordinal = 0))
	public int redirectTickTimer(int constant) {
		return 40;
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;addExhaustion(F)V",ordinal = 1))
	public void modifyNaturalHealing(FoodData instance, float exhaustion) {
		if(CombatEnhanced.CONFIG.saturationHealing()) {
			instance.addExhaustion(exhaustion);
			return;
		}
		int randomNumber = Mth.randomBetweenInclusive(RandomSource.create(), 1, 2);
		if(randomNumber == 2) {
			--foodLevel;
		}
	}

}
