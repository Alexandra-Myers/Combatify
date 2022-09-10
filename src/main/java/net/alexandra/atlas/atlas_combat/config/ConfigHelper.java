package net.alexandra.atlas.atlas_combat.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.midnightdust.lib.config.MidnightConfig;
import org.quiltmc.loader.api.QuiltLoader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class ConfigHelper extends MidnightConfig {
	@Entry public static boolean toolsAreWeapons = false;
	@Entry public static boolean bedrockBlockReach = false;
	@Entry public static boolean refinedCoyoteTime = false;
	@Entry public static boolean blockReach = true;
	@Entry(min=10,max=1000) public static int maxWaitForPacketResponse = 20;
	@Entry(min=1,max=1000) public static int potionUseDuration = 20;
	@Entry(min=1,max=1000) public static int honeyBottleUseDuration = 20;
	@Entry(min=1,max=1000) public static int milkBucketUseDuration = 20;
	@Entry(min=1,max=1000) public static int instantHealthBonus = 6;
	@Entry(min=1,max=1000) public static int eggItemCooldown = 4;
	@Entry(min=1,max=1000) public static int snowballItemCooldown = 4;
	@Entry(min=0.1,max=40) public static float snowballDamage = 0.0F;
	@Entry(min=0.1,max=4) public static float bowUncertainty = 0.25F;

	public Path configFolderPath;

	public File generalFile;

	public JsonElement generalJsonElement;

	public JsonObject generalJsonObject;

	public File itemsFile;

	public JsonElement itemsJsonElement;

	public JsonObject itemsJsonObject;

	public ConfigHelper() {
		configFolderPath = QuiltLoader.getConfigDir().getFileName();

		itemsFile = new File(configFolderPath.toAbsolutePath() + "/atlas-combat-items.json");
		if(!itemsFile.exists()) {
			try{
				itemsFile.createNewFile();
				InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("atlas-combat-items.json");
				Files.write(itemsFile.toPath(),inputStream.readAllBytes());
				inputStream.close();
			}catch (IOException e) {
				e.printStackTrace();
			}
		}

		try{
			itemsJsonElement = JsonParser.parseReader(new FileReader(itemsFile));

			itemsJsonObject = itemsJsonElement.getAsJsonObject();
			if(!Objects.equals(getString(itemsJsonObject, "version"), "1.0.3")) {
				try{
					itemsFile.createNewFile();
					InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("atlas-combat-items.json");
					Files.write(itemsFile.toPath(),inputStream.readAllBytes());
					inputStream.close();
				}catch (IOException e) {
					e.printStackTrace();
				}
				try {
					itemsJsonElement = JsonParser.parseReader(new FileReader(itemsFile));
					itemsJsonObject = itemsJsonElement.getAsJsonObject();
				}catch (IOException e) {
					e.printStackTrace();
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getString(JsonObject element, String name) {
		return element.get(name).getAsString();
	}

	public Boolean getBoolean(JsonObject element, String name) {
		return element.get(name).getAsBoolean();
	}

	public Integer getInt(JsonObject element, String name) {
		return element.get(name).getAsInt();
	}

	public Double getDouble(JsonObject element, String name) {
		return element.get(name).getAsDouble();
	}

	public Float getFloat(JsonObject element, String name) {
		return element.get(name).getAsFloat();
	}
}
