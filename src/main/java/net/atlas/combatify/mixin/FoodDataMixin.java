package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.atlas.combatify.Combatify;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(FoodData.class)
public class FoodDataMixin {

	@Shadow
	private int foodLevel;


	@Shadow
	private float saturationLevel;

	@ModifyConstant(method = "tick", constant = @Constant(intValue = 18))
	public int changeConst(int constant) {
		return Combatify.CONFIG.healingMode().getMinimumHealLevel();
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=20"))
	public int changeConst2(int original) {
		if(Combatify.CONFIG.fastHealing())
			return original;
		return 1000000;
	}

	@ModifyConstant(method = "tick", constant = @Constant(intValue = 10,ordinal = 0))
	public int redirectTickTimer(int constant) {
		return (int) (Combatify.CONFIG.fastHealingTime() * 20);
	}

	@ModifyConstant(method = "tick", constant = @Constant(intValue = 80,ordinal = 0))
	public int redirectTickTimer1(int constant) {
		return (int) (Combatify.CONFIG.healingTime() * 20);
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;addExhaustion(F)V",ordinal = 1))
	public void modifyNaturalHealing(FoodData instance, float exhaustion, Operation<Void> original) {
		switch (Combatify.CONFIG.healingMode()) {
			case VANILLA -> original.call(instance, exhaustion);
			case CTS -> {
				if (Mth.randomBetweenInclusive(RandomSource.create(), 1, 2) == 2) --foodLevel;
			}
			case NEW -> {
				if (saturationLevel > 5.0) original.call(instance, exhaustion);
				else --foodLevel;
			}
		}
	}

}
