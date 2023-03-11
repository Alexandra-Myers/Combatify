package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.extensions.IOptions;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(AccessibilityOptionsScreen.class)
public class AccessibilityOptionsMixin {

	@Inject(method = "options", at = @At("RETURN"), cancellable = true)
	private static void injectOptions(Options options, CallbackInfoReturnable<OptionInstance<?>[]> cir) {
		var optionInstance = new ArrayList<>(Arrays.stream(cir.getReturnValue()).toList());

		optionInstance.add(((IOptions)options).autoAttack());
		optionInstance.add(((IOptions)options).shieldCrouch());
		optionInstance.add(((IOptions)options).lowShield());
		optionInstance.add(((IOptions)options).attackIndicatorValue());
		optionInstance.add(((IOptions)options).fishingRodLegacy());

		cir.setReturnValue(optionInstance.toArray(new OptionInstance[0]));
	}
}
