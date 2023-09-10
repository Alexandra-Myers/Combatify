package net.atlas.combatify.config;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.BlockingType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ItemConfig {
	public Map<Item, ConfigurableItemData> configuredItems = new HashMap<>();
	public Map<WeaponType, ConfigurableWeaponData> configuredWeapons = new HashMap<>();

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
			if (!itemsJsonObject.has("items"))
				itemsJsonObject.add("items", new JsonArray());
			JsonElement items = itemsJsonObject.get("items");
			if (!itemsJsonObject.has("weapon_types"))
				itemsJsonObject.add("weapon_types", new JsonArray());
			JsonElement weapons = itemsJsonObject.get("weapon_types");
			if (items instanceof JsonArray itemArray) {
				itemArray.asList().forEach(jsonElement -> {
					if (jsonElement instanceof JsonObject jsonObject) {
						if (jsonObject.get("name") instanceof JsonArray itemsWithConfig) {
							itemsWithConfig.asList().forEach(
								itemName -> {
									Item item = itemFromName(itemName.getAsString());
									parseItemConfig(item, jsonObject);
								}
							);
						} else {
							Item item = itemFromJson(jsonObject);
							parseItemConfig(item, jsonObject);
						}
					} else
						throw new IllegalStateException("Not a JSON Object: " + jsonElement + " this may be due to an incorrectly written config file.");
				});
			}
			if (weapons instanceof JsonArray typeArray) {
				typeArray.asList().forEach(jsonElement -> {
					if (jsonElement instanceof JsonObject jsonObject) {
						WeaponType type = typeFromJson(jsonObject);
						Double damageOffset = null;
						Double speed = null;
						Double reach = null;
						Double chargedReach = null;
						Boolean tierable = getBoolean(jsonObject, "tierable");
						if (!jsonObject.has("tierable"))
							throw new JsonSyntaxException("The JSON must contain the boolean 'tierable' if a weapon type is defined!");
						if (jsonObject.has("damage_offset"))
							damageOffset = getDouble(jsonObject, "damage_offset");
						if (jsonObject.has("speed"))
							speed = getDouble(jsonObject, "speed");
						if (jsonObject.has("reach"))
							reach = getDouble(jsonObject, "reach");
						if (jsonObject.has("charged_reach"))
							chargedReach = getDouble(jsonObject, "charged_reach");
						ConfigurableWeaponData configurableWeaponData = new ConfigurableWeaponData(damageOffset, speed, reach, chargedReach, tierable);
						configuredWeapons.put(type, configurableWeaponData);
					} else
						throw new IllegalStateException("Not a JSON Object: " + jsonElement + " this may be due to an incorrectly written config file.");
				});
			}
		} catch (IOException | IllegalStateException e) {
			e.printStackTrace();
		}
	}

	public String getString(JsonObject element, String name) {
		return element.get(name).getAsString();
	}

	public Integer getInt(JsonObject element, String name) {
		return element.get(name).getAsInt();
	}

	public Double getDouble(JsonObject element, String name) {
		return element.get(name).getAsDouble();
	}
	public Boolean getBoolean(JsonObject element, String name) {
		return element.get(name).getAsBoolean();
	}

	public static Item itemFromJson(JsonObject jsonObject) {
		String string = GsonHelper.getAsString(jsonObject, "name");
		return itemFromName(string);
	}
	public static Item itemFromName(String string) {
		Item item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.tryParse(string)).orElseThrow(() -> new JsonSyntaxException("Unknown item '" + string + "'"));
		if (item == Items.AIR) {
			throw new JsonSyntaxException("You can't configure an empty item!");
		} else {
			return item;
		}
	}

	public static WeaponType typeFromJson(JsonObject jsonObject) {
		String weapon_type = GsonHelper.getAsString(jsonObject, "name");
		weapon_type = weapon_type.toUpperCase(Locale.ROOT);
		return switch (weapon_type) {
			case "SWORD", "LONGSWORD", "AXE", "PICKAXE", "HOE", "SHOVEL", "KNIFE", "TRIDENT" -> WeaponType.fromID(weapon_type);
			default -> throw new JsonSyntaxException("The specified weapon type does not exist!");
		};
	}

	public void loadFromNetwork(FriendlyByteBuf buf) {
		configuredItems = buf.readMap(buf12 -> buf12.readById(BuiltInRegistries.ITEM), buf1 -> {
			Double damage = buf1.readDouble();
			Double speed = buf1.readDouble();
			Double reach = buf1.readDouble();
			Double chargedReach = buf1.readDouble();
			Integer stackSize = buf1.readInt();
			Integer cooldown = buf1.readInt();
			Boolean cooldownAfter = null;
			if(cooldown != -10)
				cooldownAfter = buf1.readBoolean();
			String weaponType = buf1.readUtf();
			WeaponType type = null;
			String blockingType = buf1.readUtf();
			BlockingType bType = Combatify.registeredTypes.get(ResourceLocation.tryParse(blockingType));
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
			if(cooldown == -10)
				cooldown = null;
			switch (weaponType) {
				case "SWORD", "LONGSWORD", "AXE", "PICKAXE", "HOE", "SHOVEL", "KNIFE", "TRIDENT" -> type = WeaponType.fromID(weaponType);
			}
			return new ConfigurableItemData(damage, speed, reach, chargedReach, stackSize, cooldown, cooldownAfter, type, bType);
		});
		configuredWeapons = buf.readMap(buf1 -> WeaponType.fromID(buf1.readUtf()), buf1 -> {
			Double damageOffset = buf1.readDouble();
			Double speed = buf1.readDouble();
			Double reach = buf1.readDouble();
			Double chargedReach = buf1.readDouble();
			Boolean tierable = buf1.readBoolean();
			if(damageOffset == -10)
				damageOffset = null;
			if(speed == -10)
				speed = null;
			if(reach == -10)
				reach = null;
			if(chargedReach == -10)
				chargedReach = null;
			return new ConfigurableWeaponData(damageOffset, speed, reach, chargedReach, tierable);
		});
	}

	public void saveToNetwork(FriendlyByteBuf buf) {
		buf.writeMap(configuredItems, (buf1, item) -> buf1.writeId(BuiltInRegistries.ITEM, item), (buf12, configurableItemData) -> {
			buf12.writeDouble(configurableItemData.damage == null ? -10 : configurableItemData.damage);
			buf12.writeDouble(configurableItemData.speed == null ? -10 : configurableItemData.speed);
			buf12.writeDouble(configurableItemData.reach == null ? -10 : configurableItemData.reach);
			buf12.writeDouble(configurableItemData.chargedReach == null ? -10 : configurableItemData.chargedReach);
			buf12.writeInt(configurableItemData.stackSize == null ? -10 : configurableItemData.stackSize);
			buf12.writeInt(configurableItemData.cooldown == null ? -10 : configurableItemData.cooldown);
			if(configurableItemData.cooldown != null)
				buf12.writeBoolean(configurableItemData.cooldownAfter);
			buf12.writeUtf(configurableItemData.type == null ? "empty" : configurableItemData.type.name());
			buf12.writeUtf(configurableItemData.blockingType == null ? "blank" : configurableItemData.blockingType.getName().toString());
		});
		buf.writeMap(configuredWeapons, (buf1, type) -> buf1.writeUtf(type.name()), (buf12, configurableWeaponData) -> {
			buf12.writeDouble(configurableWeaponData.damageOffset == null ? -10 : configurableWeaponData.damageOffset);
			buf12.writeDouble(configurableWeaponData.speed == null ? -10 : configurableWeaponData.speed);
			buf12.writeDouble(configurableWeaponData.reach == null ? -10 : configurableWeaponData.reach);
			buf12.writeDouble(configurableWeaponData.chargedReach == null ? -10 : configurableWeaponData.chargedReach);
			buf12.writeBoolean(configurableWeaponData.tierable);
		});
	}

	public void parseItemConfig(Item item, JsonObject jsonObject) {
		Double damage = null;
		Double speed = null;
		Double reach = null;
		Double chargedReach = null;
		Integer stack_size = null;
		Integer cooldown = null;
		Boolean cooldownAfterUse = null;
		WeaponType type = null;
		BlockingType blockingType = null;
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
		if (jsonObject.has("cooldown"))
			cooldown = getInt(jsonObject, "cooldown");
		if (jsonObject.has("weapon_type")) {
			String weapon_type = getString(jsonObject, "weapon_type");
			weapon_type = weapon_type.toUpperCase(Locale.ROOT);
			switch (weapon_type) {
				case "SWORD", "LONGSWORD", "AXE", "PICKAXE", "HOE", "SHOVEL", "KNIFE", "TRIDENT" ->
					type = WeaponType.fromID(weapon_type);
				default ->
					throw new JsonSyntaxException("The specified weapon type does not exist!");
			}
		}
		if (jsonObject.has("blocking_type")) {
			String blocking_type = getString(jsonObject, "blocking_type");
			blocking_type = blocking_type.toLowerCase(Locale.ROOT);
			ResourceLocation resourceLocation = ResourceLocation.tryParse(blocking_type);
			if (!Combatify.registeredTypes.containsKey(resourceLocation))
				throw new JsonSyntaxException("The specified blocking type does not exist!");
			blockingType = Combatify.registeredTypes.get(resourceLocation);
		}
		if (cooldown != null) {
			if (!jsonObject.has("cooldown_after"))
				throw new JsonSyntaxException("The JSON must contain the boolean 'cooldown_after' if a cooldown is defined!");
			cooldownAfterUse = getBoolean(jsonObject, "cooldown_after");
		}
		ConfigurableItemData configurableItemData = new ConfigurableItemData(damage, speed, reach, chargedReach, stack_size, cooldown, cooldownAfterUse, type, blockingType);
		configuredItems.put(item, configurableItemData);
	}
}
