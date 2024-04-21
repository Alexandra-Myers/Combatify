package net.atlas.combatify.config;

import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.atlas.combatify.screen.ScreenBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AtlasConfigScreen extends OptionsSubScreen {
	@Nullable
	private OptionsList list;
	public AtlasConfigScreen(Screen screen, Options options, Component component) {
		super(screen, options, component);
	}
	protected void init() {
		this.list = this.addRenderableWidget(new OptionsList(this.minecraft, this.width, this.height, this));

		List<AbstractWidget> configButtons = new ArrayList<>();
		AtlasConfig.configs.forEach((resourceLocation, config) -> {
			if (config.hasScreen()) configButtons.add(Button.builder(Component.translatable("text.config." + config.name.getPath() + ".title"), button -> this.minecraft.setScreen(ScreenBuilder.buildAtlasConfig(this, config))).build());
		});
		list.addSmall(configButtons);
		super.init();
	}

	protected void repositionElements() {
		super.repositionElements();
		if (this.list != null) {
			this.list.updateSize(this.width, this.layout);
		}
	}
}
