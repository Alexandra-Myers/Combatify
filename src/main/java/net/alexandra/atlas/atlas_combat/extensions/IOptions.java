package net.alexandra.atlas.atlas_combat.extensions;

import net.alexandra.atlas.atlas_combat.config.ShieldIndicatorStatus;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;

public interface IOptions {
	OptionInstance<Boolean> autoAttack();
	OptionInstance<Boolean> shieldCrouch();
	OptionInstance<Boolean> lowShield();
	OptionInstance<Boolean> rhythmicAttacks();

    OptionInstance<Boolean> protIndicator();

    OptionInstance<ShieldIndicatorStatus> shieldIndicator();

    OptionInstance<Double> attackIndicatorValue();
	OptionInstance<Boolean> getReducedDebugInfo();
	static Component doubleValueLabel(Component optionText, double value) {
		return Component.translatable("options.double_value", optionText, value);
	}
}
