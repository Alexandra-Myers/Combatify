package net.atlas.combatify.impl;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.atlas.combatify.screen.CombatifyConfigsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ModMenuApiImpl implements ModMenuApi {
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return (screen) -> new CombatifyConfigsScreen(screen, Minecraft.getInstance().options, Component.translatable("text.config.combatify-general.title"));
	}
}
