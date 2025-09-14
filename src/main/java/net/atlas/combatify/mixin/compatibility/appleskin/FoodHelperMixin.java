package net.atlas.combatify.mixin.compatibility.appleskin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.annotation.mixin.ModSpecific;
import net.atlas.combatify.config.JSImpl;
import net.atlas.combatify.config.wrapper.FoodPropertiesWrapper;
import net.atlas.combatify.config.wrapper.SimpleAPIWrapper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import squeek.appleskin.helpers.FoodHelper;

@ModSpecific("appleskin")
@Mixin(FoodHelper.class)
public class FoodHelperMixin {
	@ModifyExpressionValue(method = "getEstimatedHealthIncrement(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/food/FoodProperties;)F", at = @At(value = "CONSTANT", args = "floatValue=18.0"))
	private static float modifyMinHunger(float original) {
		return (int) Combatify.CONFIG.getFoodImpl().execGetterFunc(original, "getMinimumHealingLevel()");
	}
	@WrapOperation(method = "getEstimatedHealthIncrement(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/food/FoodProperties;)F", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"))
	private static int modifyMinHunger(int i, int j, Operation<Integer> original, @Local(ordinal = 0) FoodData foodData, @Local(ordinal = 0) Player player, @Local(ordinal = 0, argsOnly = true) FoodProperties foodProperties) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return original.call(i, j);
		return (int) Combatify.CONFIG.getFoodImpl().execFoodGetterFunc(original.call(i, j), foodData, player, "estimateNewFoodLevel(foodData, player, foodProperties)", new JSImpl.Reference<>("foodProperties", foodProperties, FoodPropertiesWrapper::new));
	}
	@WrapOperation(method = "getEstimatedHealthIncrement(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/food/FoodProperties;)F", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(FF)F"))
	private static float modifyMinHunger(float a, float b, Operation<Float> original, @Local(ordinal = 0) FoodData foodData, @Local(ordinal = 0) Player player, @Local(ordinal = 0, argsOnly = true) FoodProperties foodProperties) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return original.call(a, b);
		return (float) Combatify.CONFIG.getFoodImpl().execFoodGetterFunc(original.call(a, b), foodData, player, "estimateNewSaturationLevel(foodData, player, foodProperties)", new JSImpl.Reference<>("foodProperties", foodProperties, FoodPropertiesWrapper::new));
	}
	@ModifyExpressionValue(method = "getEstimatedHealthIncrement(IFF)F", at = @At(value = "CONSTANT", args = "intValue=18"), remap = false)
	private static int modifyMinHunger(int original) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return original;
		return (int) Combatify.CONFIG.getFoodImpl().execGetterFunc(original, "getMinimumHealingLevel()");
	}
	@ModifyExpressionValue(method = "getEstimatedHealthIncrement(IFF)F", at = @At(value = "CONSTANT", args = "intValue=20"), remap = false)
	private static int changeConst2(int original, @Local(ordinal = 0, argsOnly = true) int foodLevel, @Local(ordinal = 0, argsOnly = true) float saturationLevel, @Local(ordinal = 1, argsOnly = true) float exhaustionLevel) {
		if(Combatify.CONFIG.getFoodImpl().execFunc("canFastHealRaw(foodLevel, saturationLevel, exhaustionLevel)", new JSImpl.Reference<>("foodLevel", new SimpleAPIWrapper<>(foodLevel)), new JSImpl.Reference<>("saturationLevel", new SimpleAPIWrapper<>(saturationLevel)), new JSImpl.Reference<>("exhaustionLevel", new SimpleAPIWrapper<>(exhaustionLevel))) || Combatify.getState().equals(Combatify.CombatifyState.VANILLA))
			return (int) Combatify.CONFIG.getFoodImpl().execGetterFunc(original, "getMinimumFastHealingLevel()");
		return 1000000;
	}
	@WrapMethod(method = "getEstimatedHealthIncrement(IFF)F", remap = false)
	private static float changeExhaustion(int foodLevel, float saturationLevel, float exhaustionLevel, Operation<Float> original) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return original.call(foodLevel, saturationLevel, exhaustionLevel);
		if (Combatify.CONFIG.getFoodImpl().execFunc("shouldOverrideAppleSkin()")) return (float) Combatify.CONFIG.getFoodImpl().execGetterFunc(0.0, "editAppleSkinHealthGained(foodLevel, saturationLevel, exhaustionLevel)", new JSImpl.Reference<>("foodLevel", new SimpleAPIWrapper<>(foodLevel)), new JSImpl.Reference<>("saturationLevel", new SimpleAPIWrapper<>(saturationLevel)), new JSImpl.Reference<>("exhaustionLevel", new SimpleAPIWrapper<>(exhaustionLevel)));
		return original.call(foodLevel, saturationLevel, exhaustionLevel);
	}
}
