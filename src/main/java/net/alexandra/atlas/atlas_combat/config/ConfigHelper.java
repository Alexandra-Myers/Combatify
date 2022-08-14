package net.alexandra.atlas.atlas_combat.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.quiltmc.loader.api.QuiltLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigHelper {

	public Path configFolderPath;

	public File generalFile;
	public File itemsFile;

	public JsonElement itemsJsonElement;
	public JsonElement generalJsonElement;

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
			itemsJsonElement = JsonParser.parseReader(new FileReader(itemsFile));
			generalJsonElement = JsonParser.parseReader(new FileReader(generalFile));
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

}
