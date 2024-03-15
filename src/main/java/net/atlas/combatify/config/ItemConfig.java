package net.atlas.combatify.config;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
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
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static net.atlas.combatify.Combatify.*;

public class ItemConfig extends AtlasConfig {
	public Map<Item, ConfigurableItemData> configuredItems;
	public Map<WeaponType, ConfigurableWeaponData> configuredWeapons;
	public BiMap<String, Tier> tiers;

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
		if (!object.has("tiers"))
			object.add("tiers", new JsonArray());
		JsonElement tiers = object.get("tiers");
		if (tiers instanceof JsonArray typeArray) {
			typeArray.asList().forEach(jsonElement -> {
				if (jsonElement instanceof JsonObject jsonObject) {
					parseTiers(jsonObject);
				} else
					throw new ReportedException(CrashReport.forThrowable(new IllegalStateException("Not a JSON Object: " + jsonElement + " this may be due to an incorrectly written config file."), "Configuring Tiers"));
			});
		}
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
					Boolean canSweep = null;
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
					if (jsonObject.has("can_sweep"))
						canSweep = getBoolean(jsonObject, "can_sweep");
					ConfigurableWeaponData configurableWeaponData = new ConfigurableWeaponData(damageOffset, speed, reach, chargedReach, tierable, blockingType, hasSwordEnchants, piercingLevel, canSweep);
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
		tiers = HashBiMap.create();
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

	public static Float getFloat(JsonObject element, String name) {
		return element.get(name).getAsFloat();
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
			case "SWORD", "MACE", "LONGSWORD", "AXE", "PICKAXE", "HOE", "SHOVEL", "KNIFE", "TRIDENT" -> WeaponType.fromID(weapon_type);
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
						if (jsonObject.has("has_shield_delay"))
							blockingType.setDelay(getBoolean(jsonObject, "has_shield_delay"));
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
		if (jsonObject.has("has_shield_delay"))
			blockingType.setDelay(getBoolean(jsonObject, "has_shield_delay"));
	}
	public void parseTiers(JsonObject jsonObject) {
		if (!jsonObject.has("name"))
			throw new ReportedException(CrashReport.forThrowable(new IllegalStateException("An added tier does not possess a name. This is due to an incorrectly written config file."), "Configuring Tiers"));
		String name = getString(jsonObject, "name");
		if (!jsonObject.has("base_tier"))
			throw new ReportedException(CrashReport.forThrowable(new IllegalStateException("An added tier by the name of " + name + " has no tier to base off of. This is required for a tier to function."), "Configuring Tiers"));
		Tier baseTier = getTier(getString(jsonObject, "base_tier"));
		int uses = baseTier.getUses();
		if (jsonObject.has("uses"))
			uses = getInt(jsonObject, "uses");
		float speed = baseTier.getSpeed();
		if (jsonObject.has("mining_speed"))
			speed = getFloat(jsonObject, "mining_speed");
		float damage = baseTier.getAttackDamageBonus();
		if (jsonObject.has("damage_bonus"))
			damage = getFloat(jsonObject, "damage_bonus");
		int level = baseTier.getLevel();
		if (jsonObject.has("mining_level"))
			level = getInt(jsonObject, "mining_level");
		int enchantLevel = baseTier.getEnchantmentValue();
		if (jsonObject.has("enchant_level"))
			enchantLevel = getInt(jsonObject, "enchant_level");
		Ingredient repairIngredient = baseTier.getRepairIngredient();
		if (jsonObject.has("repair_ingredient"))
			repairIngredient = Ingredient.of(itemFromName(getString(jsonObject, "repair_ingredient")));
		int finalUses = uses;
		float finalSpeed = speed;
		float finalDamage = damage;
		int finalLevel = level;
		int finalEnchantLevel = enchantLevel;
		Ingredient finalRepairIngredient = repairIngredient;
		tiers.put(name, new Tier() {
			@Override
			public int getUses() {
				return finalUses;
			}

			@Override
			public float getSpeed() {
				return finalSpeed;
			}

			@Override
			public float getAttackDamageBonus() {
				return finalDamage;
			}

			@Override
			public int getLevel() {
				return finalLevel;
			}

			@Override
			public int getEnchantmentValue() {
				return finalEnchantLevel;
			}

			@Override
			public @NotNull Ingredient getRepairIngredient() {
				return finalRepairIngredient;
			}
		});
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
					blockingType.setPercentage(buf1.readBoolean());
					blockingType.setToolBlocker(buf1.readBoolean());
					blockingType.setRequireFullCharge(buf1.readBoolean());
					blockingType.setSwordBlocking(buf1.readBoolean());
					blockingType.setDelay(buf1.readBoolean());
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
		configuredItems = buf.readMap(buf12 -> BuiltInRegistries.ITEM.get(buf12.readResourceLocation()), buf1 -> {
			Double damage = buf1.readDouble();
			Double speed = buf1.readDouble();
			Double reach = buf1.readDouble();
			Double chargedReach = buf1.readDouble();
			Integer stackSize = buf1.readVarInt();
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
			int isEnchantableAsInt = buf1.readInt();
			int hasSwordEnchantsAsInt = buf1.readInt();
			Boolean isEnchantable = null;
			Boolean hasSwordEnchants = null;
			Integer useDuration = buf1.readInt();
			Double piercingLevel = buf1.readDouble();
			int canSweepAsInt = buf1.readInt();
			Boolean canSweep = null;
			Tier tier = getTier(buf1.readUtf());
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
			if(blockStrength == -10)
				blockStrength = null;
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
			if (canSweepAsInt != -10)
				canSweep = canSweepAsInt == 1;
			switch (weaponType) {
				case "SWORD", "MACE", "LONGSWORD", "AXE", "PICKAXE", "HOE", "SHOVEL", "KNIFE", "TRIDENT" -> type = WeaponType.fromID(weaponType);
			}
			return new ConfigurableItemData(damage, speed, reach, chargedReach, stackSize, cooldown, cooldownAfter, type, bType, blockStrength, blockKbRes, enchantlevel, isEnchantable, hasSwordEnchants, useDuration, piercingLevel, canSweep, tier);
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
			int canSweepAsInt = buf1.readInt();
			Boolean canSweep = null;
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
			if (canSweepAsInt != -10)
				canSweep = canSweepAsInt == 1;
			return new ConfigurableWeaponData(damageOffset, speed, reach, chargedReach, tierable, bType, hasSwordEnchants, piercingLevel, canSweep);
		});
		tiers = HashBiMap.create(buf.readMap(FriendlyByteBuf::readUtf, buf1 -> {
			int uses = buf1.readVarInt();
			float speed = buf1.readFloat();
			float damage = buf1.readFloat();
			int level = buf1.readVarInt();
			int enchantLevel = buf1.readVarInt();
			Ingredient ingredient = Ingredient.of(BuiltInRegistries.ITEM.get(buf1.readResourceLocation()));
			return new Tier() {
				@Override
				public int getUses() {
					return uses;
				}

				@Override
				public float getSpeed() {
					return speed;
				}

				@Override
				public float getAttackDamageBonus() {
					return damage;
				}

				@Override
				public int getLevel() {
					return level;
				}

				@Override
				public int getEnchantmentValue() {
					return enchantLevel;
				}

				@Override
				public @NotNull Ingredient getRepairIngredient() {
					return ingredient;
				}
			};
		}));
		return this;
	}

