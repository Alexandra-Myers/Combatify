package net.atlas.combatify.config;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.atlas.atlaslib.AtlasLib;
import net.atlas.atlaslib.config.AtlasConfig;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.ExtendedTier;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.BlockingType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static net.atlas.combatify.Combatify.*;

public class ItemConfig extends AtlasConfig {
	public Map<Item, ConfigurableItemData> configuredItems;
	public Map<WeaponType, ConfigurableWeaponData> configuredWeapons;
	public BiMap<String, Tier> tiers;
	public static Formula armourCalcs = null;

	public ItemConfig() {
		super(id("combatify-items"));
	}

	public static Formula getArmourCalcs() {
		return armourCalcs;
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
					Boolean isPrimaryForSwordEnchants = null;
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
					if (type == null) {
						if (damageOffset == null || speed == null || reach == null)
							throw new ReportedException(CrashReport.forThrowable(new JsonSyntaxException("The JSON must contain the weapon type's attributes if a new weapon type is added!"), "Configuring Weapon Types"));
						type = tierable ? WeaponType.createBasic(GsonHelper.getAsString(jsonObject, "name").toLowerCase(Locale.ROOT), damageOffset, speed, reach) : WeaponType.createBasicUntierable(GsonHelper.getAsString(jsonObject, "name").toLowerCase(Locale.ROOT), damageOffset, speed, reach);
					}
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
					if (jsonObject.has("sword_enchants_from_enchanting"))
						isPrimaryForSwordEnchants = getBoolean(jsonObject, "sword_enchants_from_enchanting");
					ConfigurableWeaponData configurableWeaponData = new ConfigurableWeaponData(damageOffset, speed, reach, chargedReach, tierable, blockingType, hasSwordEnchants, isPrimaryForSwordEnchants, piercingLevel, canSweep);
					configuredWeapons.put(type, configurableWeaponData);
				} else
					throw new ReportedException(CrashReport.forThrowable(new IllegalStateException("Not a JSON Object: " + jsonElement + " this may be due to an incorrectly written config file."), "Configuring Weapon Types"));
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
		if (object.has("armor_calculation"))
			armourCalcs = new Formula(getString(object, "armor_calculation"));
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
	public void resetExtraHolders() {
		defineConfigHolders();
	}

	@Override
	public <T> void alertChange(ConfigValue<T> tConfigValue, T newValue) {

	}

	public static String getString(JsonObject object, String name) {
		return object.get(name).getAsString();
	}

	public static Integer getInt(JsonObject object, String name) {
		return object.get(name).getAsInt();
	}

	public static Integer getIntWithFallback(JsonObject object, String name, String fallback) {
		if (!object.has(name))
			return object.get(fallback).getAsInt();
		return object.get(name).getAsInt();
	}

	public static Float getFloat(JsonObject object, String name) {
		return object.get(name).getAsFloat();
	}

	public static Double getDouble(JsonObject object, String name) {
		return object.get(name).getAsDouble();
	}
	public static Boolean getBoolean(JsonObject object, String name) {
		return object.get(name).getAsBoolean();
	}

