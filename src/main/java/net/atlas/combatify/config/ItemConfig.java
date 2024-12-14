package net.atlas.combatify.config;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.*;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.impl.builders.IntFieldBuilder;
import net.atlas.atlascore.AtlasCore;
import net.atlas.atlascore.config.AtlasConfig;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.ExtendedTier;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.item.CombatifyItemTags;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.BlockingType;
import net.atlas.combatify.util.MethodHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.mixin.item.ItemAccessor;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntFunction;

import static net.atlas.combatify.Combatify.*;
import static net.atlas.combatify.config.ConfigurableEntityData.ENTITY_DATA_STREAM_CODEC;
import static net.atlas.combatify.config.ConfigurableItemData.ITEM_DATA_STREAM_CODEC;
import static net.atlas.combatify.config.ConfigurableWeaponData.WEAPON_DATA_STREAM_CODEC;

public class ItemConfig extends AtlasConfig {
	public List<ConfigDataWrapper<EntityType<?>, ConfigurableEntityData>> configuredEntities;
	public List<ConfigDataWrapper<Item, ConfigurableItemData>> configuredItems;
	public List<ConfigDataWrapper<WeaponType, ConfigurableWeaponData>> configuredWeapons;
	public static final StreamCodec<RegistryFriendlyByteBuf, String> NAME_STREAM_CODEC = StreamCodec.of(RegistryFriendlyByteBuf::writeUtf, RegistryFriendlyByteBuf::readUtf);
	public static final StreamCodec<RegistryFriendlyByteBuf, Tier> TIERS_STREAM_CODEC = StreamCodec.of((buf, tier) -> {
		buf.writeVarInt(ExtendedTier.getLevel(tier));
		buf.writeVarInt(tier.getEnchantmentValue());
		buf.writeVarInt(tier.getUses());
		buf.writeFloat(tier.getAttackDamageBonus());
		buf.writeFloat(tier.getSpeed());
		Ingredient.CONTENTS_STREAM_CODEC.encode(buf, tier.getRepairIngredient());
		buf.writeResourceLocation(tier.getIncorrectBlocksForDrops().location());
	}, (buf) -> {
		int level = buf.readVarInt();
		int enchantLevel = buf.readVarInt();
		int uses = buf.readVarInt();
		float damage = buf.readFloat();
		float speed = buf.readFloat();
		Ingredient repairIngredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
		TagKey<Block> incorrect = TagKey.create(Registries.BLOCK, buf.readResourceLocation());
		return ExtendedTier.create(level, enchantLevel, uses, damage, speed, repairIngredient, incorrect);
	});
	public static final StreamCodec<RegistryFriendlyByteBuf, WeaponType> REGISTERED_WEAPON_TYPE_STREAM_CODEC = StreamCodec.of((buf, weaponType) -> {
		buf.writeUtf(weaponType.name());
		buf.writeDouble(weaponType.damageOffset());
		buf.writeDouble(weaponType.speed());
		buf.writeDouble(weaponType.reach());
		buf.writeBoolean(weaponType.useAxeDamage());
		buf.writeBoolean(weaponType.useHoeDamage());
		buf.writeBoolean(weaponType.useHoeSpeed());
		buf.writeBoolean(weaponType.tierable());
	}, buf -> {
		String name = buf.readUtf();
		double damageOffset = buf.readDouble();
		double speed = buf.readDouble();
		double reach = buf.readDouble();
		boolean useAxeDamage = buf.readBoolean();
		boolean useHoeDamage = buf.readBoolean();
		boolean useHoeSpeed = buf.readBoolean();
		boolean tierable = buf.readBoolean();
		return new WeaponType(name, damageOffset, speed, reach, useAxeDamage, useHoeDamage, useHoeSpeed, tierable);
	});
	public static final StreamCodec<RegistryFriendlyByteBuf, BlockingType> BLOCKING_TYPE_STREAM_CODEC = StreamCodec.of((buf, blockingType) -> {
		buf.writeUtf(blockingType.getClass().getName());
		buf.writeUtf(blockingType.getName());
		buf.writeBoolean(blockingType.canBeDisabled());
		buf.writeBoolean(blockingType.canBlockHit());
		buf.writeBoolean(blockingType.canCrouchBlock());
		buf.writeBoolean(blockingType.defaultKbMechanics());
		buf.writeBoolean(blockingType.isToolBlocker());
		buf.writeBoolean(blockingType.requireFullCharge());
		buf.writeBoolean(blockingType.requiresSwordBlocking());
		buf.writeBoolean(blockingType.hasDelay());
	}, buf -> {
		try {
			Class<?> clazz = BlockingType.class.getClassLoader().loadClass(buf.readUtf());
			Constructor<?> constructor = clazz.getConstructor(String.class);
			String name = buf.readUtf();
			Object object = constructor.newInstance(name);
			if (object instanceof BlockingType blockingType) {
				blockingType.setDisablement(buf.readBoolean());
				blockingType.setBlockHit(buf.readBoolean());
				blockingType.setCrouchable(buf.readBoolean());
				blockingType.setKbMechanics(buf.readBoolean());
				blockingType.setToolBlocker(buf.readBoolean());
				blockingType.setRequireFullCharge(buf.readBoolean());
				blockingType.setSwordBlocking(buf.readBoolean());
				blockingType.setDelay(buf.readBoolean());
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
	public static final StreamCodec<? super FriendlyByteBuf, ConfigDataWrapper<Item, ConfigurableItemData>> ITEM_WRAPPER_STREAM_CODEC = StreamCodec.of((buf, wrapper) -> {
		buf.writeCollection(wrapper.objects, (buf1, item) -> buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(item)));
		buf.writeCollection(wrapper.tagKeys, (buf1, tagKey) -> buf.writeResourceLocation(tagKey.location()));
		ITEM_DATA_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, wrapper.configurableData);
	}, buf -> {
		List<Item> items = buf.readList(buf1 -> BuiltInRegistries.ITEM.get(buf1.readResourceLocation()));
		List<TagKey<Item>> tags = buf.readList(buf1 -> TagKey.create(Registries.ITEM, buf1.readResourceLocation()));
		ConfigurableItemData configurableItemData = ITEM_DATA_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf);
		return new ConfigDataWrapper<>(items, tags, configurableItemData);
	});
	public static final StreamCodec<? super FriendlyByteBuf, ConfigDataWrapper<EntityType<?>, ConfigurableEntityData>> ENTITY_WRAPPER_STREAM_CODEC = StreamCodec.of((buf, wrapper) -> {
		buf.writeCollection(wrapper.objects, (buf1, item) -> buf.writeResourceLocation(BuiltInRegistries.ENTITY_TYPE.getKey(item)));
		buf.writeCollection(wrapper.tagKeys, (buf1, tagKey) -> buf.writeResourceLocation(tagKey.location()));
		ENTITY_DATA_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, wrapper.configurableData);
	}, buf -> {
		List<EntityType<?>> items = buf.readList(buf1 -> BuiltInRegistries.ENTITY_TYPE.get(buf1.readResourceLocation()));
		List<TagKey<EntityType<?>>> tags = buf.readList(buf1 -> TagKey.create(Registries.ENTITY_TYPE, buf1.readResourceLocation()));
		ConfigurableEntityData configurableEntityData = ENTITY_DATA_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf);
		return new ConfigDataWrapper<>(items, tags, configurableEntityData);
	});
	public static final StreamCodec<? super FriendlyByteBuf, ConfigDataWrapper<WeaponType, ConfigurableWeaponData>> WEAPON_WRAPPER_STREAM_CODEC = StreamCodec.of((buf, wrapper) -> {
		buf.writeCollection(wrapper.objects, WeaponType.STREAM_CODEC);
		WEAPON_DATA_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, wrapper.configurableData);
	}, buf -> {
		List<WeaponType> items = buf.readList(WeaponType.STREAM_CODEC);
		ConfigurableWeaponData configurableEntityData = WEAPON_DATA_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf);
		return new ConfigDataWrapper<>(items, Collections.emptyList(), configurableEntityData);
	});
	public static Formula armourCalcs = null;

	public ItemConfig() {
		super(id("combatify-items"));
		modify();
	}

	public static Formula getArmourCalcs() {
		return armourCalcs;
	}

	@Override
	public void reload() {
		super.reload();
		modify();
	}

	@Override
	public void reloadFromDefault() {
		super.reloadFromDefault();
		modify();
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
		if (!object.has("entities"))
			object.add("entities", new JsonArray());
		JsonElement entities = object.get("entities");
		if (tiers instanceof JsonArray typeArray) {
			typeArray.asList().forEach(jsonElement -> {
				if (jsonElement instanceof JsonObject jsonObject) {
					parseTiers(jsonObject);
				} else
					notJSONObject(jsonElement, "Configuring Tiers");
			});
		}
		if (defenders instanceof JsonArray typeArray) {
			typeArray.asList().forEach(jsonElement -> {
				if (jsonElement instanceof JsonObject jsonObject) {
					parseBlockingType(jsonObject);
				} else
					notJSONObject(jsonElement, "Configuring Blocking Types");
			});
		}
		if (weapons instanceof JsonArray typeArray) {
			typeArray.asList().forEach(jsonElement -> {
				if (jsonElement instanceof JsonObject jsonObject) {
					List<WeaponType> weaponTypes;
					if (jsonObject.get("name") instanceof JsonArray itemsWithConfig) weaponTypes = itemsWithConfig.asList().stream().map(weaponName -> typeFromName(weaponName.getAsString())).toList();
					else weaponTypes = Collections.singletonList(typeFromJson(jsonObject));

					List<WeaponType> blankTypes = new ArrayList<>(weaponTypes.stream().filter(Objects::isNull).toList());
					int added = blankTypes.size();
					blankTypes.clear();
					for (int i = 0; i < added; i++) {
						WeaponType weaponType = WeaponType.FULL_CODEC.parse(JsonOps.INSTANCE, jsonObject).getOrThrow();
						blankTypes.add(weaponType);
					}
					if (!blankTypes.isEmpty()) {
						weaponTypes = weaponTypes.stream().filter(Objects::nonNull).toList();
						parseWeaponType(blankTypes, jsonObject, ConfigurableWeaponData.ADDED_TYPE_CODEC);
					}
					parseWeaponType(weaponTypes, jsonObject, ConfigurableWeaponData.CODEC);
				} else
					notJSONObject(jsonElement, "Configuring Weapon Types");
			});
		}
		if (items instanceof JsonArray itemArray) {
			itemArray.asList().forEach(jsonElement -> {
				if (jsonElement instanceof JsonObject jsonObject) {
					List<Item> itemList = Collections.emptyList();
					List<TagKey<Item>> tagsList = Collections.emptyList();
					if (jsonObject.has("name")) {
						if (jsonObject.get("name") instanceof JsonArray itemsWithConfig)
							itemList = itemsWithConfig.asList().stream().map(itemName -> itemFromName(itemName.getAsString())).toList();
						else itemList = Collections.singletonList(itemFromJson(jsonObject));
					}
					if (jsonObject.has("tag")) {
						if (jsonObject.get("tag") instanceof JsonArray itemsWithConfig) tagsList = itemsWithConfig.asList().stream().map(itemName -> itemTagFromName(itemName.getAsString())).toList();
						else tagsList = Collections.singletonList(itemTagFromJson(jsonObject));
					}
					if (!(jsonObject.has("name") || jsonObject.has("tag"))) {
						noNamePresent(jsonElement, "Configuring Items");
						return;
					}
					parseItemConfig(itemList, tagsList, jsonObject);
				} else
					notJSONObject(jsonElement, "Configuring Items");
			});
		}
		if (entities instanceof JsonArray entityArray) {
			entityArray.asList().forEach(jsonElement -> {
				if (jsonElement instanceof JsonObject jsonObject) {
					List<? extends EntityType<?>> entityList = Collections.emptyList();
					List<TagKey<EntityType<?>>> tagsList = Collections.emptyList();
					if (jsonObject.has("name")) {
						if (jsonObject.get("name") instanceof JsonArray entitiesWithConfig)
							entityList = entitiesWithConfig.asList().stream().map(entityName -> BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse(entityName.getAsString()))).toList();
						else entityList = Collections.singletonList(entityFromJson(jsonObject));
					}
					if (jsonObject.has("tag")) {
						if (jsonObject.get("tag") instanceof JsonArray entitiesWithConfig) tagsList = entitiesWithConfig.asList().stream().map(entityTagName -> entityTagFromName(entityTagName.getAsString())).toList();
						else tagsList = Collections.singletonList(entityTagFromJson(jsonObject));
					}
					if (!(jsonObject.has("name") || jsonObject.has("tag"))) {
						noNamePresent(jsonElement, "Configuring Entities");
						return;
					}
					parseEntityType((List<EntityType<?>>) entityList, tagsList, jsonObject);
				} else
					notJSONObject(jsonElement, "Configuring Entities");
			});
		}
		if (object.has("armor_calculation"))
			armourCalcs = new Formula(getString(object, "armor_calculation"));
	}
	public void parseEntityType(List<EntityType<?>> entities, List<TagKey<EntityType<?>>> entityTags, JsonObject jsonObject) {
		ConfigurableEntityData configurableEntityData = ConfigurableEntityData.CODEC.parse(JsonOps.INSTANCE, jsonObject).getOrThrow();
		ConfigDataWrapper<EntityType<?>, ConfigurableEntityData> configDataWrapper = new ConfigDataWrapper<>(entities, entityTags, configurableEntityData);
		configuredEntities.add(configDataWrapper);
	}
	public void parseWeaponType(List<WeaponType> types, JsonObject jsonObject, Codec<ConfigurableWeaponData> codec) {
		ConfigurableWeaponData configurableWeaponData = codec.parse(JsonOps.INSTANCE, jsonObject).getOrThrow();
		addWeaponTypeConfiguration(types, configurableWeaponData);
	}
	public void addWeaponTypeConfiguration(List<WeaponType> types, ConfigurableWeaponData configurableWeaponData) {
		ConfigDataWrapper<WeaponType, ConfigurableWeaponData> configDataWrapper = new ConfigDataWrapper<>(types, Collections.emptyList(), configurableWeaponData);
		configuredWeapons.add(configDataWrapper);
	}
	@Override
	protected InputStream getDefaultedConfig() {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream("combatify-items.json");
	}

	@Override
	public void defineConfigHolders() {
		configuredEntities = new ArrayList<>();
		configuredItems = new ArrayList<>();
		configuredWeapons = new ArrayList<>();
	}

	@Override
	public void resetExtraHolders() {
		configuredEntities = new ArrayList<>();
		configuredItems = new ArrayList<>();
		configuredWeapons = new ArrayList<>();
		tiers = HashBiMap.create(defaultTiers);
		registeredWeaponTypes = new HashMap<>(defaultWeaponTypes);
		registeredTypes = new HashMap<>(defaultTypes);
	}

	@Override
	public <T> void alertChange(ConfigValue<T> tConfigValue, T newValue) {

	}

	public static TagKey<EntityType<?>> entityTagFromJson(JsonObject jsonObject) {
		return entityTagFromName(GsonHelper.getAsString(jsonObject, "tag"));
	}

	public static TagKey<EntityType<?>> entityTagFromName(String string) {
		return TagKey.create(Registries.ENTITY_TYPE, Objects.requireNonNull(ResourceLocation.tryParse(stripHexStarter(string))));
	}

	public static EntityType<?> entityFromJson(JsonObject jsonObject) {
		return BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.tryParse(GsonHelper.getAsString(jsonObject, "name"))).orElse(null);
	}

	public static TagKey<Item> itemTagFromJson(JsonObject jsonObject) {
		return itemTagFromName(GsonHelper.getAsString(jsonObject, "tag"));
	}

	public static TagKey<Item> itemTagFromName(String string) {
		return TagKey.create(Registries.ITEM, Objects.requireNonNull(ResourceLocation.tryParse(stripHexStarter(string))));
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
		return typeFromName(weapon_type);
	}
	public static WeaponType typeFromName(String name) {
		if (!registeredWeaponTypes.containsKey(name))
			return null;
		return WeaponType.fromID(name);
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
		if (!jsonObject.has("name")) {
			LOGGER.error("An added tier does not possess a name. This is due to an incorrectly written config file." + errorStage("Configuring Tiers"));
			return;
		}
		String name = getString(jsonObject, "name");
		tiers.put(name, ExtendedTier.CODEC.parse(JsonOps.INSTANCE, jsonObject).getOrThrow());
	}

	public ItemConfig loadFromNetwork(RegistryFriendlyByteBuf buf) {
		super.loadFromNetwork(buf);
		tiers = HashBiMap.create(readMap(buf, NAME_STREAM_CODEC, TIERS_STREAM_CODEC));
		registeredWeaponTypes = readMap(buf, NAME_STREAM_CODEC, REGISTERED_WEAPON_TYPE_STREAM_CODEC);
		registeredTypes = readMap(buf, NAME_STREAM_CODEC, BLOCKING_TYPE_STREAM_CODEC);
		configuredEntities = buf.readList(ENTITY_WRAPPER_STREAM_CODEC);
		configuredItems = buf.readList(ITEM_WRAPPER_STREAM_CODEC);
		configuredWeapons = buf.readList(WEAPON_WRAPPER_STREAM_CODEC);
		String formula = buf.readUtf();
		if (!formula.equals("empty"))
			armourCalcs = new Formula(formula);
		return this;
	}

	public static Tier getTier(String s) {
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
	public static String getTierName(Tier tier) {
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
	private static Tier getTierRaw(String s) {
		if (!tiers.containsKey(s) || Objects.equals(s, "empty")) return null;
		return tiers.get(s);
	}

	public void saveToNetwork(RegistryFriendlyByteBuf buf) {
		super.saveToNetwork(buf);
		writeMap(buf, tiers, NAME_STREAM_CODEC, TIERS_STREAM_CODEC);
		writeMap(buf, registeredWeaponTypes, NAME_STREAM_CODEC, REGISTERED_WEAPON_TYPE_STREAM_CODEC);
		writeMap(buf, Combatify.registeredTypes, NAME_STREAM_CODEC, BLOCKING_TYPE_STREAM_CODEC);
		buf.writeCollection(configuredEntities, ENTITY_WRAPPER_STREAM_CODEC);
		buf.writeCollection(configuredItems, ITEM_WRAPPER_STREAM_CODEC);
		buf.writeCollection(configuredWeapons, WEAPON_WRAPPER_STREAM_CODEC);
		buf.writeUtf(armourCalcs == null ? "empty" : armourCalcs.written);
	}

	public static <B extends FriendlyByteBuf, K, V> Map<K, V> readMap(B buf, StreamCodec<B, K> keyCodec, StreamCodec<B, V> valueCodec) {
		return readMap(buf, Maps::newHashMapWithExpectedSize, keyCodec, valueCodec);
	}

	public static <B extends FriendlyByteBuf, K, V, M extends Map<K, V>> M readMap(B buf, IntFunction<M> intFunction, StreamCodec<B, K> keyCodec, StreamCodec<B, V> valueCodec) {
		int size = buf.readVarInt();
		M map = intFunction.apply(size);

		for (int index = 0; index < size; ++index) {
			K key = keyCodec.decode(buf);
			V value = valueCodec.decode(buf);
			map.put(key, value);
		}

		return map;
	}

	public static <B extends FriendlyByteBuf, K, V> void writeMap(B buf, Map<K, V> map, StreamCodec<B, K> keyCodec, StreamCodec<B, V> valueCodec) {
		buf.writeVarInt(map.size());
		map.forEach((key, value) -> {
			keyCodec.encode(buf, key);
			valueCodec.encode(buf, value);
		});
	}

	@Override
	public void saveExtra(JsonWriter jsonWriter, PrintWriter printWriter) {

	}

	@Override
	public void handleExtraSync(AtlasCore.AtlasConfigPacket packet, LocalPlayer player, PacketSender sender) {
		if (CONFIG.enableDebugLogging())
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
		IntFieldBuilder size = new IntFieldBuilder(Component.translatable("text.config.combatify-general.reset"),
			Component.translatable("text.config.combatify-items.size"),
			configuredItems.size());
		size.setMin(0).setSaveConsumer((newSize) -> {
			int oldSize = configuredItems.size();
			if (oldSize < newSize) {
				for (int i = oldSize; i < newSize; i++) {
					configuredItems.add(i, ConfigDataWrapper.EMPTY_ITEM);
				}
			} else if (oldSize > newSize) {
                configuredItems.subList(newSize, oldSize).clear();
			}
		});
		configCategory.addEntry(size.build());
		builder.setFallbackCategory(configCategory);

		return builder.build();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean hasScreen() {
		return false;
	}

	public void parseItemConfig(List<Item> items, List<TagKey<Item>> tags, JsonObject jsonObject) {
		ConfigurableItemData configurableItemData = ConfigurableItemData.CODEC.parse(JsonOps.INSTANCE, jsonObject).getOrThrow();
		ConfigDataWrapper<Item, ConfigurableItemData> configDataWrapper = new ConfigDataWrapper<>(items, tags, configurableItemData);
		configuredItems.add(configDataWrapper);
	}
	public static void noNamePresent(JsonElement invalid, String stage) {
		LOGGER.error("No name is present: " + invalid + ", no changes will occur. This may be due to an incorrectly written config file. " + errorStage(stage));
	}
	public static void notJSONObject(JsonElement invalid, String stage) {
		LOGGER.error("Not a JSON Object: " + invalid + " this may be due to an incorrectly written config file. " + errorStage(stage));
	}
	public static String errorStage(String stage) {
		return "[Config Stage]: " + stage;
	}
	@SuppressWarnings("all")
	public void modify() {
		for (Item item : BuiltInRegistries.ITEM) {
			DataComponentMap.Builder builder = DataComponentMap.builder().addAll(item.components());
			boolean damageOverridden = false;
			ConfigurableItemData configurableItemData = MethodHandler.forItem(item);
			boolean isConfiguredItem = configurableItemData != null;
			if (isConfiguredItem) {
				Integer durability = configurableItemData.armourStats().durability().getValue(item);
				Integer maxStackSize = configurableItemData.stackSize();
				Tier tier = configurableItemData.tier();
				TagKey<Block> mineable = configurableItemData.toolMineableTag();
				Tool tool = configurableItemData.tool();
				if (maxStackSize != null)
					builder.set(DataComponents.MAX_STACK_SIZE, maxStackSize);
				if (durability != null) {
					setDurability(builder, item, durability);
					damageOverridden = true;
				}
				if (tool != null) builder.set(DataComponents.TOOL, tool);
				else if (tier != null && mineable != null) builder.set(DataComponents.TOOL, tier.createToolProperties(mineable));
			}
			if (!damageOverridden && ((ItemExtensions)item).getTierFromConfig().isPresent()) {
				int value = ((ItemExtensions) item).getTierFromConfig().get().getUses();
				if (item.builtInRegistryHolder().is(CombatifyItemTags.DOUBLE_TIER_DURABILITY))
					value *= 2;
				setDurability(builder, item, value);
			}
			updateModifiers(builder, item, isConfiguredItem, configurableItemData);
			((ItemAccessor) item).setComponents(builder.build());
		}
	}
	@SuppressWarnings("ALL")
	public void updateModifiers(DataComponentMap.Builder builder, Item item, boolean isConfiguredItem, @Nullable ConfigurableItemData configurableItemData) {
		ItemAttributeModifiers modifier = item.components().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
		ItemAttributeModifiers def = item.getDefaultAttributeModifiers();
		ItemAttributeModifiers original = originalModifiers.get(item);
		if (modifier.equals(ItemAttributeModifiers.EMPTY) && !def.equals(ItemAttributeModifiers.EMPTY))
			modifier = def;
		if (!original.equals(ItemAttributeModifiers.EMPTY))
			modifier = original;
		modifier = ((ItemExtensions)item).modifyAttributeModifiers(modifier);
		if (modifier != null) {
			modifyFromItemConfig: {
				if (isConfiguredItem) {
					if (!configurableItemData.itemAttributeModifiers().equals(ItemAttributeModifiers.EMPTY)) {
						modifier = configurableItemData.itemAttributeModifiers();
						break modifyFromItemConfig;
					}
					if (configurableItemData.weaponStats().weaponType() != null) {
						ItemAttributeModifiers.Builder itemAttributeBuilder = ItemAttributeModifiers.builder();
						configurableItemData.weaponStats().weaponType().addCombatAttributes(((ItemExtensions) item).getConfigTier(), itemAttributeBuilder);
						modifier.modifiers().forEach(entry -> {
							boolean bl = entry.attribute().is(Attributes.ATTACK_DAMAGE)
								|| entry.attribute().is(Attributes.ATTACK_SPEED)
								|| entry.attribute().is(Attributes.ENTITY_INTERACTION_RANGE);
							if (!bl)
								itemAttributeBuilder.add(entry.attribute(), entry.modifier(), entry.slot());
						});
						modifier = itemAttributeBuilder.build();
					}
					ItemAttributeModifiers.Builder itemAttributeBuilder = ItemAttributeModifiers.builder();
					boolean modDamage = false;
					AtomicReference<ItemAttributeModifiers.Entry> damage = new AtomicReference<>();
					boolean modSpeed = false;
					AtomicReference<ItemAttributeModifiers.Entry> speed = new AtomicReference<>();
					boolean modReach = false;
					AtomicReference<ItemAttributeModifiers.Entry> reach = new AtomicReference<>();
					boolean modDefense = false;
					AtomicReference<ItemAttributeModifiers.Entry> defense = new AtomicReference<>();
					boolean modToughness = false;
					AtomicReference<ItemAttributeModifiers.Entry> toughness = new AtomicReference<>();
					boolean modKnockbackResistance = false;
					AtomicReference<ItemAttributeModifiers.Entry> knockbackResistance = new AtomicReference<>();
					def.modifiers().forEach(entry -> {
						if (entry.attribute().is(Attributes.ATTACK_DAMAGE))
							damage.set(entry);
						else if (entry.attribute().is(Attributes.ENTITY_INTERACTION_RANGE))
							reach.set(entry);
						else if (entry.attribute().is(Attributes.ATTACK_SPEED))
							speed.set(entry);
						else if (entry.attribute().is(Attributes.ARMOR))
							defense.set(entry);
						else if (entry.attribute().is(Attributes.ARMOR_TOUGHNESS))
							toughness.set(entry);
						else if (entry.attribute().is(Attributes.KNOCKBACK_RESISTANCE))
							knockbackResistance.set(entry);
						else
							itemAttributeBuilder.add(entry.attribute(), entry.modifier(), entry.slot());
					});
					modifier.modifiers().forEach(entry -> {
						if (entry.attribute().is(Attributes.ATTACK_DAMAGE))
							damage.set(entry);
						else if (entry.attribute().is(Attributes.ENTITY_INTERACTION_RANGE))
							reach.set(entry);
						else if (entry.attribute().is(Attributes.ATTACK_SPEED))
							speed.set(entry);
						else if (entry.attribute().is(Attributes.ARMOR))
							defense.set(entry);
						else if (entry.attribute().is(Attributes.ARMOR_TOUGHNESS))
							toughness.set(entry);
						else if (entry.attribute().is(Attributes.KNOCKBACK_RESISTANCE))
							knockbackResistance.set(entry);
						else
							itemAttributeBuilder.add(entry.attribute(), entry.modifier(), entry.slot());
					});
					if (configurableItemData.weaponStats().attackDamage() != null) {
						modDamage = true;
						itemAttributeBuilder.add(Attributes.ATTACK_DAMAGE,
							new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, configurableItemData.weaponStats().attackDamage() - (CONFIG.fistDamage() ? 1 : 2), AttributeModifier.Operation.ADD_VALUE),
							EquipmentSlotGroup.MAINHAND);
					}
					if (!modDamage && damage.get() != null)
						itemAttributeBuilder.add(damage.get().attribute(), damage.get().modifier(), damage.get().slot());
					if (configurableItemData.weaponStats().attackSpeed() != null) {
						modSpeed = true;
						itemAttributeBuilder.add(Attributes.ATTACK_SPEED,
							new AttributeModifier(WeaponType.BASE_ATTACK_SPEED_CTS_ID, configurableItemData.weaponStats().attackSpeed() - CONFIG.baseHandAttackSpeed(), AttributeModifier.Operation.ADD_VALUE),
							EquipmentSlotGroup.MAINHAND);
					}
					if (!modSpeed && speed.get() != null)
						itemAttributeBuilder.add(speed.get().attribute(), speed.get().modifier(), speed.get().slot());
					if (configurableItemData.weaponStats().attackReach() != null) {
						modReach = true;
						itemAttributeBuilder.add(Attributes.ENTITY_INTERACTION_RANGE,
							new AttributeModifier(WeaponType.BASE_ATTACK_REACH_ID, configurableItemData.weaponStats().attackReach() - 2.5, AttributeModifier.Operation.ADD_VALUE),
							EquipmentSlotGroup.MAINHAND);
					}
					if (!modReach && reach.get() != null)
						itemAttributeBuilder.add(reach.get().attribute(), reach.get().modifier(), reach.get().slot());
					ResourceLocation resourceLocation = ResourceLocation.withDefaultNamespace("armor.any");
					EquipmentSlotGroup slotGroup = EquipmentSlotGroup.ARMOR;
					if (item instanceof Equipable equipable)
						slotGroup = EquipmentSlotGroup.bySlot(equipable.getEquipmentSlot());
					resourceLocation = switch (slotGroup) {
						case HEAD -> ResourceLocation.withDefaultNamespace("armor.helmet");
						case CHEST -> ResourceLocation.withDefaultNamespace("armor.chestplate");
						case LEGS -> ResourceLocation.withDefaultNamespace("armor.leggings");
						case FEET -> ResourceLocation.withDefaultNamespace("armor.boots");
						case BODY -> ResourceLocation.withDefaultNamespace("armor.body");
						default -> resourceLocation;
					};
					if (configurableItemData.armourStats().defense().getValue(item) != null) {
						modDefense = true;
						itemAttributeBuilder.add(Attributes.ARMOR,
							new AttributeModifier(resourceLocation, configurableItemData.armourStats().defense().getValue(item), AttributeModifier.Operation.ADD_VALUE),
							slotGroup);
					}
					if (!modDefense && defense.get() != null)
						itemAttributeBuilder.add(defense.get().attribute(), defense.get().modifier(), defense.get().slot());
					if (configurableItemData.armourStats().toughness() != null) {
						modToughness = true;
						itemAttributeBuilder.add(Attributes.ARMOR_TOUGHNESS,
							new AttributeModifier(resourceLocation, configurableItemData.armourStats().toughness(), AttributeModifier.Operation.ADD_VALUE),
							slotGroup);
					}
					if (!modToughness && toughness.get() != null)
						itemAttributeBuilder.add(toughness.get().attribute(), toughness.get().modifier(), toughness.get().slot());
					if (configurableItemData.armourStats().armourKbRes() != null) {
						modKnockbackResistance = true;
						if (configurableItemData.armourStats().armourKbRes() > 0)
							itemAttributeBuilder.add(Attributes.KNOCKBACK_RESISTANCE,
								new AttributeModifier(resourceLocation, configurableItemData.armourStats().armourKbRes(), AttributeModifier.Operation.ADD_VALUE),
								slotGroup);
					}
					if (!modKnockbackResistance && knockbackResistance.get() != null)
						itemAttributeBuilder.add(knockbackResistance.get().attribute(), knockbackResistance.get().modifier(), knockbackResistance.get().slot());
					if (modDamage || modSpeed || modReach || modDefense || modToughness || modKnockbackResistance)
						modifier = itemAttributeBuilder.build();
				}
			}
		}
		builder.set(DataComponents.ATTRIBUTE_MODIFIERS, modifier);
	}
	public record ConfigDataWrapper<T, U>(List<T> objects, List<TagKey<T>> tagKeys, U configurableData) {
		public static final ConfigDataWrapper<Item, ConfigurableItemData> EMPTY_ITEM = new ConfigDataWrapper<>(Collections.emptyList(), Collections.emptyList(), ConfigurableItemData.EMPTY);
		private boolean matches(Holder<T> test) {
			return objects.contains(test.value()) || tagKeys.stream().anyMatch(test::is);
		}
		private boolean matches(T test) {
			return objects.contains(test);
		}
		public U match(Holder<T> test) {
			if (matches(test)) {
				return configurableData;
			}
			return null;
		}
		public U match(T test) {
			if (matches(test)) {
				return configurableData;
			}
			return null;
		}
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof ConfigDataWrapper<?, ?> that)) return false;
            return Objects.equals(objects, that.objects) && Objects.equals(tagKeys, that.tagKeys) && Objects.equals(configurableData, that.configurableData);
		}

		@Override
		public int hashCode() {
			return Objects.hash(objects, tagKeys, configurableData);
		}
	}
	public record Formula(String written) {
		public float armourCalcs(float amount, DamageSource damageSource, float armour, float armourToughness) {
			String armourFormula = written.split("enchant:", 2)[0];
			armourFormula = armourFormula.replaceAll("D", String.valueOf(amount)).replaceAll("P", String.valueOf(armour)).replaceAll("T", String.valueOf(armourToughness));
			float result = solveFormula(armourFormula);
			ItemStack itemStack = damageSource.getWeaponItem();
			if (itemStack != null && damageSource.getEntity().level() instanceof ServerLevel serverLevel)
				result = 1 - Mth.clamp(EnchantmentHelper.modifyArmorEffectiveness(serverLevel, itemStack, damageSource.getEntity(), damageSource, result), 0.0F, 1.0F);
			else result = 1 - result;
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
