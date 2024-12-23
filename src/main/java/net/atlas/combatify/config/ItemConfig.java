package net.atlas.combatify.config;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.impl.builders.IntFieldBuilder;
import net.atlas.atlascore.AtlasCore;
import net.atlas.atlascore.config.AtlasConfig;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.component.CustomDataComponents;
import net.atlas.combatify.extensions.Tier;
import net.atlas.combatify.extensions.ToolMaterialWrapper;
import net.atlas.combatify.item.CombatifyItemTags;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.BlockingType;
import net.atlas.combatify.util.MethodHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.mixin.item.ItemAccessor;
import net.minecraft.CrashReport;
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
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.IntFunction;

import static net.atlas.combatify.Combatify.*;
import static net.atlas.combatify.config.ConfigurableEntityData.ENTITY_DATA_STREAM_CODEC;
import static net.atlas.combatify.config.ConfigurableItemData.ITEM_DATA_STREAM_CODEC;
import static net.atlas.combatify.config.ConfigurableWeaponData.WEAPON_DATA_STREAM_CODEC;

public class ItemConfig extends AtlasConfig {
	public boolean isModifying = false;
	public List<ConfigDataWrapper<EntityType<?>, ConfigurableEntityData>> configuredEntities;
	public List<ConfigDataWrapper<Item, ConfigurableItemData>> configuredItems;
	public List<ConfigDataWrapper<WeaponType, ConfigurableWeaponData>> configuredWeapons;
	public static final StreamCodec<RegistryFriendlyByteBuf, String> NAME_STREAM_CODEC = StreamCodec.of(RegistryFriendlyByteBuf::writeUtf, RegistryFriendlyByteBuf::readUtf);
	public static final StreamCodec<RegistryFriendlyByteBuf, Tier> TIERS_STREAM_CODEC = StreamCodec.of((buf, tier) -> {
		buf.writeFloat(tier.combatify$blockingLevel());
		buf.writeVarInt(tier.combatify$weaponLevel());
		buf.writeVarInt(tier.enchantmentValue());
		buf.writeVarInt(tier.durability());
		buf.writeFloat(tier.attackDamageBonus());
		buf.writeFloat(tier.speed());
		buf.writeResourceLocation(tier.repairItems().location());
		buf.writeResourceLocation(tier.incorrectBlocksForDrops().location());
	}, (buf) -> {
		float blockingLevel = buf.readFloat();
		int weaponLevel = buf.readVarInt();
		int enchantLevel = buf.readVarInt();
		int uses = buf.readVarInt();
		float damage = buf.readFloat();
		float speed = buf.readFloat();
		TagKey<Item> repairItems = TagKey.create(Registries.ITEM, buf.readResourceLocation());
		TagKey<Block> incorrect = TagKey.create(Registries.BLOCK, buf.readResourceLocation());
		return ToolMaterialWrapper.create(blockingLevel, weaponLevel, enchantLevel, uses, damage, speed, repairItems, incorrect);
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
		buf.writeResourceLocation(Combatify.registeredTypeFactories.inverse().get(blockingType.factory()));
		buf.writeUtf(blockingType.getName());
		buf.writeBoolean(blockingType.canCrouchBlock());
		buf.writeBoolean(blockingType.canBlockHit());
		buf.writeBoolean(blockingType.canBeDisabled());
		buf.writeBoolean(blockingType.requireFullCharge());
		buf.writeBoolean(blockingType.defaultKbMechanics());
		buf.writeBoolean(blockingType.hasDelay());
	}, buf -> {
		BlockingType.Factory<?> factory = registeredTypeFactories.get(buf.readResourceLocation());
		String name = buf.readUtf();
		boolean canCrouchBlock = buf.readBoolean();
		boolean canBlockHit = buf.readBoolean();
		boolean canDisable = buf.readBoolean();
		boolean requireFullCharge = buf.readBoolean();
		boolean defaultKbMechanics = buf.readBoolean();
		boolean hasDelay = buf.readBoolean();
		return factory.create(name, canCrouchBlock, canBlockHit, canDisable, requireFullCharge, defaultKbMechanics, hasDelay);
	});
	public static final StreamCodec<? super FriendlyByteBuf, ConfigDataWrapper<Item, ConfigurableItemData>> ITEM_WRAPPER_STREAM_CODEC = StreamCodec.of((buf, wrapper) -> {
		buf.writeCollection(wrapper.objects, (buf1, item) -> buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(item)));
		buf.writeCollection(wrapper.tagKeys, (buf1, tagKey) -> buf.writeResourceLocation(tagKey.location()));
		ITEM_DATA_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, wrapper.configurableData);
	}, buf -> {
		List<Item> items = buf.readList(buf1 -> BuiltInRegistries.ITEM.get(buf1.readResourceLocation()).get().value());
		List<TagKey<Item>> tags = buf.readList(buf1 -> TagKey.create(Registries.ITEM, buf1.readResourceLocation()));
		ConfigurableItemData configurableItemData = ITEM_DATA_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf);
		return new ConfigDataWrapper<>(items, tags, configurableItemData);
	});
	public static final StreamCodec<? super FriendlyByteBuf, ConfigDataWrapper<EntityType<?>, ConfigurableEntityData>> ENTITY_WRAPPER_STREAM_CODEC = StreamCodec.of((buf, wrapper) -> {
		buf.writeCollection(wrapper.objects, (buf1, item) -> buf.writeResourceLocation(BuiltInRegistries.ENTITY_TYPE.getKey(item)));
		buf.writeCollection(wrapper.tagKeys, (buf1, tagKey) -> buf.writeResourceLocation(tagKey.location()));
		ENTITY_DATA_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, wrapper.configurableData);
	}, buf -> {
		List<EntityType<?>> items = buf.readList(buf1 -> BuiltInRegistries.ENTITY_TYPE.get(buf1.readResourceLocation()).get().value());
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
	public static final Codec<Holder<EntityType<?>>> ENTITY_TYPE_CODEC = BuiltInRegistries.ENTITY_TYPE.holderByNameCodec();
	public static Codec<BiMap<String, ToolMaterialWrapper>> TIERS_CODEC = Codec.unboundedMap(Codec.STRING, ToolMaterialWrapper.CODEC).xmap(HashBiMap::create, Function.identity());

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
		isModifying = true;
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
			object.add("tiers", new JsonObject());
		JsonElement tiers = object.get("tiers");
		if (!object.has("entities"))
			object.add("entities", new JsonArray());
		JsonElement entities = object.get("entities");
		Combatify.tiers = HashBiMap.create(Combatify.defaultTiers);
		Combatify.tiers.putAll(TIERS_CODEC.parse(JsonOps.INSTANCE, tiers).getOrThrow());
		registeredTypes = new HashMap<>(defaultTypes);
		List<BlockingType> altered = BlockingType.CODEC.listOf().orElse(Collections.emptyList()).parse(JsonOps.INSTANCE, defenders).getOrThrow();
		altered.forEach(Combatify::registerBlockingType);
		if (weapons instanceof JsonArray typeArray) {
			typeArray.asList().forEach(jsonElement -> {
				if (jsonElement instanceof JsonObject jsonObject) {
					List<WeaponType> weaponTypes = Codec.withAlternative(Codec.withAlternative(WeaponType.STRICT_CODEC.listOf(), WeaponType.STRICT_CODEC, Collections::singletonList).fieldOf("name").codec(), Codec.withAlternative(WeaponType.FULL_CODEC.listOf(), WeaponType.FULL_CODEC, Collections::singletonList)).parse(JsonOps.INSTANCE, jsonObject).getOrThrow();
					parseWeaponType(weaponTypes, jsonObject);
				} else
					notJSONObject(jsonElement, "Configuring Weapon Types");
			});
		}
		if (items instanceof JsonArray itemArray) {
			itemArray.asList().forEach(jsonElement -> {
				if (jsonElement instanceof JsonObject jsonObject) {
					List<Item> itemList = Codec.withAlternative(Item.CODEC.listOf(), Item.CODEC, Collections::singletonList)
						.xmap(holders -> holders.stream().map(Holder::value).toList(), item -> item.stream().map(item1 -> (Holder<Item>) item1.builtInRegistryHolder()).toList()).lenientOptionalFieldOf("name", Collections.emptyList()).codec().parse(JsonOps.INSTANCE, jsonObject).getOrThrow();
					Codec<TagKey<Item>> alternating = Codec.withAlternative(TagKey.codec(Registries.ITEM), TagKey.hashedCodec(Registries.ITEM));
					List<TagKey<Item>> tagsList = Codec.withAlternative(alternating.listOf(), alternating, Collections::singletonList).lenientOptionalFieldOf("tag", Collections.emptyList()).codec().parse(JsonOps.INSTANCE, jsonObject).getOrThrow();
					if (itemList.isEmpty() && tagsList.isEmpty()) {
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
					List<? extends EntityType<?>> entityList = Codec.withAlternative(ENTITY_TYPE_CODEC.listOf(), ENTITY_TYPE_CODEC, Collections::singletonList)
						.xmap(holders -> holders.stream().map(Holder::value).toList(), item -> item.stream().map(item1 -> (Holder<EntityType<?>>) item1.builtInRegistryHolder()).toList()).lenientOptionalFieldOf("name", Collections.emptyList()).codec().parse(JsonOps.INSTANCE, jsonObject).getOrThrow();
					Codec<TagKey<EntityType<?>>> alternating = Codec.withAlternative(TagKey.codec(Registries.ENTITY_TYPE), TagKey.hashedCodec(Registries.ENTITY_TYPE));
					List<TagKey<EntityType<?>>> tagsList = Codec.withAlternative(alternating.listOf(), alternating, Collections::singletonList).lenientOptionalFieldOf("tag", Collections.emptyList()).codec().parse(JsonOps.INSTANCE, jsonObject).getOrThrow();
					if (entityList.isEmpty() && tagsList.isEmpty()) {
						noNamePresent(jsonElement, "Configuring Entities");
						return;
					}
					parseEntityType((List<EntityType<?>>) entityList, tagsList, jsonObject);
				} else
					notJSONObject(jsonElement, "Configuring Entities");
			});
		}
		armourCalcs = Formula.CODEC.lenientOptionalFieldOf("armor_calculation").codec().parse(JsonOps.INSTANCE, object).getOrThrow().orElse(null);
		isModifying = false;
	}
	public void parseEntityType(List<EntityType<?>> entities, List<TagKey<EntityType<?>>> entityTags, JsonObject jsonObject) {
		ConfigurableEntityData configurableEntityData = ConfigurableEntityData.CODEC.parse(JsonOps.INSTANCE, jsonObject).getOrThrow();
		ConfigDataWrapper<EntityType<?>, ConfigurableEntityData> configDataWrapper = new ConfigDataWrapper<>(entities, entityTags, configurableEntityData);
		configuredEntities.add(configDataWrapper);
	}
	public void parseWeaponType(List<WeaponType> types, JsonObject jsonObject) {
		ConfigurableWeaponData configurableWeaponData = ConfigurableWeaponData.CODEC.parse(JsonOps.INSTANCE, jsonObject).getOrThrow();
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
			case "wood" -> ToolMaterial.WOOD;
			case "stone" -> ToolMaterial.STONE;
			case "iron" -> ToolMaterial.IRON;
			case "gold" -> ToolMaterial.GOLD;
			case "diamond" -> ToolMaterial.DIAMOND;
			case "netherite" -> ToolMaterial.NETHERITE;
			default -> getTierRaw(s);
		};
	}
	public static String getTierName(Tier tier) {
		if (tier instanceof ToolMaterial material) {
            if (material.equals(ToolMaterial.NETHERITE)) return "netherite";
			if (material.equals(ToolMaterial.DIAMOND)) return "diamond";
			if (material.equals(ToolMaterial.IRON)) return "iron";
			if (material.equals(ToolMaterial.STONE)) return "stone";
			if (material.equals(ToolMaterial.GOLD)) return "gold";
			return "wood";
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
		buf.writeUtf(armourCalcs == null ? "empty" : armourCalcs.written());
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
			Tier tier = null;
			boolean isConfiguredItem = configurableItemData != null;
			if (isConfiguredItem) {
				Integer durability = configurableItemData.armourStats().durability().getValue(item);
				Integer maxStackSize = configurableItemData.stackSize();
				Double piercingLevel = configurableItemData.weaponStats().piercingLevel();
				tier = configurableItemData.tier();
				TagKey<Block> mineable = configurableItemData.toolMineableTag();
				Tool tool = configurableItemData.tool();
				Enchantable enchantable = configurableItemData.enchantable();
				TagKey<Item> repairItems = configurableItemData.repairItems();
				UseCooldown cooldown = configurableItemData.cooldown();
				if (cooldown != null)
					builder.set(DataComponents.USE_COOLDOWN, cooldown);
				if (maxStackSize != null)
					builder.set(DataComponents.MAX_STACK_SIZE, maxStackSize);
				if (durability != null) {
					setDurability(builder, item, durability);
					damageOverridden = true;
				}
				if (enchantable != null) builder.set(DataComponents.ENCHANTABLE, enchantable);
				else if (tier != null) builder.set(DataComponents.ENCHANTABLE, new Enchantable(tier.enchantmentValue()));
				if (repairItems != null) builder.set(DataComponents.REPAIRABLE, new Repairable(BuiltInRegistries.ITEM.getOrThrow(repairItems)));
				else if (tier != null) builder.set(DataComponents.REPAIRABLE, new Repairable(BuiltInRegistries.ITEM.getOrThrow(tier.repairItems())));
				if (piercingLevel != null) builder.set(CustomDataComponents.PIERCING_LEVEL, piercingLevel.floatValue());
				else {
					ConfigurableWeaponData configurableWeaponData;
					if ((configurableWeaponData = MethodHandler.forWeapon(item.combatify$getWeaponType())) != null && configurableWeaponData.piercingLevel() != null)
						builder.set(CustomDataComponents.PIERCING_LEVEL, configurableWeaponData.piercingLevel().floatValue());
				}
				if (tool != null) builder.set(DataComponents.TOOL, tool);
				else if (tier != null && mineable != null) builder.set(DataComponents.TOOL, new Tool(List.of(Tool.Rule.deniesDrops(BuiltInRegistries.BLOCK.getOrThrow(tier.incorrectBlocksForDrops())), Tool.Rule.minesAndDrops(BuiltInRegistries.BLOCK.getOrThrow(mineable), tier.speed())), 1.0F, 1));
			}
			if (!damageOverridden && item.getTierFromConfig().isPresent()) {
				int value = item.getTierFromConfig().get().durability();
				if (item.builtInRegistryHolder().is(CombatifyItemTags.DOUBLE_TIER_DURABILITY))
					value *= 2;
				setDurability(builder, item, value);
			}
			updateModifiers(builder, item, tier, isConfiguredItem, configurableItemData);
			((ItemAccessor) item).setComponents(builder.build());
		}
	}
	@SuppressWarnings("ALL")
	public void updateModifiers(DataComponentMap.Builder builder, Item item, @Nullable Tier tier, boolean isConfiguredItem, @Nullable ConfigurableItemData configurableItemData) {
		if (tier != null) builder.set(CustomDataComponents.BLOCKING_LEVEL, tier.combatify$blockingLevel());
		ItemAttributeModifiers modifier = item.components().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
		ItemAttributeModifiers original = originalModifiers.get(item.builtInRegistryHolder());
		if (!original.equals(ItemAttributeModifiers.EMPTY))
			modifier = original;
		modifier = item.modifyAttributeModifiers(modifier);
		if (modifier != null) {
			modifyFromItemConfig: {
				if (isConfiguredItem) {
					if (!configurableItemData.itemAttributeModifiers().equals(ItemAttributeModifiers.EMPTY)) {
						modifier = configurableItemData.itemAttributeModifiers();
						break modifyFromItemConfig;
					}
					if (configurableItemData.weaponStats().weaponType() != null) {
						ItemAttributeModifiers.Builder itemAttributeBuilder = ItemAttributeModifiers.builder();
						configurableItemData.weaponStats().weaponType().addCombatAttributes(item.getConfigTier(), itemAttributeBuilder);
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
					if (item.components().has(DataComponents.EQUIPPABLE))
						slotGroup = EquipmentSlotGroup.bySlot(item.components().get(DataComponents.EQUIPPABLE).slot());
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
	public record Formula(String armour, String enchantment) {
		public Formula(String written) {
			this(written.split("enchant:", 2)[0], written.split("enchant:", 2)[1]);
		}
		public String written() {
			return armour + "enchant:" + enchantment;
		}
		public static final Codec<Formula> SINGLE_LINE = Codec.STRING.xmap(Formula::new, Formula::written);
		public static final Codec<Formula> CONCAT_OBJECT = RecordCodecBuilder.create(instance ->
			instance.group(Codec.STRING.fieldOf("armor_protection").forGetter(Formula::armour),
				Codec.STRING.fieldOf("enchantment_protection").forGetter(Formula::enchantment))
				.apply(instance, Formula::new));
		public static final Codec<Formula> CODEC = Codec.withAlternative(CONCAT_OBJECT, SINGLE_LINE);
		public float armourCalcs(float amount, DamageSource damageSource, float armour, float armourToughness) {
			String armourFormula = this.armour;
			armourFormula = armourFormula.replaceAll("D", String.valueOf(amount)).replaceAll("P", String.valueOf(armour)).replaceAll("T", String.valueOf(armourToughness));
			float result = solveFormula(armourFormula);
			ItemStack itemStack = damageSource.getWeaponItem();
			if (itemStack != null && damageSource.getEntity().level() instanceof ServerLevel serverLevel)
				result = 1 - Mth.clamp(EnchantmentHelper.modifyArmorEffectiveness(serverLevel, itemStack, damageSource.getEntity(), damageSource, result), 0.0F, 1.0F);
			else result = 1 - result;
			return amount * result;
		}
		public float enchantCalcs(float amount, float enchantLevel) {
			String enchantFormula = this.enchantment;
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
