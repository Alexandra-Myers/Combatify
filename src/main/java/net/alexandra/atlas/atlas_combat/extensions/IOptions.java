package net.alexandra.atlas.atlas_combat.extensions;

import net.alexandra.atlas.atlas_combat.config.ShieldIndicatorStatus;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public interface IOptions {
	Boolean autoAttack();
	Boolean shieldCrouch();
	Boolean lowShield();
	Boolean rhythmicAttacks();

    Boolean protIndicator();

    Boolean swordBlockStyle();

	Boolean fishingRodLegacy();

	ShieldIndicatorStatus shieldIndicator();

    Double attackIndicatorValue();

	static Component doubleValueLabel(Component optionText, double value) {
		return new TranslatableComponent("options.double_value", optionText, value);
	}
}
