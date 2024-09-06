package net.atlas.combatify;

import net.atlas.atlascore.util.PrefixLogger;
import net.atlas.combatify.config.cookey.ModConfig;
import net.atlas.combatify.keybind.Keybinds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CookeyMod implements ClientModInitializer {
	private static final PrefixLogger COOKEY_LOGGER = new PrefixLogger(LogManager.getLogger("CookeyMod"));
	private static final ModConfig config = new ModConfig(FabricLoader.getInstance().getConfigDir().resolve("cookeymod").resolve("config.toml"));
	private static Keybinds keybinds;

	/**
	 * Runs the mod initializer on the client environment.
	 */
	@Override
	public void onInitializeClient() {
		COOKEY_LOGGER.info("Loading CookeyMod...");

		keybinds = new Keybinds();
	}

	public static Logger getCookeyModLogger() {
		return COOKEY_LOGGER.unwrap();
	}

	public static ModConfig getConfig() {
		return config;
	}

	public static Keybinds getKeybinds() {
		return keybinds;
	}
}
