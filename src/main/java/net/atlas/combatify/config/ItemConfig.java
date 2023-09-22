package net.atlas.combatify.config;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.BlockingType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
			if (!itemsJsonObject.has("blocking_types"))
				itemsJsonObject.add("blocking_types", new JsonArray());
			JsonElement defenders = itemsJsonObject.get("blocking_types");
			if (defenders instanceof JsonArray typeArray) {
				typeArray.asList().forEach(jsonElement -> {
					if (jsonElement instanceof JsonObject jsonObject) {
						parseBlockingType(jsonObject);
					} else
						throw new ReportedException(CrashReport.forThrowable(new IllegalStateException("Not a JSON Object: " + jsonElement + " this may be due to an incorrectly written config file."), "Configuring Blocking Types"));
				});
			}
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
						throw new ReportedException(CrashReport.forThrowable(new IllegalStateException("Not a JSON Object: " + jsonElement + " this may be due to an incorrectly written config file."), "Configuring Items"));
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
						BlockingType blockingType = null;
						Boolean tierable = getBoolean(jsonObject, "tierable");
						if (!jsonObject.has("tierable"))
							throw new ReportedException(CrashReport.forThrowable(new JsonSyntaxException("The JSON must contain the boolean `tierable` if a weapon type is defined!"), "Configuring Weapon Types"));
						if (jsonObject.has("damage_offset"))
							damageOffset = getDouble(jsonObject, "damage_offset");
						if (jsonObject.has("speed"))
							speed = getDouble(jsonObject, "speed");
						if (jsonObject.has("reach"))
							reach = getDouble(jsonObject, "reach");
						if (jsonObject.has("charged_reach"))
							chargedReach = getDouble(jsonObject, "charged_reach");
						if (jsonObject.has("blocking_type")) {
							String blocking_type = getString(jsonObject, "blocking_type");
							blocking_type = blocking_type.toLowerCase(Locale.ROOT);
							if (!Combatify.registeredTypes.containsKey(blocking_type)) {
								CrashReport report = CrashReport.forThrowable(new JsonSyntaxException("The specified blocking type does not exist!"), "Applying Item Blocking Type");
								CrashReportCategory crashReportCategory = report.addCategory("Weapon Type being parsed");
								crashReportCategory.setDetail("Type name", blocking_type);
								crashReportCategory.setDetail("Json Object", jsonObject);
								throw new ReportedException(report);
							}
							blockingType = Combatify.registeredTypes.get(blocking_type);
						}
						ConfigurableWeaponData configurableWeaponData = new ConfigurableWeaponData(damageOffset, speed, reach, chargedReach, tierable, blockingType);
						configuredWeapons.put(type, configurableWeaponData);
					} else
						throw new ReportedException(CrashReport.forThrowable(new IllegalStateException("Not a JSON Object: " + jsonElement + " this may be due to an incorrectly written config file."), "Configuring Weapon Types"));
				});
			}
		} catch (IOException | IllegalStateException e) {
			e.printStackTrace();
		}
	}

	public static String getString(JsonObject element, String name) {
		return element.get(name).getAsString();
	}

	public static Integer getInt(JsonObject element, String name) {
		return element.get(name).getAsInt();
	}

	public static Double getDouble(JsonObject element, String name) {
		return element.get(name).getAsDouble();
	}
	public static Boolean getBoolean(JsonObject element, String name) {
		return element.get(name).getAsBoolean();
	}

	public static Item itemFromJson(JsonObject jsonObject) {
		String string = GsonHelper.getAsString(jsonObject, "name");
		return itemFromName(string);
	}

	public static Item itemFromName(String string) {
		Item item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.tryParse(string)).orElse(null);
		if (item == Items.AIR) {
			throw new ReportedException(CrashReport.forThrowable(new JsonSyntaxException("You can't configure an empty item!"), "Configuring Items"));
		} else {
			return item;
		}
	}

	public static WeaponType typeFromJson(JsonObject jsonObject) {
		String weapon_type = GsonHelper.getAsString(jsonObject, "name");
		weapon_type = weapon_type.toUpperCase(Locale.ROOT);
		return switch (weapon_type) {
			case "SWORD", "LONGSWORD", "AXE", "PICKAXE", "HOE", "SHOVEL", "KNIFE", "TRIDENT" -> WeaponType.fromID(weapon_type);
			default -> {
				CrashReport report = CrashReport.forThrowable(new JsonSyntaxException("The specified weapon type does not exist!"), "Getting Weapon Type");
				CrashReportCategory crashReportCategory = report.addCategory("Weapon Type being parsed");
				crashReportCategory.setDetail("Type name", weapon_type);
				crashReportCategory.setDetail("Json Object", jsonObject);
				throw new ReportedException(report);
			}
		};
	}

	public static void parseBlockingType(JsonObject jsonObject) {
		String blocking_type = GsonHelper.getAsString(jsonObject, "name");
		blocking_type = blocking_type.toLowerCase(Locale.ROOT);
		if (blocking_type.equals("empty") || blocking_type.equals("blank"))
			return;
		if (!Combatify.registeredTypes.containsKey(blocking_type) || jsonObject.has("class")) {
			if (jsonObject.has("class") && jsonObject.get("class") instanceof JsonPrimitive jsonPrimitive && jsonPrimitive.isString()) {
				try {
					Class<?> clazz = BlockingType.class.getClassLoader().loadClass(jsonPrimitive.getAsString());
					Constructor<?> constructor = clazz.getConstructor(String.class);
					Object object = constructor.newInstance(blocking_type);
					if (object instanceof BlockingType blockingType) {
						if (jsonObject.has("require_full_charge"))
							blockingType.setRequireFullCharge(getBoolean(jsonObject, "require_full_charge"));
						if (jsonObject.has("is_tool"))
							blockingType.setToolBlocker(getBoolean(jsonObject, "is_tool"));
						if (jsonObject.has("is_percentage"))
							blockingType.setPercentage(getBoolean(jsonObject, "is_percentage"));
						if (jsonObject.has("can_block_hit"))
							blockingType.setBlockHit(getBoolean(jsonObject, "can_block_hit"));
						if (jsonObject.has("can_crouch_block"))
							blockingType.setCrouchable(getBoolean(jsonObject, "can_crouch_block"));
						if (jsonObject.has("can_be_disabled"))
							blockingType.setDisablement(getBoolean(jsonObject, "can_be_disabled"));
						if (jsonObject.has("default_kb_mechanics"))
							blockingType.setKbMechanics(getBoolean(jsonObject, "default_kb_mechanics"));
						if (jsonObject.has("requires_sword_blocking"))
							blockingType.setSwordBlocking(getBoolean(jsonObject, "requires_sword_blocking"));
						Combatify.registerBlockingType(blockingType);
						return;
					} else {
						CrashReport report = CrashReport.forThrowable(new JsonSyntaxException("The specified class is not an instance of BlockingType!"), "Creating Blocking Type");
						CrashReportCategory crashReportCategory = report.addCategory("Blocking Type being parsed");
						crashReportCategory.setDetail("Class", clazz.getName());
						crashReportCategory.setDetail("Type Name", blocking_type);
						crashReportCategory.setDetail("Json Object", jsonObject);
						throw new ReportedException(report);
					}
				} catch (ClassNotFoundException | NoSuchMethodException e) {
					CrashReport report = CrashReport.forThrowable(new JsonSyntaxException("The specified class does not exist, or otherwise lacks a constructor with a String parameter", e), "Creating Blocking Type");
					CrashReportCategory crashReportCategory = report.addCategory("Blocking Type being parsed");
					crashReportCategory.setDetail("Class", getString(jsonObject, "class"));
					crashReportCategory.setDetail("Type Name", blocking_type);
					crashReportCategory.setDetail("Json Object", jsonObject);
					throw new ReportedException(report);
				} catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
					CrashReport report = CrashReport.forThrowable(new RuntimeException(e), "Creating Blocking Type");
					CrashReportCategory crashReportCategory = report.addCategory("Blocking Type being parsed");
					crashReportCategory.setDetail("Class", getString(jsonObject, "class"));
					crashReportCategory.setDetail("Type Name", blocking_type);
					crashReportCategory.setDetail("Json Object", jsonObject);
					throw new ReportedException(report);
				}
			} else {
				CrashReport report = CrashReport.forThrowable(new JsonSyntaxException("You cannot create a blocking type without a class!"), "Creating Blocking Type");
				CrashReportCategory crashReportCategory = report.addCategory("Blocking Type being parsed");
				crashReportCategory.setDetail("Type Name", blocking_type);
				crashReportCategory.setDetail("Json Object", jsonObject);
				throw new ReportedException(report);
			}
		}
		BlockingType blockingType = Combatify.registeredTypes.get(blocking_type);
		if (jsonObject.has("require_full_charge"))
			blockingType.setRequireFullCharge(getBoolean(jsonObject, "require_full_charge"));
		if (jsonObject.has("is_tool"))
			blockingType.setToolBlocker(getBoolean(jsonObject, "is_tool"));
		if (jsonObject.has("is_percentage"))
			blockingType.setPercentage(getBoolean(jsonObject, "is_percentage"));
		if (jsonObject.has("can_block_hit"))
			blockingType.setBlockHit(getBoolean(jsonObject, "can_block_hit"));
		if (jsonObject.has("can_crouch_block"))
			blockingType.setCrouchable(getBoolean(jsonObject, "can_crouch_block"));
		if (jsonObject.has("can_be_disabled"))
			blockingType.setDisablement(getBoolean(jsonObject, "can_be_disabled"));
		if (jsonObject.has("default_kb_mechanics"))
			blockingType.setKbMechanics(getBoolean(jsonObject, "default_kb_mechanics"));
		if (jsonObject.has("requires_sword_blocking"))
			blockingType.setSwordBlocking(getBoolean(jsonObject, "requires_sword_blocking"));
	}

	public ItemConfig loadFromNetwork(FriendlyByteBuf buf) {
		Combatify.registeredTypes = buf.readMap(FriendlyByteBuf::readUtf, buf1 -> {
			try {
				Class<?> clazz = BlockingType.class.getClassLoader().loadClass(buf1.readUtf());
				Constructor<?> constructor = clazz.getConstructor(String.class);
				String name = buf1.readUtf();
				Object object = constructor.newInstance(name);
				if (object instanceof BlockingType blockingType) {
					blockingType.setDisablement(buf1.readBoolean());
					blockingType.setBlockHit(buf1.readBoolean());
					blockingType.setCrouchable(buf1.readBoolean());
					blockingType.setKbMechanics(buf1.readBoolean());
					blockingType.setPercentage(buf1.readBoolean());
					blockingType.setToolBlocker(buf1.readBoolean());
					blockingType.setRequireFullCharge(buf1.readBoolean());
					blockingType.setSwordBlocking(buf1.readBoolean());
					Combatify.registerBlockingType(blockingType);
					return blockingType;
				} else {
					CrashReport report = CrashReport.forThrowable(new IllegalStateException("The specified class is not an instance of BlockingType!"), "Syncing Blocking Types");
					CrashReportCategory crashReportCategory = report.addCategory("Blocking Type being synced");
					crashReportCategory.setDetail("Class", clazz.getName());
					crashReportCategory.setDetail("Type Name", name);
					throw new ReportedException(report);
				}
			} catch (InvocationTargetException | InstantiationException | IllegalAccessException |
					 ClassNotFoundException | NoSuchMethodException e) {
				throw new ReportedException(CrashReport.forThrowable(new RuntimeException(e), "Syncing Blocking Types"));
			}
		});
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
			BlockingType bType = Combatify.registeredTypes.get(blockingType);
			Double blockStrength = buf1.readDouble();
			Double blockKbRes = buf1.readDouble();
			Integer enchantlevel = buf1.readInt();
			if(damage == -100)
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
			if(blockStrength == -10)
				blockStrength = null;
			if(blockKbRes == -10)
				blockKbRes = null;
			switch (weaponType) {
				case "SWORD", "LONGSWORD", "AXE", "PICKAXE", "HOE", "SHOVEL", "KNIFE", "TRIDENT" -> type = WeaponType.fromID(weaponType);
			}
			return new ConfigurableItemData(damage, speed, reach, chargedReach, stackSize, cooldown, cooldownAfter, type, bType, blockStrength, blockKbRes, enchantlevel);
		});
		configuredWeapons = buf.readMap(buf1 -> WeaponType.fromID(buf1.readUtf()), buf1 -> {
			Double damageOffset = buf1.readDouble();
			Double speed = buf1.readDouble();
			Double reach = buf1.readDouble();
			Double chargedReach = buf1.readDouble();
			Boolean tierable = buf1.readBoolean();
			String blockingType = buf1.readUtf();
			BlockingType bType = Combatify.registeredTypes.get(blockingType);
			if(damageOffset == -10)
				damageOffset = null;
			if(speed == -10)
				speed = null;
			if(reach == -10)
				reach = null;
			if(chargedReach == -10)
				chargedReach = null;
			return new ConfigurableWeaponData(damageOffset, speed, reach, chargedReach, tierable, bType);
		});
		return this;
	}

	public void saveToNetwork(FriendlyByteBuf buf) {
		buf.writeMap(Combatify.registeredTypes, FriendlyByteBuf::writeUtf, (buf1, blockingType) -> {
			buf1.writeUtf(blockingType.getClass().getName());
			buf1.writeUtf(blockingType.getName());
			buf1.writeBoolean(blockingType.canBeDisabled());
			buf1.writeBoolean(blockingType.canBlockHit());
			buf1.writeBoolean(blockingType.canCrouchBlock());
			buf1.writeBoolean(blockingType.defaultKbMechanics());
			buf1.writeBoolean(blockingType.isPercentage());
			buf1.writeBoolean(blockingType.isToolBlocker());
			buf1.writeBoolean(blockingType.requireFullCharge());
			buf1.writeBoolean(blockingType.requiresSwordBlocking());
		});
		buf.writeMap(configuredItems, (buf1, item) -> buf1.writeId(BuiltInRegistries.ITEM, item), (buf12, configurableItemData) -> {
			buf12.writeDouble(configurableItemData.damage == null ? -100 : configurableItemData.damage);
			buf12.writeDouble(configurableItemData.speed == null ? -10 : configurableItemData.speed);
			buf12.writeDouble(configurableItemData.reach == null ? -10 : configurableItemData.reach);
			buf12.writeDouble(configurableItemData.chargedReach == null ? -10 : configurableItemData.chargedReach);
			buf12.writeInt(configurableItemData.stackSize == null ? -10 : configurableItemData.stackSize);
			buf12.writeInt(configurableItemData.cooldown == null ? -10 : configurableItemData.cooldown);
			if(configurableItemData.cooldown != null)
				buf12.writeBoolean(configurableItemData.cooldownAfter);
			buf12.writeUtf(configurableItemData.type == null ? "empty" : configurableItemData.type.name());
			buf12.writeUtf(configurableItemData.blockingType == null ? "blank" : configurableItemData.blockingType.getName());
			buf12.writeDouble(configurableItemData.blockStrength == null ? -10 : configurableItemData.blockStrength);
			buf12.writeDouble(configurableItemData.blockKbRes == null ? -10 : configurableItemData.blockKbRes);
			buf12.writeInt(configurableItemData.enchantability == null ? -10 : configurableItemData.enchantability);
		});
		buf.writeMap(configuredWeapons, (buf1, type) -> buf1.writeUtf(type.name()), (buf12, configurableWeaponData) -> {
			buf12.writeDouble(configurableWeaponData.damageOffset == null ? -10 : configurableWeaponData.damageOffset);
			buf12.writeDouble(configurableWeaponData.speed == null ? -10 : configurableWeaponData.speed);
			buf12.writeDouble(configurableWeaponData.reach == null ? -10 : configurableWeaponData.reach);
			buf12.writeDouble(configurableWeaponData.chargedReach == null ? -10 : configurableWeaponData.chargedReach);
			buf12.writeBoolean(configurableWeaponData.tierable);
			buf12.writeUtf(configurableWeaponData.blockingType == null ? "blank" : configurableWeaponData.blockingType.getName());
		});
	}

	public void parseItemConfig(Item item, JsonObject jsonObject) {
		if(item == null)
			return;
		Double damage = null;
		Double speed = null;
		Double reach = null;
		Double chargedReach = null;
		Integer stack_size = null;
		Integer cooldown = null;
		Boolean cooldownAfterUse = null;
		WeaponType type = null;
		BlockingType blockingType = null;
		Double blockStrength = null;
		Double blockKbRes = null;
		Integer enchantment_level = null;
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
				default -> {
					CrashReport report = CrashReport.forThrowable(new JsonSyntaxException("The specified weapon type does not exist!"), "Applying Item Weapon Type");
					CrashReportCategory crashReportCategory = report.addCategory("Weapon Type being parsed");
					crashReportCategory.setDetail("Type name", weapon_type);
					crashReportCategory.setDetail("Json Object", jsonObject);
					throw new ReportedException(report);
				}
			}
		}
		if (jsonObject.has("blocking_type")) {
			String blocking_type = getString(jsonObject, "blocking_type");
			blocking_type = blocking_type.toLowerCase(Locale.ROOT);
			if (!Combatify.registeredTypes.containsKey(blocking_type)) {
				CrashReport report = CrashReport.forThrowable(new JsonSyntaxException("The specified blocking type does not exist!"), "Applying Item Blocking Type");
				CrashReportCategory crashReportCategory = report.addCategory("Blocking Type being parsed");
				crashReportCategory.setDetail("Type name", blocking_type);
				crashReportCategory.setDetail("Json Object", jsonObject);
				throw new ReportedException(report);
			}
			blockingType = Combatify.registeredTypes.get(blocking_type);
		}
		if (cooldown != null) {
			if (!jsonObject.has("cooldown_after"))
				throw new ReportedException(CrashReport.forThrowable(new JsonSyntaxException("The JSON must contain the boolean 'cooldown_after' if a cooldown is defined!"), "Applying Item Cooldown"));
			cooldownAfterUse = getBoolean(jsonObject, "cooldown_after");
		}
		if (jsonObject.has("damage_protection"))
			blockStrength = getDouble(jsonObject, "damage_protection");
		if (jsonObject.has("block_knockback_resistance"))
			blockKbRes = getDouble(jsonObject, "block_knockback_resistance");
		if (jsonObject.has("enchantment_level"))
			enchantment_level = getInt(jsonObject, "enchantment_level");
		ConfigurableItemData configurableItemData = new ConfigurableItemData(damage, speed, reach, chargedReach, stack_size, cooldown, cooldownAfterUse, type, blockingType, blockStrength, blockKbRes, enchantment_level);
		configuredItems.put(item, configurableItemData);
	}
}
