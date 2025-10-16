package net.atlas.combatify.mixin.compatibility.appleskin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.atlas.combatify.Combatify;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import squeek.appleskin.client.HUDOverlayHandler;

@Mixin(HUDOverlayHandler.class)
public abstract class HUDOverlayHandlerMixin {
	@ModifyExpressionValue(method = "shouldShowEstimatedHealth", at = @At(value = "CONSTANT", args = "intValue=18"))
	private static int modifyMinHunger(int original) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return original;
		return (int) Combatify.CONFIG.getFoodImpl().execGetterFunc(original, "getMinimumHealingLevel()");
	}
}
