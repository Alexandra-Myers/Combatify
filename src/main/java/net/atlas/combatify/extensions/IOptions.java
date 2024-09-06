package net.atlas.combatify.extensions;

import net.minecraft.network.chat.Component;

public interface IOptions {
	static Component doubleValueLabel(Component optionText, double value) {
		return Component.translatable("options.double_value", optionText, value);
	}
}
