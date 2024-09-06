package net.atlas.combatify.screen;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.atlas.combatify.CookeyMod;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.atlas.combatify.config.cookey.ModConfig;
import net.atlas.combatify.config.cookey.category.Category;

import java.io.IOException;

public final class ScreenBuilder {
    private ScreenBuilder() {
    }

    public static Screen buildConfig(Screen prevScreen) {
        ModConfig config = CookeyMod.getConfig();

        ConfigBuilder builder = ConfigBuilder.create()
                .setTitle(Component.translatable(ModConfig.TRANSLATION_KEY))
                .transparentBackground()
                .setSavingRunnable(() -> {
                    try {
                        config.saveConfig();
                    } catch (IOException e) {
                        CookeyMod.getCookeyModLogger().error("Failed to save CookeyMod config file!");
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
}
