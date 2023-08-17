package net.atlas.combatify.extensions;

import net.atlas.combatify.config.ShieldIndicatorStatus;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;

public interface IOptions {
	OptionInstance<Boolean> autoAttack();
	OptionInstance<Boolean> shieldCrouch();
	OptionInstance<Boolean> rhythmicAttacks();

    OptionInstance<Boolean> swordBlockStyle();

	OptionInstance<Boolean> protIndicator();

	OptionInstance<Boolean> fishingRodLegacy();

	OptionInstance<ShieldIndicatorStatus> shieldIndicator();

    OptionInstance<Double> attackIndicatorValue();

	static Component doubleValueLabel(Component optionText, double value) {
		return Component.translatable("options.double_value", optionText, value);
	}
}
