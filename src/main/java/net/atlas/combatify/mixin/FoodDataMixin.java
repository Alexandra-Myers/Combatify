package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.JSImpl;
import net.atlas.combatify.config.wrapper.SimpleAPIWrapper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public class FoodDataMixin {
	@WrapMethod(method = "add")
	public void capAt20(int i, float f, Operation<Void> original) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) {
			original.call(i, f);
			return;
		}
		boolean cancel = Combatify.CONFIG.getFoodImpl().execFoodFunc(FoodData.class.cast(this), null, "addFood(foodData, food, saturation)", new JSImpl.Reference<>("food", new SimpleAPIWrapper<>(i)), new JSImpl.Reference<>("saturation", new SimpleAPIWrapper<>(f)));
		if (!cancel) original.call(i, f);
	}

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	public void onTickExecuteJS(Player player, CallbackInfo ci) {
		boolean cancel = Combatify.CONFIG.getFoodImpl().execFoodFunc(FoodData.class.cast(this), player, "processHungerTick(foodData, player)");
		if (cancel) ci.cancel();
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=18"))
	public int changeConst(int original, @Local(ordinal = 0, argsOnly = true) Player player) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return original;
		return (int) Combatify.CONFIG.getFoodImpl().execGetterFunc(original, "getMinimumHealingLevel()");
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=20"))
	public int changeConst2(int original, @Local(ordinal = 0, argsOnly = true) Player player) {
		if (Combatify.CONFIG.getFoodImpl().execFoodFunc(FoodData.class.cast(this), player, "canFastHeal(foodData, player)") || Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return (int) Combatify.CONFIG.getFoodImpl().execGetterFunc(original, "getMinimumFastHealingLevel()");
		return 1000000;
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=10", ordinal = 0))
	public int redirectTickTimer(int original, @Local(ordinal = 0, argsOnly = true) Player player) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return original;
		return (int) (Combatify.CONFIG.getFoodImpl().execGetterFunc(original / 20.0, "getFastHealSeconds()") * 20);
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=80", ordinal = 0))
	public int redirectTickTimer1(int original, @Local(ordinal = 0, argsOnly = true) Player player) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return original;
		return (int) (Combatify.CONFIG.getFoodImpl().execGetterFunc(original / 20.0, "getHealSeconds()") * 20);
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=80", ordinal = 1))
	public int redirectTickTimer2(int original, @Local(ordinal = 0, argsOnly = true) Player player) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return original;
		return (int) (Combatify.CONFIG.getFoodImpl().execGetterFunc(original / 20.0, "getStarvationSeconds()") * 20);
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;heal(F)V", ordinal = 0))
	public void modifyFastHealing(Player instance, float health, Operation<Void> original, @Share("shouldContinueVanillaHeal") LocalBooleanRef cont) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) {
			original.call(instance, health);
			return;
		}
		cont.set(Combatify.CONFIG.getFoodImpl().execFoodFunc(FoodData.class.cast(this), instance, "fastHeal(foodData, player)"));
		if (!cont.get()) {
			original.call(instance, health);
		}
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;addExhaustion(F)V", ordinal = 0))
	public void modifyFastHealing(FoodData instance, float exhaustion, Operation<Void> original, @Share("shouldContinueVanillaHeal") LocalBooleanRef cont) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) {
			original.call(instance, exhaustion);
			return;
		}
		if (!cont.get()) {
			original.call(instance, exhaustion);
		}
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;heal(F)V", ordinal = 1))
	public void modifyNaturalHealing(Player instance, float health, Operation<Void> original, @Share("shouldContinueVanillaHeal") LocalBooleanRef cont) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) {
			original.call(instance, health);
			return;
		}
		cont.set(Combatify.CONFIG.getFoodImpl().execFoodFunc(FoodData.class.cast(this), instance, "heal(foodData, player)"));
		if (!cont.get()) {
			original.call(instance, health);
		}
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;addExhaustion(F)V", ordinal = 1))
	public void modifyNaturalHealing(FoodData instance, float exhaustion, Operation<Void> original, @Share("shouldContinueVanillaHeal") LocalBooleanRef cont) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) {
			original.call(instance, exhaustion);
			return;
		}
		if (!cont.get()) {
			original.call(instance, exhaustion);
		}
	}
}