	public static Item itemFromJson(JsonObject jsonObject) {
		return itemFromName(GsonHelper.getAsString(jsonObject, "name"));
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
		weapon_type = weapon_type.toLowerCase(Locale.ROOT);
		if (!registeredWeaponTypes.containsKey(weapon_type))
			return null;
		return WeaponType.fromID(weapon_type);
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
		String baseTierName = getString(jsonObject, "base_tier");
		Tier baseTier = getTier(baseTierName);
		int uses = baseTier.getUses();
		if (jsonObject.has("uses"))
			uses = getInt(jsonObject, "uses");
		float speed = baseTier.getSpeed();
		if (jsonObject.has("mining_speed"))
			speed = getFloat(jsonObject, "mining_speed");
		float damage = baseTier.getAttackDamageBonus();
		if (jsonObject.has("damage_bonus"))
			damage = getFloat(jsonObject, "damage_bonus");
		int level = baseTier instanceof ExtendedTier extendedTier ? extendedTier.getLevel() : ExtendedTier.getLevelFromDefault(baseTier);
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
		tiers.put(name, new ExtendedTier() {
			@Override
			public int getLevel() {
				return finalLevel;
			}

			@Override
			public String baseTierName() {
				return baseTierName;
			}

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
		tiers = HashBiMap.create(buf.readMap(FriendlyByteBuf::readUtf, buf1 -> {
			int uses = buf1.readVarInt();
			float speed = buf1.readFloat();
			float damage = buf1.readFloat();
			int level = buf1.readVarInt();
			int enchantLevel = buf1.readVarInt();
			Ingredient ingredient = Ingredient.of(BuiltInRegistries.ITEM.get(buf1.readResourceLocation()));
			String baseTier = buf1.readUtf();
			return new ExtendedTier() {
				@Override
				public int getLevel() {
					return level;
				}

				@Override
				public String baseTierName() {
					return baseTier;
				}

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
				public int getEnchantmentValue() {
					return enchantLevel;
				}

				@Override
				public @NotNull Ingredient getRepairIngredient() {
					return ingredient;
				}
			};
		}));
		registeredWeaponTypes = buf.readMap(FriendlyByteBuf::readUtf, buf1 -> {
			String name = buf1.readUtf();
			double damageOffset = buf1.readDouble();
			double speed = buf1.readDouble();
			double reach = buf1.readDouble();
			boolean useAxeDamage = buf1.readBoolean();
			boolean useHoeDamage = buf1.readBoolean();
			boolean useHoeSpeed = buf1.readBoolean();
			boolean hasSwordEnchants = buf1.readBoolean();
			boolean tierable = buf1.readBoolean();
			return new WeaponType(name, damageOffset, speed, reach, useAxeDamage, useHoeDamage, useHoeSpeed, tierable, hasSwordEnchants, true);
		});
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
			Integer durability = buf1.readVarInt();
			Integer cooldown = buf1.readInt();
			Boolean cooldownAfter = null;
			if (cooldown != -10)
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
			int isPrimaryForSwordEnchantsAsInt = buf1.readInt();
			Boolean isEnchantable = null;
			Boolean hasSwordEnchants = null;
			Boolean isPrimaryForSwordEnchants = null;
			Integer useDuration = buf1.readInt();
			Double piercingLevel = buf1.readDouble();
			int canSweepAsInt = buf1.readInt();
			Boolean canSweep = null;
			Tier tier = getTier(buf1.readUtf());
			Integer defense = buf1.readInt();
			Double toughness = buf1.readDouble();
			Double armourKbRes = buf1.readDouble();
			String repairIngredient = buf1.readUtf();
			Ingredient ingredient = Objects.equals(repairIngredient, "empty") ? null : Ingredient.of(BuiltInRegistries.ITEM.get(new ResourceLocation(repairIngredient)));
			if (damage == -10)
				damage = null;
			if (speed == -10)
				speed = null;
			if (reach == -10)
				reach = null;
			if (chargedReach == -10)
				chargedReach = null;
			if (stackSize == -10)
				stackSize = null;
			if (durability == -10)
				durability = null;
			if (cooldown == -10)
				cooldown = null;
			if (blockStrength == -10)
				blockStrength = null;
			if (blockKbRes == -10)
				blockKbRes = null;
			if (enchantlevel == -10)
				enchantlevel = null;
			if (isEnchantableAsInt != -10)
				isEnchantable = isEnchantableAsInt == 1;
			if (hasSwordEnchantsAsInt != -10)
				hasSwordEnchants = hasSwordEnchantsAsInt == 1;
			if (isPrimaryForSwordEnchantsAsInt != -10)
				isPrimaryForSwordEnchants = isPrimaryForSwordEnchantsAsInt == 1;
			if (useDuration == -10)
				useDuration = null;
			if (piercingLevel == -10)
				piercingLevel = null;
			if (canSweepAsInt != -10)
				canSweep = canSweepAsInt == 1;
			if (registeredWeaponTypes.containsKey(weaponType))
				type = WeaponType.fromID(weaponType);
			if (defense == -10)
				defense = null;
			if (toughness == -10)
				toughness = null;
			if (armourKbRes == -10)
				armourKbRes = null;
			return new ConfigurableItemData(damage, speed, reach, chargedReach, stackSize, cooldown, cooldownAfter, type, bType, blockStrength, blockKbRes, enchantlevel, isEnchantable, hasSwordEnchants, isPrimaryForSwordEnchants, useDuration, piercingLevel, canSweep, tier, durability, defense, toughness, armourKbRes, ingredient);
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
			int isPrimaryForSwordEnchantsAsInt = buf1.readInt();
			Boolean isPrimaryForSwordEnchants = null;
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
			if (isPrimaryForSwordEnchantsAsInt != -10)
				isPrimaryForSwordEnchants = isPrimaryForSwordEnchantsAsInt == 1;
			if (piercingLevel == -10)
				piercingLevel = null;
			if (canSweepAsInt != -10)
				canSweep = canSweepAsInt == 1;
			return new ConfigurableWeaponData(damageOffset, speed, reach, chargedReach, tierable, bType, hasSwordEnchants, isPrimaryForSwordEnchants, piercingLevel, canSweep);
		});
		String formula = buf.readUtf();
		if (!formula.equals("empty"))
			armourCalcs = new Formula(formula);
		return this;
	}

