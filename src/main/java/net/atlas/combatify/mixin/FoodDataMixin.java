package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.wrapper.FoodDataWrapper;
import net.atlas.combatify.config.wrapper.PlayerWrapper;
import net.atlas.combatify.extensions.FoodDataExtensions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public class FoodDataMixin implements FoodDataExtensions {
	@Unique
	private final FoodData foodData = FoodData.class.cast(this);
	@WrapMethod(method = "add")
	public void capAt20(int food, float saturation, Operation<Void> original) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) {
			original.call(food, saturation);
			return;
		}
		boolean cancel = Combatify.CONFIG.getFoodImpl().execFunc("addFood(foodData, food, saturation)", new FoodDataWrapper(foodData), food, saturation);
		if (!cancel) original.call(food, saturation);
	}

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	public void onTickExecuteJS(ServerPlayer serverPlayer, CallbackInfo ci) {
		boolean cancel = Combatify.CONFIG.getFoodImpl().execFunc("processHungerTick(foodData, player)", new FoodDataWrapper(foodData), new PlayerWrapper<>(serverPlayer));
		if (cancel) ci.cancel();
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=18"))
	public int changeConst(int original, @Local(ordinal = 0, argsOnly = true) ServerPlayer player) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return original;
		return (int) Combatify.CONFIG.getFoodImpl().execGetterFunc(original, "getMinimumHealingLevel()");
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=20"))
	public int changeConst2(int original, @Local(ordinal = 0, argsOnly = true) ServerPlayer player) {
		if (Combatify.CONFIG.getFoodImpl().execFunc("canFastHeal(foodData, player)", new FoodDataWrapper(foodData), new PlayerWrapper<>(player)) || Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return (int) Combatify.CONFIG.getFoodImpl().execGetterFunc(original, "getMinimumFastHealingLevel()");
		return 1000000;
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=10", ordinal = 0))
	public int redirectTickTimer(int original, @Local(ordinal = 0, argsOnly = true) ServerPlayer player) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return original;
		return (int) (Combatify.CONFIG.getFoodImpl().execGetterFunc(original / 20.0, "getFastHealSeconds()") * 20);
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=80", ordinal = 0))
	public int redirectTickTimer1(int original, @Local(ordinal = 0, argsOnly = true) ServerPlayer player) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return original;
		return (int) (Combatify.CONFIG.getFoodImpl().execGetterFunc(original / 20.0, "getHealSeconds()") * 20);
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=80", ordinal = 1))
	public int redirectTickTimer2(int original, @Local(ordinal = 0, argsOnly = true) ServerPlayer player) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return original;
		return (int) (Combatify.CONFIG.getFoodImpl().execGetterFunc(original / 20.0, "getStarvationSeconds()") * 20);
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;heal(F)V", ordinal = 0))
	public void modifyFastHealing(ServerPlayer instance, float health, Operation<Void> original, @Share("shouldContinueVanillaHeal") LocalBooleanRef cont) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) {
			original.call(instance, health);
			return;
		}
		cont.set(Combatify.CONFIG.getFoodImpl().execFunc("fastHeal(foodData, player)", new FoodDataWrapper(foodData), new PlayerWrapper<>(instance)));
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

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;heal(F)V", ordinal = 1))
	public void modifyNaturalHealing(ServerPlayer instance, float health, Operation<Void> original, @Share("shouldContinueVanillaHeal") LocalBooleanRef cont) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) {
			original.call(instance, health);
			return;
		}
		cont.set(Combatify.CONFIG.getFoodImpl().execFunc("heal(foodData, player)", new FoodDataWrapper(foodData), new PlayerWrapper<>(instance)));
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
