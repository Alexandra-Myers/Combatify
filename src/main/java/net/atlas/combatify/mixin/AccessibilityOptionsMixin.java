package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.extensions.IOptions;
import net.atlas.combatify.util.ArrayListExtensions;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;

@Mixin(AccessibilityOptionsScreen.class)
public class AccessibilityOptionsMixin {

	@SuppressWarnings("unused")
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
