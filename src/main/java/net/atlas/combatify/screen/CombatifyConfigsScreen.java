package net.atlas.combatify.screen;

import java.util.ArrayList;
import java.util.List;
import net.atlas.atlascore.client.ScreenBuilder;
import net.atlas.combatify.Combatify;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public class CombatifyConfigsScreen extends OptionsSubScreen {
	public CombatifyConfigsScreen(Screen screen, Options options, Component component) {
		super(screen, options, component);
	}

	protected void addOptions() {
		List<AbstractWidget> configButtons = new ArrayList<>();
		configButtons.add(Button.builder(Component.translatable("text.config." + Combatify.CONFIG.name.getPath() + ".title"), (button) -> this.minecraft.setScreen(ScreenBuilder.buildAtlasConfig(this, Combatify.CONFIG))).build());
		configButtons.add(Button.builder(Component.translatable("options.cookeymod.button"), (button) -> this.minecraft.setScreen(net.atlas.combatify.screen.ScreenBuilder.buildConfig(this.minecraft.screen))).build());
		this.list.addSmall(configButtons);
	}

	protected void repositionElements() {
		super.repositionElements();
		if (this.list != null) {
			this.list.updateSize(this.width, this.layout);
		}

	}
}

