package net.atlas.combatify.mixin.compatibility.appleskin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.annotation.mixin.ModSpecific;
import net.atlas.combatify.config.HealingMode;
import net.minecraft.client.Minecraft;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import squeek.appleskin.api.event.HUDOverlayEvent;
import squeek.appleskin.client.HUDOverlayHandler;

@ModSpecific("appleskin")
@Mixin(HUDOverlayHandler.class)
public abstract class HUDOverlayHandlerMixin {
	@ModifyExpressionValue(method = "shouldShowEstimatedHealth", at = @At(value = "CONSTANT", args = "intValue=18"))
	public int modifyMinHunger(int original) {
		if (Combatify.state.equals(Combatify.CombatifyState.VANILLA)) return original;
		return original == Combatify.CONFIG.healingMode().getMinimumHealLevel() ? original : Combatify.CONFIG.healingMode().getMinimumHealLevel();
	}
	@ModifyExpressionValue(method = "onRenderFood", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodProperties;saturation()F"))
	public float captureFoodSaturationIncrement(float original, @Share("foodSaturationIncrement") LocalFloatRef inc) {
		inc.set(original);
		return original;
	}
	@WrapOperation(method = "onRenderFood", at = @At(value = "INVOKE", target = "Lsqueek/appleskin/client/HUDOverlayHandler;drawSaturationOverlay(Lsqueek/appleskin/api/event/HUDOverlayEvent$Saturation;Lnet/minecraft/client/Minecraft;FFI)V"))
	public void modifyNewSaturation(HUDOverlayHandler instance, HUDOverlayEvent.Saturation event, Minecraft mc, float saturationGained, float alpha, int guiTicks, Operation<Void> original, @Local(ordinal = 0) FoodData foodData, @Share("foodSaturationIncrement") LocalFloatRef foodSaturationIncrement) {
		if (Combatify.state.equals(Combatify.CombatifyState.VANILLA)) {
			original.call(instance, event, mc, saturationGained, alpha, guiTicks);
			return;
		}
		if (Combatify.CONFIG.ctsSaturationCap()) saturationGained = Math.max(foodData.getSaturationLevel(), foodSaturationIncrement.get()) - foodData.getSaturationLevel();
		else if (Combatify.CONFIG.healingMode() != HealingMode.VANILLA) saturationGained = foodSaturationIncrement.get();
		original.call(instance, event, mc, saturationGained, alpha, guiTicks);
	}
}
