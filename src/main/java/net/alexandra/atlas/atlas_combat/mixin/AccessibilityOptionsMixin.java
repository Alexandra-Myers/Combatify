package net.alexandra.atlas.atlas_combat.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.alexandra.atlas.atlas_combat.extensions.IOptions;
import net.alexandra.atlas.atlas_combat.util.ArrayListExtensions;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;

@Mixin(AccessibilityOptionsScreen.class)
public class AccessibilityOptionsMixin {

	@ModifyReturnValue(method = "options", at = @At("RETURN"))
	private static OptionInstance<?>[] injectOptions(OptionInstance<?>[] original, @Local(ordinal = 0) Options options) {
		var optionInstance = new ArrayListExtensions<>(Arrays.stream(original).toList());

		optionInstance.addAll(((IOptions)options).autoAttack(),
			((IOptions)options).shieldCrouch(),
			((IOptions)options).attackIndicatorValue(),
			((IOptions)options).fishingRodLegacy());

		return optionInstance.toArray(new OptionInstance[0]);
	}
}
