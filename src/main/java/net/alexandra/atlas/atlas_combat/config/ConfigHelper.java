package net.alexandra.atlas.atlas_combat.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.quiltmc.loader.api.QuiltLoader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class ConfigHelper {

	public Path configFolderPath;

	public File generalFile;

	public JsonElement generalJsonElement;

	public JsonObject generalJsonObject;

	public File itemsFile;

	public JsonElement itemsJsonElement;

	public JsonObject itemsJsonObject;

	public ConfigHelper() {
		configFolderPath = QuiltLoader.getConfigDir().getFileName();

		generalFile = new File(configFolderPath.toAbsolutePath() + "/atlas-combat-general.json");
		itemsFile = new File(configFolderPath.toAbsolutePath() + "/atlas-combat-items.json");
		if(!generalFile.exists()) {
			try{
				generalFile.createNewFile();
				InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("atlas-combat.json");
				Files.write(generalFile.toPath(),inputStream.readAllBytes());
				inputStream.close();
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
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
			generalJsonElement = JsonParser.parseReader(new FileReader(generalFile));
			itemsJsonElement = JsonParser.parseReader(new FileReader(itemsFile));

			generalJsonObject = generalJsonElement.getAsJsonObject();
			itemsJsonObject = itemsJsonElement.getAsJsonObject();
			if(!Objects.equals(getString(generalJsonObject, "version"), "1.0.3")) {
				try{
					generalFile.createNewFile();
					InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("atlas-combat.json");
					Files.write(generalFile.toPath(),inputStream.readAllBytes());
					inputStream.close();
				}catch (IOException e) {
					e.printStackTrace();
				}
				try {
					generalJsonElement = JsonParser.parseReader(new FileReader(generalFile));
					generalJsonObject = generalJsonElement.getAsJsonObject();
				}catch (IOException e) {
					e.printStackTrace();
				}
			}
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
