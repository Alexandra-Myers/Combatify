package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.atlascore.util.ArrayListExtensions;
import net.atlas.combatify.CombatifyClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;

@SuppressWarnings("unused")
@Mixin(VideoSettingsScreen.class)
public class VideoSettingsMixin {
	@ModifyReturnValue(method = "options", at = @At("RETURN"))
	private static OptionInstance<?>[] injectOptions(OptionInstance<?>[] original, @Local(ordinal = 0, argsOnly = true) Options options) {
		var optionInstance = new ArrayListExtensions<>(Arrays.stream(original).toList());
		int i = optionInstance.indexOf(Minecraft.getInstance().options.attackIndicator());

		optionInstance.add(i + 1, CombatifyClient.shieldIndicator);
		optionInstance.addAll(CombatifyClient.attackIndicatorMinValue,
			CombatifyClient.attackIndicatorMaxValue,
			CombatifyClient.rhythmicAttacks);

		return optionInstance.toArray(new OptionInstance[0]);
	}
}
