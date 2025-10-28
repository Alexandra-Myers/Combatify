package net.atlas.combatify.config;

import com.google.gson.*;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.networking.NetworkingHandler;
import net.atlas.combatify.util.BlockingType;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static net.atlas.combatify.Combatify.*;
import static net.atlas.combatify.Combatify.ITEMS;

public class ItemConfig extends AtlasConfig {
	public Map<Item, ConfigurableItemData> configuredItems;
	public Map<WeaponType, ConfigurableWeaponData> configuredWeapons;

	public ItemConfig() {
		super(id("combatify-items"));
	}

	@Override
	protected void loadExtra(JsonObject object) {
		if (!object.has("items"))
			object.add("items", new JsonArray());
		JsonElement items = object.get("items");
		if (!object.has("weapon_types"))
			object.add("weapon_types", new JsonArray());
		JsonElement weapons = object.get("weapon_types");
		if (!object.has("blocking_types"))
			object.add("blocking_types", new JsonArray());
		JsonElement defenders = object.get("blocking_types");
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
					Boolean tierable;
					Boolean hasSwordEnchants = null;
					Double piercingLevel = null;
					if (jsonObject.has("tierable"))
						tierable = getBoolean(jsonObject, "tierable");
					else
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
					if (jsonObject.has("has_sword_enchants"))
						hasSwordEnchants = getBoolean(jsonObject, "has_sword_enchants");
					if (jsonObject.has("armor_piercing"))
						piercingLevel = getDouble(jsonObject, "armor_piercing");
					ConfigurableWeaponData configurableWeaponData = new ConfigurableWeaponData(damageOffset, speed, reach, chargedReach, tierable, blockingType, hasSwordEnchants, piercingLevel);
					configuredWeapons.put(type, configurableWeaponData);
				} else
					throw new ReportedException(CrashReport.forThrowable(new IllegalStateException("Not a JSON Object: " + jsonElement + " this may be due to an incorrectly written config file."), "Configuring Weapon Types"));
			});
		}
	}
	@Override
	protected InputStream getDefaultedConfig() {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream("combatify-items.json");
	}

	@Override
	public void defineConfigHolders() {
		configuredItems = new HashMap<>();
		configuredWeapons = new HashMap<>();
	}

	@Override
	public <T> void alertChange(ConfigValue<T> tConfigValue, T newValue) {

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
		super.loadFromNetwork(buf);
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
			Double blockBase = buf1.readDouble();
			Double blockFactor = buf1.readDouble();
			Double blockKbRes = buf1.readDouble();
			Integer enchantlevel = buf1.readInt();
			int isEnchantableAsInt = buf1.readInt();
			int hasSwordEnchantsAsInt = buf1.readInt();
			Boolean isEnchantable = null;
			Boolean hasSwordEnchants = null;
			Integer useDuration = buf1.readInt();
			Double piercingLevel = buf1.readDouble();
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
			if (blockBase == -10)
				blockBase = null;
			if (blockFactor == -10)
				blockFactor = null;
			if(blockKbRes == -10)
				blockKbRes = null;
			if(enchantlevel == -10)
				enchantlevel = null;
			if (isEnchantableAsInt != -10)
				isEnchantable = isEnchantableAsInt == 1;
			if (hasSwordEnchantsAsInt != -10)
				hasSwordEnchants = hasSwordEnchantsAsInt == 1;
			if (useDuration == -10)
				useDuration = null;
			if (piercingLevel == -10)
				piercingLevel = null;
			switch (weaponType) {
				case "SWORD", "LONGSWORD", "AXE", "PICKAXE", "HOE", "SHOVEL", "KNIFE", "TRIDENT" -> type = WeaponType.fromID(weaponType);
			}
			return new ConfigurableItemData(damage, speed, reach, chargedReach, stackSize, cooldown, cooldownAfter, type, bType, blockBase, blockFactor, blockKbRes, enchantlevel, isEnchantable, hasSwordEnchants, useDuration, piercingLevel);
		});
		configuredWeapons = buf.readMap(buf1 -> WeaponType.fromID(buf1.readUtf()), buf1 -> {
			Double damageOffset = buf1.readDouble();
			Double speed = buf1.readDouble();
			Double reach = buf1.readDouble();
			Double chargedReach = buf1.readDouble();
			Boolean tierable = buf1.readBoolean();
			String blockingType = buf1.readUtf();
			BlockingType bType = Combatify.registeredTypes.get(blockingType);
			int hasSwordEnchantsAsInt = buf1.readInt();
			Boolean hasSwordEnchants = null;
			Double piercingLevel = buf1.readDouble();
			if (damageOffset == -10)
				damageOffset = null;
			if (speed == -10)
				speed = null;
			if (reach == -10)
				reach = null;
			if (chargedReach == -10)
				chargedReach = null;
			if (hasSwordEnchantsAsInt != -10)
				hasSwordEnchants = hasSwordEnchantsAsInt == 1;
			if (piercingLevel == -10)
				piercingLevel = null;
			return new ConfigurableWeaponData(damageOffset, speed, reach, chargedReach, tierable, bType, hasSwordEnchants, piercingLevel);
		});
		return this;
	}

	public void saveToNetwork(FriendlyByteBuf buf) {
		super.saveToNetwork(buf);
		buf.writeMap(Combatify.registeredTypes, FriendlyByteBuf::writeUtf, (buf1, blockingType) -> {
			buf1.writeUtf(blockingType.getClass().getName());
			buf1.writeUtf(blockingType.getName());
			buf1.writeBoolean(blockingType.canBeDisabled());
			buf1.writeBoolean(blockingType.canBlockHit());
			buf1.writeBoolean(blockingType.canCrouchBlock());
			buf1.writeBoolean(blockingType.defaultKbMechanics());
			buf1.writeBoolean(blockingType.isToolBlocker());
			buf1.writeBoolean(blockingType.requireFullCharge());
			buf1.writeBoolean(blockingType.requiresSwordBlocking());
		});
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
			buf12.writeUtf(configurableItemData.blockingType == null ? "blank" : configurableItemData.blockingType.getName());
			buf12.writeDouble(configurableItemData.blockBase == null ? -10 : configurableItemData.blockBase);
			buf12.writeDouble(configurableItemData.blockFactor == null ? -10 : configurableItemData.blockFactor);
			buf12.writeDouble(configurableItemData.blockKbRes == null ? -10 : configurableItemData.blockKbRes);
			buf12.writeInt(configurableItemData.enchantability == null ? -10 : configurableItemData.enchantability);
			buf12.writeInt(configurableItemData.isEnchantable == null ? -10 : configurableItemData.isEnchantable ? 1 : 0);
			buf12.writeInt(configurableItemData.hasSwordEnchants == null ? -10 : configurableItemData.hasSwordEnchants ? 1 : 0);
			buf12.writeInt(configurableItemData.useDuration == null ? -10 : configurableItemData.useDuration);
			buf12.writeDouble(configurableItemData.piercingLevel == null ? -10 : configurableItemData.piercingLevel);
		});
		buf.writeMap(configuredWeapons, (buf1, type) -> buf1.writeUtf(type.name()), (buf12, configurableWeaponData) -> {
			buf12.writeDouble(configurableWeaponData.damageOffset == null ? -10 : configurableWeaponData.damageOffset);
			buf12.writeDouble(configurableWeaponData.speed == null ? -10 : configurableWeaponData.speed);
			buf12.writeDouble(configurableWeaponData.reach == null ? -10 : configurableWeaponData.reach);
			buf12.writeDouble(configurableWeaponData.chargedReach == null ? -10 : configurableWeaponData.chargedReach);
			buf12.writeBoolean(configurableWeaponData.tierable);
			buf12.writeUtf(configurableWeaponData.blockingType == null ? "blank" : configurableWeaponData.blockingType.getName());
			buf12.writeInt(configurableWeaponData.hasSwordEnchants == null ? -10 : configurableWeaponData.hasSwordEnchants ? 1 : 0);
			buf12.writeDouble(configurableWeaponData.piercingLevel == null ? -10 : configurableWeaponData.piercingLevel);
		});
	}

	@Override
	public void handleExtraSync(NetworkingHandler.AtlasConfigPacket packet, LocalPlayer player, PacketSender sender) {
		LOGGER.info("Loading config details from buffer.");

		List<Item> items = BuiltInRegistries.ITEM.stream().toList();

		for(Item item : items)
			((ItemExtensions) item).modifyAttributeModifiers();
		for (Item item : ITEMS.configuredItems.keySet()) {
			ConfigurableItemData configurableItemData = ITEMS.configuredItems.get(item);
			if (configurableItemData.stackSize != null)
				((ItemExtensions) item).setStackSize(configurableItemData.stackSize);
		}
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
		Double blockBase = null;
		Double blockFactor = null;
		Double blockKbRes = null;
		Integer enchantment_level = null;
		Boolean isEnchantable = null;
		Boolean hasSwordEnchants = null;
		Integer useDuration = null;
		Double piercingLevel = null;
		if (configuredItems.containsKey(item)) {
			ConfigurableItemData oldData = configuredItems.get(item);
			damage = oldData.damage;
			speed = oldData.speed;
			reach = oldData.reach;
			chargedReach = oldData.chargedReach;
			stack_size = oldData.stackSize;
			cooldown = oldData.cooldown;
			cooldownAfterUse = oldData.cooldownAfter;
			type = oldData.type;
			blockingType = oldData.blockingType;
			blockBase = oldData.blockBase;
			blockFactor = oldData.blockFactor;
			blockKbRes = oldData.blockKbRes;
			enchantment_level = oldData.enchantability;
			isEnchantable = oldData.isEnchantable;
			hasSwordEnchants = oldData.hasSwordEnchants;
			useDuration = oldData.useDuration;
			piercingLevel = oldData.piercingLevel;
		}
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
		if (cooldown != null && cooldownAfterUse == null) {
			if (!jsonObject.has("cooldown_after"))
				throw new ReportedException(CrashReport.forThrowable(new JsonSyntaxException("The JSON must contain the boolean 'cooldown_after' if a cooldown is defined!"), "Applying Item Cooldown"));
			cooldownAfterUse = getBoolean(jsonObject, "cooldown_after");
		}
		if (jsonObject.has("base_protection"))
			blockBase = getDouble(jsonObject, "base_protection");
		if (jsonObject.has("protection_factor"))
			blockFactor = getDouble(jsonObject, "protection_factor");
		if (jsonObject.has("block_knockback_resistance"))
			blockKbRes = getDouble(jsonObject, "block_knockback_resistance");
		if (jsonObject.has("enchantment_level"))
			enchantment_level = getInt(jsonObject, "enchantment_level");
		if (jsonObject.has("is_enchantable"))
			isEnchantable = getBoolean(jsonObject, "is_enchantable");
		if (jsonObject.has("has_sword_enchants"))
			hasSwordEnchants = getBoolean(jsonObject, "has_sword_enchants");
		if (jsonObject.has("use_duration"))
			useDuration = getInt(jsonObject, "use_duration");
		if (jsonObject.has("armor_piercing"))
			piercingLevel = getDouble(jsonObject, "armor_piercing");
		ConfigurableItemData configurableItemData = new ConfigurableItemData(damage, speed, reach, chargedReach, stack_size, cooldown, cooldownAfterUse, type, blockingType, blockBase, blockFactor, blockKbRes, enchantment_level, isEnchantable, hasSwordEnchants, useDuration, piercingLevel);
		configuredItems.put(item, configurableItemData);
	}
}
