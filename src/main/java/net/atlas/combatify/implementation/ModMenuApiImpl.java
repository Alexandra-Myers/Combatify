package net.atlas.combatify.implementation;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.AtlasConfig;
import net.atlas.combatify.screen.ScreenBuilder;

import java.util.HashMap;
import java.util.Map;

public class ModMenuApiImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> ScreenBuilder.buildAtlasConfig(screen, Combatify.CONFIG);
    }

	@Override
	public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
		Map<String, ConfigScreenFactory<?>> configs = new HashMap<>();
		AtlasConfig.menus.forEach((modID, config) -> configs.put(modID, screen -> ScreenBuilder.buildAtlasConfig(screen, config)));
		return configs;
	}
}
