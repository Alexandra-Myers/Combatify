package net.atlas.combatify.mixin.compatibility.appleskin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.annotation.mixin.ModSpecific;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import squeek.appleskin.helpers.ConsumableFood;
import squeek.appleskin.helpers.FoodHelper;
@ModSpecific("appleskin")
@Mixin(FoodHelper.class)
public class FoodHelperMixin {
	@ModifyExpressionValue(method = "getEstimatedHealthIncrement(Lnet/minecraft/world/entity/player/Player;Lsqueek/appleskin/helpers/ConsumableFood;)F", at = @At(value = "CONSTANT", args = "floatValue=18.0"))
	private static float modifyMinHunger(float original) {
		return Combatify.CONFIG.getFoodImpl().getMinimumHealingLevel((int) original);
	}
	@WrapOperation(method = "getEstimatedHealthIncrement(Lnet/minecraft/world/entity/player/Player;Lsqueek/appleskin/helpers/ConsumableFood;)F", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"))
	private static int modifyMinHunger(int i, int j, Operation<Integer> original, @Local(ordinal = 0) FoodData foodData, @Local(ordinal = 0, argsOnly = true) Player player, @Local(ordinal = 0, argsOnly = true) ConsumableFood food) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return original.call(i, j);
		return Combatify.CONFIG.getFoodImpl().estimateNewFoodLevel(original.call(i, j), foodData, player, food.food());
	}
	@WrapOperation(method = "getEstimatedHealthIncrement(Lnet/minecraft/world/entity/player/Player;Lsqueek/appleskin/helpers/ConsumableFood;)F", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(FF)F"))
	private static float modifyMinHunger(float a, float b, Operation<Float> original, @Local(ordinal = 0) FoodData foodData, @Local(ordinal = 0, argsOnly = true) Player player, @Local(ordinal = 0, argsOnly = true) ConsumableFood food) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return original.call(a, b);
		return Combatify.CONFIG.getFoodImpl().estimateNewSaturationLevel(original.call(a, b), foodData, player, food.food());
	}
	@ModifyExpressionValue(method = "getEstimatedHealthIncrement(IFF)F", at = @At(value = "CONSTANT", args = "intValue=18"), remap = false)
	private static int modifyMinHunger(int original) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return original;
		return Combatify.CONFIG.getFoodImpl().getMinimumHealingLevel(original);
	}
	@ModifyExpressionValue(method = "getEstimatedHealthIncrement(IFF)F", at = @At(value = "CONSTANT", args = "intValue=20"), remap = false)
	private static int changeConst2(int original, @Local(ordinal = 0, argsOnly = true) int foodLevel, @Local(ordinal = 0, argsOnly = true) float saturationLevel, @Local(ordinal = 1, argsOnly = true) float exhaustionLevel) {
		if(Combatify.CONFIG.getFoodImpl().canFastHealRaw(foodLevel, saturationLevel, exhaustionLevel) || Combatify.getState().equals(Combatify.CombatifyState.VANILLA))
			return Combatify.CONFIG.getFoodImpl().getMinimumFastHealingLevel(original);
		return 1000000;
	}
	@WrapMethod(method = "getEstimatedHealthIncrement(IFF)F", remap = false)
	private static float changeExhaustion(int foodLevel, float saturationLevel, float exhaustionLevel, Operation<Float> original) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return original.call(foodLevel, saturationLevel, exhaustionLevel);
		if (Combatify.CONFIG.getFoodImpl().shouldOverrideAppleSkin()) return Combatify.CONFIG.getFoodImpl().editAppleSkinHealthGained(foodLevel, saturationLevel, exhaustionLevel);
		return original.call(foodLevel, saturationLevel, exhaustionLevel);
	}
}
