package net.atlas.combatify.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.util.ArrayListExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;

@SuppressWarnings("unused")
@Mixin(VideoSettingsScreen.class)
public class VideoSettingsMixin {
	@ModifyExpressionValue(
		method = "init",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screens/VideoSettingsScreen;options(Lnet/minecraft/client/Options;)[Lnet/minecraft/client/OptionInstance;"
		)
	)
	private OptionInstance<?>[] addOptions(OptionInstance<?>[] optionInstances) {
		var optionInstance = new ArrayListExtensions<>(Arrays.stream(optionInstances).toList());
		int i = optionInstance.indexOf(Minecraft.getInstance().options.attackIndicator());

		optionInstance.add(i + 1, CombatifyClient.shieldIndicator);
		optionInstance.addAll(CombatifyClient.attackIndicatorMinValue,
			CombatifyClient.attackIndicatorMaxValue,
			CombatifyClient.rhythmicAttacks);

		return optionInstance.toArray(new OptionInstance[0]);
	}
}