	public Tier getTier(String s) {
		return switch (s.toLowerCase()) {
			case "wood" -> Tiers.WOOD;
			case "stone" -> Tiers.STONE;
			case "iron" -> Tiers.IRON;
			case "gold" -> Tiers.GOLD;
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
		buf.writeMap(tiers, FriendlyByteBuf::writeUtf, (buf1, tier) -> {
			buf1.writeVarInt(tier.getUses());
			buf1.writeFloat(tier.getSpeed());
			buf1.writeFloat(tier.getAttackDamageBonus());
			buf1.writeVarInt(ExtendedTier.getLevel(tier));
			buf1.writeVarInt(tier.getEnchantmentValue());
			buf1.writeResourceLocation(BuiltInRegistries.ITEM.getKey(tier.getRepairIngredient().getItems()[0].getItem()));
			buf1.writeUtf(tier instanceof ExtendedTier extendedTier ? extendedTier.baseTierName() : getTierName(tier));
		});
		buf.writeMap(registeredWeaponTypes, FriendlyByteBuf::writeUtf, (buf1, weaponType) -> {
			buf1.writeUtf(weaponType.name);
			buf1.writeDouble(weaponType.damageOffset);
			buf1.writeDouble(weaponType.speed);
			buf1.writeDouble(weaponType.reach);
			buf1.writeBoolean(weaponType.useAxeDamage);
			buf1.writeBoolean(weaponType.useHoeDamage);
			buf1.writeBoolean(weaponType.useHoeSpeed);
			buf1.writeBoolean(weaponType.hasSwordEnchants);
			buf1.writeBoolean(weaponType.tierable);
		});
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
			buf1.writeBoolean(blockingType.hasDelay());
		});
		buf.writeMap(configuredItems, (buf1, item) -> buf1.writeResourceLocation(BuiltInRegistries.ITEM.getKey(item)), (buf12, configurableItemData) -> {
			buf12.writeDouble(configurableItemData.damage == null ? -10 : configurableItemData.damage);
			buf12.writeDouble(configurableItemData.speed == null ? -10 : configurableItemData.speed);
			buf12.writeDouble(configurableItemData.reach == null ? -10 : configurableItemData.reach);
			buf12.writeDouble(configurableItemData.chargedReach == null ? -10 : configurableItemData.chargedReach);
			buf12.writeVarInt(configurableItemData.stackSize == null ? -10 : configurableItemData.stackSize);
			buf12.writeVarInt(configurableItemData.durability == null ? -10 : configurableItemData.durability);
			buf12.writeInt(configurableItemData.cooldown == null ? -10 : configurableItemData.cooldown);
			if(configurableItemData.cooldown != null)
				buf12.writeBoolean(configurableItemData.cooldownAfter);
			buf12.writeUtf(configurableItemData.type == null ? "empty" : configurableItemData.type.name);
			buf12.writeUtf(configurableItemData.blockingType == null ? "blank" : configurableItemData.blockingType.getName());
			buf12.writeDouble(configurableItemData.blockStrength == null ? -10 : configurableItemData.blockStrength);
			buf12.writeDouble(configurableItemData.blockKbRes == null ? -10 : configurableItemData.blockKbRes);
			buf12.writeInt(configurableItemData.enchantability == null ? -10 : configurableItemData.enchantability);
			buf12.writeInt(configurableItemData.isEnchantable == null ? -10 : configurableItemData.isEnchantable ? 1 : 0);
			buf12.writeInt(configurableItemData.hasSwordEnchants == null ? -10 : configurableItemData.hasSwordEnchants ? 1 : 0);
			buf12.writeInt(configurableItemData.isPrimaryForSwordEnchants == null ? -10 : configurableItemData.isPrimaryForSwordEnchants ? 1 : 0);
			buf12.writeInt(configurableItemData.useDuration == null ? -10 : configurableItemData.useDuration);
			buf12.writeDouble(configurableItemData.piercingLevel == null ? -10 : configurableItemData.piercingLevel);
			buf12.writeInt(configurableItemData.canSweep == null ? -10 : configurableItemData.canSweep ? 1 : 0);
			buf12.writeUtf(configurableItemData.tier == null ? "empty" : getTierName(configurableItemData.tier));
			buf12.writeInt(configurableItemData.defense == null ? -10 : configurableItemData.defense);
			buf12.writeDouble(configurableItemData.toughness == null ? -10 : configurableItemData.toughness);
			buf12.writeDouble(configurableItemData.armourKbRes == null ? -10 : configurableItemData.armourKbRes);
			buf12.writeUtf(configurableItemData.repairIngredient == null ? "empty" : BuiltInRegistries.ITEM.getKey(configurableItemData.repairIngredient.getItems()[0].getItem()).toString());
		});
		buf.writeMap(configuredWeapons, (buf1, type) -> buf1.writeUtf(type.name), (buf12, configurableWeaponData) -> {
			buf12.writeDouble(configurableWeaponData.damageOffset == null ? -10 : configurableWeaponData.damageOffset);
			buf12.writeDouble(configurableWeaponData.speed == null ? -10 : configurableWeaponData.speed);
			buf12.writeDouble(configurableWeaponData.reach == null ? -10 : configurableWeaponData.reach);
			buf12.writeDouble(configurableWeaponData.chargedReach == null ? -10 : configurableWeaponData.chargedReach);
			buf12.writeBoolean(configurableWeaponData.tierable);
			buf12.writeUtf(configurableWeaponData.blockingType == null ? "blank" : configurableWeaponData.blockingType.getName());
			buf12.writeInt(configurableWeaponData.hasSwordEnchants == null ? -10 : configurableWeaponData.hasSwordEnchants ? 1 : 0);
			buf12.writeInt(configurableWeaponData.isPrimaryForSwordEnchants == null ? -10 : configurableWeaponData.isPrimaryForSwordEnchants ? 1 : 0);
			buf12.writeDouble(configurableWeaponData.piercingLevel == null ? -10 : configurableWeaponData.piercingLevel);
			buf12.writeInt(configurableWeaponData.canSweep == null ? -10 : configurableWeaponData.canSweep ? 1 : 0);
		});
		buf.writeUtf(armourCalcs == null ? "empty" : armourCalcs.written);
	}

	@Override
	public void saveExtra(JsonWriter jsonWriter, PrintWriter printWriter) {

	}

	@Override
	public void handleExtraSync(AtlasLib.AtlasConfigPacket packet, LocalPlayer player, PacketSender sender) {
		LOGGER.info("Loading config details from buffer.");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Screen createScreen(Screen prevScreen) {
		ConfigBuilder builder = ConfigBuilder.create()
			.setTitle(Component.translatable("text.config.combatify-items.title"))
			.transparentBackground()
			.setSavingRunnable(() -> {
				try {
					saveConfig();
				} catch (IOException e) {
					Combatify.LOGGER.error("Failed to save combatify:combatify-items config file!");
					e.printStackTrace();
				}
			});
		if (prevScreen != null) builder.setParentScreen(prevScreen);
		ConfigCategory configCategory = builder.getOrCreateCategory(Component.translatable("text.config.combatify-items.title"));
		builder.setFallbackCategory(configCategory);

		return builder.build();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean hasScreen() {
		return false;
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
		Boolean isPrimaryForSwordEnchants = null;
		Integer useDuration = null;
		Double piercingLevel = null;
		Boolean canSweep = null;
		Tier tier = null;
		Integer durability = null;
		Integer defense = null;
		Double toughness = null;
		Double armourKbRes = null;
		Ingredient ingredient = null;
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
			isPrimaryForSwordEnchants = oldData.isPrimaryForSwordEnchants;
			useDuration = oldData.useDuration;
			piercingLevel = oldData.piercingLevel;
			canSweep = oldData.canSweep;
			tier = oldData.tier;
			durability = oldData.durability;
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
			weapon_type = weapon_type.toLowerCase(Locale.ROOT);
			if (!registeredWeaponTypes.containsKey(weapon_type)) {
				CrashReport report = CrashReport.forThrowable(new JsonSyntaxException("The specified weapon type does not exist!"), "Applying Item Weapon Type");
				CrashReportCategory crashReportCategory = report.addCategory("Weapon Type being parsed");
				crashReportCategory.setDetail("Type name", weapon_type);
				crashReportCategory.setDetail("Json Object", jsonObject);
				throw new ReportedException(report);
			}
			type = WeaponType.fromID(weapon_type);
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
		if (jsonObject.has("durability")) {
			JsonElement durabilityE = jsonObject.get("durability");
			if (durabilityE instanceof JsonObject durabilityO) {
				if (!durabilityO.has("any"))
					throw new ReportedException(CrashReport.forThrowable(new JsonSyntaxException("The JSON must contain the integer 'any' when armour is configured!"), "Applying Item Durability"));
				if (item instanceof ArmorItem armorItem) {
					durability = getIntWithFallback(durabilityO, armorItem.getType().getName(), "any");
				} else durability = getInt(durabilityO, "any");
			} else durability = durabilityE.getAsInt();
		}
		if (jsonObject.has("sword_enchants_from_enchanting"))
			isPrimaryForSwordEnchants = getBoolean(jsonObject, "sword_enchants_from_enchanting");
		if (jsonObject.has("armor")) {
			JsonElement defenseE = jsonObject.get("armor");
			if (defenseE instanceof JsonObject defenseO) {
				if (!defenseO.has("any"))
					throw new ReportedException(CrashReport.forThrowable(new JsonSyntaxException("The JSON must contain the integer 'any' when armour is configured!"), "Applying Item Defense"));
				if (item instanceof ArmorItem armorItem) {
                    defense = getIntWithFallback(defenseO, armorItem.getType().getName(), "any");
				} else defense = getInt(defenseO, "any");
			} else defense = defenseE.getAsInt();
		}
		if (jsonObject.has("armor_toughness"))
			toughness = getDouble(jsonObject, "armor_toughness");
		if (jsonObject.has("armor_knockback_resistance"))
			armourKbRes = getDouble(jsonObject, "armor_knockback_resistance");
		if (jsonObject.has("repair_ingredient"))
			ingredient = Ingredient.of(itemFromName(getString(jsonObject, "repair_ingredient")));
		ConfigurableItemData configurableItemData = new ConfigurableItemData(damage, speed, reach, chargedReach, stack_size, cooldown, cooldownAfterUse, type, blockingType, blockStrength, blockKbRes, enchantment_level, isEnchantable, hasSwordEnchants, isPrimaryForSwordEnchants, useDuration, piercingLevel, canSweep, tier, durability, defense, toughness, armourKbRes, ingredient);
		configuredItems.put(item, configurableItemData);
	}
	public record Formula(String written) {
		public float armourCalcs(float amount, DamageSource damageSource, float armour, float armourToughness) {
			String armourFormula = written.split("enchant:", 2)[0];
			armourFormula = armourFormula.replaceAll("D", String.valueOf(amount)).replaceAll("P", String.valueOf(armour)).replaceAll("T", String.valueOf(armourToughness));
			float result = solveFormula(armourFormula);
			result = 1.0F - EnchantmentHelper.calculateArmorBreach(damageSource.getEntity(), result);
			return amount * result;
		}
		public float enchantCalcs(float amount, float enchantLevel) {
			String enchantFormula = written.split("enchant:", 2)[1];
			enchantFormula = enchantFormula.replaceAll("D", String.valueOf(amount)).replaceAll("E", String.valueOf(enchantLevel));
			float result = solveFormula(enchantFormula);
			return amount * result;
		}
		public float solveFormula(String formula) {
			if (!formula.contains("(") && !formula.contains("[") && !formula.contains("<") && !formula.contains("{"))
				return solveInner(formula);
			float res;
			String par = formula;
			while (par.contains("(") || par.contains("[") || par.contains("<") || par.contains("{")) {
				par = simplifyParenthesis(par, 0);
			}
			res = solveInner(par);
			return res;
		}
		public String simplifyParenthesis(String par, int recursion) {
			if (recursion > 10)
				throw new ReportedException(CrashReport.forThrowable(new IllegalStateException("Cannot have more than 10 operations inside of each other!"), "Handling armour calculations"));
			if (par.contains("min") || par.contains("max")) {
				boolean minB = par.lastIndexOf("min") > par.lastIndexOf("max") || !par.contains("max");
				if (minB) {
					String[] min = par.substring(par.lastIndexOf("min(") + 4, par.indexOf(")", par.lastIndexOf("min("))).split(",");
					float[] minf = new float[2];
					for (int i = 0; i < min.length; i++) {
						String minSt = min[i];
						while (minSt.contains("(") || minSt.contains("[") || minSt.contains("<") || minSt.contains("{")) {
							minSt = simplifyParenthesis(minSt, recursion + 1);
						}
						minf[i] = solveInner(minSt);
					}
					par = par.replace("min(" + min[0] + "," + min[1] + ")", String.valueOf(Math.min(minf[0], minf[1])));
				} else {
					String[] max = par.substring(par.lastIndexOf("max(") + 4, par.indexOf(")", par.lastIndexOf("max("))).split(",");
					float[] maxf = new float[2];
					for (int i = 0; i < max.length; i++) {
						String maxSt = max[i];
						while (maxSt.contains("(") || maxSt.contains("[") || maxSt.contains("<") || maxSt.contains("{")) {
							maxSt = simplifyParenthesis(maxSt, recursion + 1);
						}
						maxf[i] = solveInner(maxSt);
					}
					par = par.replace("max(" + max[0] + "," + max[1] + ")", String.valueOf(Math.max(maxf[0], maxf[1])));
				}
			} else if (par.contains("pow")) {
				String[] pow = par.substring(par.lastIndexOf("pow<") + 4, par.indexOf(">", par.lastIndexOf("pow<"))).split("\\^");
				float[] powf = new float[2];
				for (int i = 0; i < pow.length; i++) {
					String powSt = pow[i];
					while (powSt.contains("(") || powSt.contains("[") || powSt.contains("<") || powSt.contains("{")) {
						powSt = simplifyParenthesis(powSt, recursion + 1);
					}
					powf[i] = solveInner(powSt);
				}
				par = par.replace("pow<" + pow[0] + "^" + pow[1] + ">", String.valueOf(Math.pow(powf[0], powf[1])));
			} else if (par.contains("mul") || par.contains("div")) {
				boolean mulB = par.lastIndexOf("mul") > par.lastIndexOf("div") || !par.contains("div");
				if (mulB) {
					String[] mul = par.substring(par.lastIndexOf("mul[") + 4, par.indexOf("]", par.lastIndexOf("mul["))).split("\\*");
					float[] mulf = new float[2];
					for (int i = 0; i < mul.length; i++) {
						String mulSt = mul[i];
						while (mulSt.contains("(") || mulSt.contains("[") || mulSt.contains("<") || mulSt.contains("{")) {
							mulSt = simplifyParenthesis(mulSt, recursion + 1);
						}
						mulf[i] = solveInner(mulSt);
					}
					par = par.replace("mul[" + mul[0] + "*" + mul[1] + "]", String.valueOf(mulf[0] * mulf[1]));
				} else {
					String[] div = par.substring(par.lastIndexOf("div[") + 4, par.indexOf("]", par.lastIndexOf("div["))).split("/");
					float[] divf = new float[2];
					for (int i = 0; i < div.length; i++) {
						String divSt = div[i];
						while (divSt.contains("(") || divSt.contains("[") || divSt.contains("<") || divSt.contains("{")) {
							divSt = simplifyParenthesis(divSt, recursion + 1);
						}
						divf[i] = solveInner(divSt);
					}
					par = par.replace("div[" + div[0] + "/" + div[1] + "]", String.valueOf(divf[0] / divf[1]));
				}
			} else if (par.contains("add") || par.contains("sub")) {
				boolean addB = par.lastIndexOf("add") > par.lastIndexOf("sub") || !par.contains("sub");
				if (addB) {
					String[] add = par.substring(par.lastIndexOf("add{") + 4, par.indexOf("}", par.lastIndexOf("add{"))).split("\\+");
					float[] addf = new float[2];
					for (int i = 0; i < add.length; i++) {
						String addSt = add[i];
						while (addSt.contains("(") || addSt.contains("[") || addSt.contains("<") || addSt.contains("{")) {
							addSt = simplifyParenthesis(addSt, recursion + 1);
						}
						addf[i] = solveInner(addSt);
					}
					par = par.replace("add{" + add[0] + "+" + add[1] + "}", String.valueOf(addf[0] + addf[1]));
				} else {
					String[] sub = par.substring(par.lastIndexOf("sub{") + 4, par.indexOf("}", par.lastIndexOf("sub{"))).split("-");
					float[] subf = new float[2];
					for (int i = 0; i < sub.length; i++) {
						String subSt = sub[i];
						while (subSt.contains("(") || subSt.contains("[") || subSt.contains("<") || subSt.contains("{")) {
							subSt = simplifyParenthesis(subSt, recursion + 1);
						}
						subf[i] = solveInner(subSt);
					}
					par = par.replace("sub{" + sub[0] + "-" + sub[1] + "}", String.valueOf(subf[0] - subf[1]));
				}
			}
			return par;
		}
		public float solveInner(String par) {
			return Float.parseFloat(par);
		}
		public static int min(int val, int comp) {
			if (comp == -1 || comp == 0)
				comp = Integer.MAX_VALUE;
			if (val == -1 || val == 0)
				val = Integer.MAX_VALUE;
			return Math.min(val, comp);
		}
	}
}
