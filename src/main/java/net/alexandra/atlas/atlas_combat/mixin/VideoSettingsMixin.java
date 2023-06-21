package net.alexandra.atlas.atlas_combat.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.alexandra.atlas.atlas_combat.extensions.IOptions;
import net.alexandra.atlas.atlas_combat.util.ArrayListExtensions;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;

@Mixin(VideoSettingsScreen.class)
public class VideoSettingsMixin {
	@ModifyReturnValue(method = "options", at = @At("RETURN"))
	private static OptionInstance<?>[] injectOptions(OptionInstance<?>[] original, @Local(ordinal = 0) Options options) {
		var optionInstance = new ArrayListExtensions<>(Arrays.stream(original).toList());
		int i = optionInstance.indexOf(options.attackIndicator());

		optionInstance.add(i + 1, ((IOptions)options).shieldIndicator());
		optionInstance.addAll(((IOptions)options).lowShield(),
			((IOptions)options).attackIndicatorValue(),
			((IOptions)options).rhythmicAttacks(),
			((IOptions)options).fishingRodLegacy());

		return optionInstance.toArray(new OptionInstance[0]);
	}
}
