package net.atlas.combatify.mixin.compatibility.appleskin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.annotation.mixin.ModSpecific;
import net.atlas.combatify.config.HealingMode;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import squeek.appleskin.helpers.ConsumableFood;
import squeek.appleskin.helpers.FoodHelper;

@ModSpecific("appleskin")
@Mixin(FoodHelper.class)
public class FoodHelperMixin {
	@ModifyExpressionValue(method = "getEstimatedHealthIncrement(Lnet/minecraft/world/entity/player/Player;Lsqueek/appleskin/helpers/ConsumableFood;)F", at = @At(value = "CONSTANT", args = "floatValue=18.0"))
	private static float modifyMinHunger(float original) {
		if (Combatify.state.equals(Combatify.CombatifyState.VANILLA)) return original;
		return original == Combatify.CONFIG.healingMode().getMinimumHealLevel() ? original : Combatify.CONFIG.healingMode().getMinimumHealLevel();
	}
	@WrapOperation(method = "getEstimatedHealthIncrement(Lnet/minecraft/world/entity/player/Player;Lsqueek/appleskin/helpers/ConsumableFood;)F", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(FF)F"))
	private static float modifyMinHunger(float a, float b, Operation<Float> original, @Local(ordinal = 0) FoodData foodData, @Local(ordinal = 0, argsOnly = true) ConsumableFood consumableFood) {
		if (Combatify.state.equals(Combatify.CombatifyState.VANILLA)) return original.call(a, b);
		return original.call(a, Combatify.CONFIG.healingMode() == HealingMode.VANILLA ? b : 20);
	}

	@ModifyArg(method = "getEstimatedHealthIncrement(Lnet/minecraft/world/entity/player/Player;Lsqueek/appleskin/helpers/ConsumableFood;)F", at = @At(value = "INVOKE", target = "Lsqueek/appleskin/helpers/FoodHelper;getEstimatedHealthIncrement(IFF)F"), index = 1)
	private static float modSaturation(float saturationLevel, @Local(ordinal = 0) FoodData foodData, @Local(ordinal = 0, argsOnly = true) ConsumableFood consumableFood) {
		if (Combatify.CONFIG.ctsSaturationCap() && !Combatify.state.equals(Combatify.CombatifyState.VANILLA)) return Math.max(foodData.getSaturationLevel(), consumableFood.food().saturation());
		return saturationLevel;
	}

	@ModifyExpressionValue(method = "getEstimatedHealthIncrement(IFF)F", at = @At(value = "CONSTANT", args = "intValue=18"), remap = false)
	private static int modifyMinHunger(int original) {
		if (Combatify.state.equals(Combatify.CombatifyState.VANILLA)) return original;
		return original == Combatify.CONFIG.healingMode().getMinimumHealLevel() ? original : Combatify.CONFIG.healingMode().getMinimumHealLevel();
	}
	@ModifyExpressionValue(method = "getEstimatedHealthIncrement(IFF)F", at = @At(value = "CONSTANT", args = "intValue=20"), remap = false)
	private static int changeConst2(int original) {
		if (Combatify.CONFIG.fastHealing() || Combatify.state.equals(Combatify.CombatifyState.VANILLA))
			return original;
		return 1000000;
	}
	@ModifyExpressionValue(method = "getEstimatedHealthIncrement(IFF)F", at = @At(value = "FIELD", target = "Lsqueek/appleskin/helpers/FoodHelper;REGEN_EXHAUSTION_INCREMENT:F", ordinal = 2), remap = false)
	private static float changeExhaustion(float original, @Local(ordinal = 0, argsOnly = true) LocalIntRef foodLevel, @Local(ordinal = 0, argsOnly = true) float saturationLevel, @Local(ordinal = 2) float health) {
		if (Combatify.state.equals(Combatify.CombatifyState.VANILLA)) return original;
		return switch (Combatify.CONFIG.healingMode()) {
			case VANILLA -> original;
			case CTS -> {
				if (health % 2 == 0) foodLevel.set(foodLevel.get() - 1);
				yield 0;
			}
			case NEW -> {
				if (saturationLevel > 5.0) yield original;
				else foodLevel.set(foodLevel.get() - 1);
				yield original;
			}
		};
	}
}