	public Tier getTier(String s) {
		return switch (s.toLowerCase()) {
			case "wood", "wooden" -> Tiers.WOOD;
			case "stone" -> Tiers.STONE;
			case "iron" -> Tiers.IRON;
			case "gold", "golden" -> Tiers.GOLD;
			case "diamond" -> Tiers.DIAMOND;
			case "netherite" -> Tiers.NETHERITE;
			default -> getTierRaw(s);
		};
	}
	private String getTierName(Tier tier) {
		if (tier instanceof Tiers vanilla) {
			return switch (vanilla) {
				case WOOD -> "wood";
				case GOLD -> "gold";
				case STONE -> "stone";
				case IRON -> "iron";
				case DIAMOND -> "diamond";
				case NETHERITE -> "netherite";
			};
		}
		return tiers.inverse().get(tier);
	}
	private Tier getTierRaw(String s) {
		if (!tiers.containsKey(s) || Objects.equals(s, "empty")) return null;
		return tiers.get(s);
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
			buf1.writeBoolean(blockingType.isPercentage());
			buf1.writeBoolean(blockingType.isToolBlocker());
			buf1.writeBoolean(blockingType.requireFullCharge());
			buf1.writeBoolean(blockingType.requiresSwordBlocking());
			buf1.writeBoolean(blockingType.hasDelay());
		});
		buf.writeMap(configuredItems, (buf1, item) -> buf1.writeResourceLocation(BuiltInRegistries.ITEM.getKey(item)), (buf12, configurableItemData) -> {
			buf12.writeDouble(configurableItemData.damage == null ? -10 : configurableItemData.damage);
			buf12.writeDouble(configurableItemData.speed == null ? -10 : configurableItemData.speed);
			buf12.writeDouble(configurableItemData.reach == null ? -10 : configurableItemData.reach);
			buf12.writeDouble(configurableItemData.chargedReach == null ? -10 : configurableItemData.chargedReach);
			buf12.writeVarInt(configurableItemData.stackSize == null ? -10 : configurableItemData.stackSize);
			buf12.writeInt(configurableItemData.cooldown == null ? -10 : configurableItemData.cooldown);
			if(configurableItemData.cooldown != null)
				buf12.writeBoolean(configurableItemData.cooldownAfter);
			buf12.writeUtf(configurableItemData.type == null ? "empty" : configurableItemData.type.name());
			buf12.writeUtf(configurableItemData.blockingType == null ? "blank" : configurableItemData.blockingType.getName());
			buf12.writeDouble(configurableItemData.blockStrength == null ? -10 : configurableItemData.blockStrength);
			buf12.writeDouble(configurableItemData.blockKbRes == null ? -10 : configurableItemData.blockKbRes);
			buf12.writeInt(configurableItemData.enchantability == null ? -10 : configurableItemData.enchantability);
			buf12.writeInt(configurableItemData.isEnchantable == null ? -10 : configurableItemData.isEnchantable ? 1 : 0);
			buf12.writeInt(configurableItemData.hasSwordEnchants == null ? -10 : configurableItemData.hasSwordEnchants ? 1 : 0);
			buf12.writeInt(configurableItemData.useDuration == null ? -10 : configurableItemData.useDuration);
			buf12.writeDouble(configurableItemData.piercingLevel == null ? -10 : configurableItemData.piercingLevel);
			buf12.writeInt(configurableItemData.canSweep == null ? -10 : configurableItemData.canSweep ? 1 : 0);
			buf12.writeUtf(configurableItemData.tier == null ? "empty" : getTierName(configurableItemData.tier));
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
			buf12.writeInt(configurableWeaponData.canSweep == null ? -10 : configurableWeaponData.canSweep ? 1 : 0);
		});
		buf.writeMap(tiers, FriendlyByteBuf::writeUtf, (buf1, tier) -> {
			buf1.writeVarInt(tier.getUses());
			buf1.writeFloat(tier.getSpeed());
			buf1.writeFloat(tier.getAttackDamageBonus());
			buf1.writeVarInt(tier.getLevel());
			buf1.writeVarInt(tier.getEnchantmentValue());
			buf1.writeResourceLocation(BuiltInRegistries.ITEM.getKey(tier.getRepairIngredient().getItems()[0].getItem()));
		});
	}

	@Override
	public void handleExtraSync(NetworkingHandler.AtlasConfigPacket packet, LocalPlayer player, PacketSender sender) {
		LOGGER.info("Loading config details from buffer.");

		for (Item item : Combatify.ITEMS.configuredItems.keySet()) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(item);
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
		Double blockStrength = null;
		Double blockKbRes = null;
		Integer enchantment_level = null;
		Boolean isEnchantable = null;
		Boolean hasSwordEnchants = null;
		Integer useDuration = null;
		Double piercingLevel = null;
		Boolean canSweep = null;
		Tier tier = null;
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
			blockStrength = oldData.blockStrength;
			blockKbRes = oldData.blockKbRes;
			enchantment_level = oldData.enchantability;
			isEnchantable = oldData.isEnchantable;
			hasSwordEnchants = oldData.hasSwordEnchants;
			useDuration = oldData.useDuration;
			piercingLevel = oldData.piercingLevel;
			canSweep = oldData.canSweep;
			tier = oldData.tier;
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
				case "SWORD", "MACE", "LONGSWORD", "AXE", "PICKAXE", "HOE", "SHOVEL", "KNIFE", "TRIDENT" ->
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
		if (jsonObject.has("damage_protection"))
			blockStrength = getDouble(jsonObject, "damage_protection");
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
		if (jsonObject.has("can_sweep"))
			canSweep = getBoolean(jsonObject, "can_sweep");
		if (jsonObject.has("tier"))
			tier = getTier(getString(jsonObject, "tier"));
		ConfigurableItemData configurableItemData = new ConfigurableItemData(damage, speed, reach, chargedReach, stack_size, cooldown, cooldownAfterUse, type, blockingType, blockStrength, blockKbRes, enchantment_level, isEnchantable, hasSwordEnchants, useDuration, piercingLevel, canSweep, tier);
		configuredItems.put(item, configurableItemData);
	}
}
