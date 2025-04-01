package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.HealingMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FoodData.class)
public class FoodDataMixin {

	@Shadow
	private int foodLevel;


	@Shadow
	private float saturationLevel;

	@WrapOperation(method = "add", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
	public float capAt20(float val, float min, float max, Operation<Float> original, @Local(ordinal = 0, argsOnly = true) float saturation) {
		if (Combatify.state.equals(Combatify.CombatifyState.VANILLA)) return original.call(val, min, max);
		if (Combatify.CONFIG.ctsSaturationCap()) return Math.max(saturationLevel, saturation);
		return original.call(val, min, Combatify.CONFIG.healingMode() == HealingMode.VANILLA ? max : 20);
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=18"))
	public int changeConst(int original) {
		if (Combatify.state.equals(Combatify.CombatifyState.VANILLA)) return original;
		return Combatify.CONFIG.healingMode().getMinimumHealLevel();
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=20"))
	public int changeConst2(int original) {
		if(Combatify.CONFIG.fastHealing() || Combatify.state.equals(Combatify.CombatifyState.VANILLA)) return original;
		return 1000000;
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=10", ordinal = 0))
	public int redirectTickTimer(int original) {
		if (Combatify.state.equals(Combatify.CombatifyState.VANILLA)) return original;
		return (int) (Combatify.CONFIG.fastHealingTime() * 20);
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=80", ordinal = 0))
	public int redirectTickTimer1(int original) {
		if (Combatify.state.equals(Combatify.CombatifyState.VANILLA)) return original;
		return (int) (Combatify.CONFIG.healingTime() * 20);
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=80", ordinal = 1))
	public int redirectTickTimer2(int original) {
		if (Combatify.state.equals(Combatify.CombatifyState.VANILLA)) return original;
		return (int) (Combatify.CONFIG.starvingTime() * 20);
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;addExhaustion(F)V",ordinal = 1))
	public void modifyNaturalHealing(FoodData instance, float exhaustion, Operation<Void> original, @Local(argsOnly = true) Player player) {
		if (Combatify.state.equals(Combatify.CombatifyState.VANILLA)) {
			original.call(instance, exhaustion);
			return;
		}
		switch (Combatify.CONFIG.healingMode()) {
			case VANILLA -> original.call(instance, exhaustion);
			case CTS -> {
				if (player.level().random.nextBoolean()) --foodLevel;
			}
			case NEW -> {
				if (saturationLevel > 5.0) original.call(instance, exhaustion);
				else --foodLevel;
			}
		}
	}

}
