package net.atlas.combatify.mixin.compatibility.appleskin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.annotation.mixin.ModSpecific;
import net.atlas.combatify.config.JSImpl;
import net.atlas.combatify.config.wrapper.FoodPropertiesWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import squeek.appleskin.api.event.HUDOverlayEvent;
import squeek.appleskin.client.HUDOverlayHandler;
import squeek.appleskin.helpers.FoodHelper;

@ModSpecific("appleskin")
@Mixin(HUDOverlayHandler.class)
public abstract class HUDOverlayHandlerMixin {
	@ModifyExpressionValue(method = "shouldShowEstimatedHealth", at = @At(value = "CONSTANT", args = "intValue=18"))
	public int modifyMinHunger(int original) {
		if (Combatify.state.equals(Combatify.CombatifyState.VANILLA)) return original;
		return (int) Combatify.CONFIG.getFoodImpl().execGetterFunc(original, "getMinimumHealingLevel()");
	}
	@ModifyExpressionValue(method = "onRenderFood", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodProperties;saturation()F"))
	public float captureFoodSaturationIncrement(float original, @Share("foodSaturationIncrement") LocalFloatRef inc) {
		inc.set(original);
		return original;
	}
	@WrapOperation(method = "onRenderFood", at = @At(value = "INVOKE", target = "Lsqueek/appleskin/client/HUDOverlayHandler;drawHungerOverlay(Lsqueek/appleskin/api/event/HUDOverlayEvent$HungerRestored;Lnet/minecraft/client/Minecraft;IFZI)V"))
	public void modifyNewHunger(HUDOverlayHandler instance, HUDOverlayEvent.HungerRestored event, Minecraft mc, int hunger, float alpha, boolean useRottenTextures, int guiTicks, Operation<Void> original, @Local(ordinal = 0) FoodData foodData, @Local(ordinal = 0, argsOnly = true) Player player, @Local(ordinal = 0) FoodHelper.QueriedFoodResult foodResult) {
		if (Combatify.state.equals(Combatify.CombatifyState.VANILLA)) {
			original.call(instance, event, mc, hunger, alpha, useRottenTextures, guiTicks);
			return;
		}
		original.call(instance, event, mc, (int) Combatify.CONFIG.getFoodImpl().execFoodGetterFunc(hunger, player.getFoodData(), player, "estimateGainedFoodLevel(foodData, player, foodProperties)", new JSImpl.Reference<>("foodProperties", foodResult.modifiedFoodComponent, FoodPropertiesWrapper::new)), alpha, useRottenTextures, guiTicks);
	}
	@WrapOperation(method = "onRenderFood", at = @At(value = "INVOKE", target = "Lsqueek/appleskin/client/HUDOverlayHandler;drawSaturationOverlay(Lsqueek/appleskin/api/event/HUDOverlayEvent$Saturation;Lnet/minecraft/client/Minecraft;FFI)V", ordinal = 1))
	public void modifyNewSaturation(HUDOverlayHandler instance, HUDOverlayEvent.Saturation event, Minecraft mc, float saturationGained, float alpha, int guiTicks, Operation<Void> original, @Local(ordinal = 0) FoodData foodData, @Local(ordinal = 0, argsOnly = true) Player player, @Local(ordinal = 0) FoodHelper.QueriedFoodResult foodResult) {
		if (Combatify.state.equals(Combatify.CombatifyState.VANILLA)) {
			original.call(instance, event, mc, saturationGained, alpha, guiTicks);
			return;
		}
		original.call(instance, event, mc, (float) Combatify.CONFIG.getFoodImpl().execFoodGetterFunc(saturationGained, player.getFoodData(), player, "estimateGainedSaturationLevel(foodData, player, foodProperties)", new JSImpl.Reference<>("foodProperties", foodResult.modifiedFoodComponent, FoodPropertiesWrapper::new)), alpha, guiTicks);
	}
}
