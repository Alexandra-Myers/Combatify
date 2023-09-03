package net.atlas.combatify.config;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import net.atlas.combatify.util.UtilClass;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ItemConfig {
	public Map<Item, ConfigurableItemData> configuredItems = new HashMap<>();

	final Path configFolderPath;

	File itemsFile;

	JsonElement itemsJsonElement;

	JsonObject itemsJsonObject;

	public ItemConfig() {
		configFolderPath = FabricLoader.getInstance().getConfigDir().getFileName();

		load();
	}
	public void load() {
		itemsFile = new File(configFolderPath.toAbsolutePath() + "/combatify-items.json");
		if (!itemsFile.exists()) {
			try {
				itemsFile.createNewFile();
				InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("combatify-items.json");
				Files.write(itemsFile.toPath(), inputStream.readAllBytes());
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			itemsJsonElement = JsonParser.parseReader(new JsonReader(new FileReader(itemsFile)));

			itemsJsonObject = itemsJsonElement.getAsJsonObject();
			JsonElement items = itemsJsonObject.get("items");
			if (items instanceof JsonArray itemArray) {
				itemArray.asList().forEach(jsonElement -> {
					if (jsonElement instanceof JsonObject jsonObject) {
						Item item = itemFromJson(jsonObject);
						Double damage = null;
						Double speed = null;
						Double reach = null;
						Double chargedReach = null;
						Integer stack_size = null;
						if (jsonObject.has("damage"))
							damage = getDouble(jsonObject, "damage");
						if (jsonObject.has("speed"))
							speed = getDouble(jsonObject, "speed");
						if (jsonObject.has("reach"))
							reach = getDouble(jsonObject, "reach");
						if (jsonObject.has("charged_reach"))
							chargedReach = getDouble(jsonObject, "charged_reach");
						if (jsonObject.has("stack_size"))
							stack_size = getInt(jsonObject, "stack_size");
						ConfigurableItemData configurableItemData = new ConfigurableItemData(damage, speed, reach, chargedReach, stack_size);
						configuredItems.put(item, configurableItemData);

					} else
						throw new IllegalStateException("Not a JSON Object: " + jsonElement + " this may be due to an incorrectly written config file.");
				});
			}
		} catch (IOException | IllegalStateException e) {
			e.printStackTrace();
		}
	}

	public Integer getInt(JsonObject element, String name) {
		return element.get(name).getAsInt();
	}

	public Double getDouble(JsonObject element, String name) {
		return element.get(name).getAsDouble();
	}

	public static Item itemFromJson(JsonObject jsonObject) {
		String string = GsonHelper.getAsString(jsonObject, "name");
		Item item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.tryParse(string)).orElseThrow(() -> new JsonSyntaxException("Unknown item '" + string + "'"));
		if (item == Items.AIR) {
			throw new JsonSyntaxException("You can't configure an empty item!");
		} else {
			return item;
		}
	}

	public void loadFromNetwork(FriendlyByteBuf buf) {
		configuredItems = buf.readMap(buf12 -> {
			ItemStack stack = buf12.readItem();
			return stack.getItem();
		}, buf1 -> {
			Double damage = buf1.readDouble();
			Double speed = buf1.readDouble();
			Double reach = buf1.readDouble();
			Double chargedReach = buf1.readDouble();
			Integer stackSize = buf1.readInt();
			if(damage == -10)
				damage = null;
			if(speed == -10)
				speed = null;
			if(reach == -10)
				reach = null;
			if(chargedReach == -10)
				chargedReach = null;
			if(stackSize == -10)
				stackSize = null;
			return new ConfigurableItemData(damage, speed, reach, chargedReach, stackSize);
		});
	}

	public void saveToBuf(FriendlyByteBuf buf) {
		buf.writeMap(configuredItems, (buf1, item) -> buf1.writeItem(new ItemStack(item)), (buf12, configurableItemData) -> {
			buf12.writeDouble(configurableItemData.damage == null ? -10 : configurableItemData.damage);
			buf12.writeDouble(configurableItemData.speed == null ? -10 : configurableItemData.speed);
			buf12.writeDouble(configurableItemData.reach == null ? -10 : configurableItemData.reach);
			buf12.writeDouble(configurableItemData.chargedReach == null ? -10 : configurableItemData.chargedReach);
			buf12.writeInt(configurableItemData.stackSize == null ? -10 : configurableItemData.stackSize);
		});
	}
}
