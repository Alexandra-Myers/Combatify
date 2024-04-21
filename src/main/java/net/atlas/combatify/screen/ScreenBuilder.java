package net.atlas.combatify.screen;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.config.AtlasConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.atlas.combatify.config.cookey.ModConfig;
import net.atlas.combatify.config.cookey.category.Category;

import java.io.IOException;

public final class ScreenBuilder {
    private ScreenBuilder() {
    }

    public static Screen buildConfig(Screen prevScreen) {
        ModConfig config = CombatifyClient.getInstance().getConfig();

        ConfigBuilder builder = ConfigBuilder.create()
                .setTitle(Component.translatable(ModConfig.TRANSLATION_KEY))
                .transparentBackground()
                .setSavingRunnable(() -> {
                    try {
                        config.saveConfig();
                    } catch (IOException e) {
                        CombatifyClient.getInstance().getCookeyModLogger().error("Failed to save CookeyMod config file!");
                        e.printStackTrace();
                    }
                });
        if (prevScreen != null) builder.setParentScreen(prevScreen);

        for (String id : config.getCategories().keySet()) {
            Category category = config.getCategory(id);
            ConfigCategory configCategory = builder.getOrCreateCategory(Component.translatable(category.getTranslationKey()));

            for (AbstractConfigListEntry<?> entry : category.getConfigEntries()) {
                configCategory.addEntry(entry);
            }
        }

        return builder.build();
    }

	public static Screen buildAtlasConfig(Screen prevScreen, AtlasConfig config) {
		Screen special = config.createScreen(prevScreen);
		if (special != null)
			return special;
		ConfigBuilder builder = ConfigBuilder.create()
			.setTitle(Component.translatable("text.config." + config.name.getPath() + ".title"))
			.transparentBackground()
			.setSavingRunnable(() -> {
				try {
					config.saveConfig();
				} catch (IOException e) {
					Combatify.LOGGER.error("Failed to save " + config.name + " config file!");
					e.printStackTrace();
				}
			});
		if (prevScreen != null) builder.setParentScreen(prevScreen);

		for (AtlasConfig.Category category : config.categories) {
			ConfigCategory configCategory = builder.getOrCreateCategory(Component.translatable(category.translationKey()));

			for (AbstractConfigListEntry<?> entry : category.membersAsCloth()) {
				configCategory.addEntry(entry);
			}
		}

		return builder.build();
	}
}
