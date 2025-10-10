package net.atlas.combatify.config;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.atlas.atlascore.AtlasCore;
import net.atlas.atlascore.config.AtlasConfig;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.item.CombatifyItemTags;
import net.atlas.combatify.util.blocking.BlockingType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;

import static net.atlas.combatify.Combatify.*;

public class ItemConfig extends AtlasConfig {
	public static final List<RegistryConfigDataWrapper<Item, ConfigurableItemData>> DEFAULT_ITEMS;
	static {
		DEFAULT_ITEMS = List.of(new RegistryConfigDataWrapper<>(HolderSet.direct(), List.of(CombatifyItemTags.PROJECTILES_WITH_COOLDOWNS), new ConfigurableItemData(null, 0.2)),
			new RegistryConfigDataWrapper<>(HolderSet.direct(), List.of(CombatifyItemTags.FAST_DRINKABLES), new ConfigurableItemData(1.0, null)));
	}
	public boolean isModifying = false;
	public TagHolder<List<RegistryConfigDataWrapper<EntityType<?>, ConfigurableEntityData>>> entities;
	public TagHolder<List<RegistryConfigDataWrapper<Item, ConfigurableItemData>>> items;
	public TagHolder<JSImpl> armourCalcs;
	public static final StreamCodec<RegistryFriendlyByteBuf, ResourceLocation> RESOURCE_NAME_STREAM_CODEC = StreamCodec.of(RegistryFriendlyByteBuf::writeResourceLocation, RegistryFriendlyByteBuf::readResourceLocation);
	public static final MapCodec<RegistryConfigDataWrapper<Item, ConfigurableItemData>> ITEMS_CODEC = RegistryConfigDataWrapper.mapCodec(BuiltInRegistries.ITEM,
		holder -> holder.is(Items.AIR.builtInRegistryHolder()) ? DataResult.error(() -> "Item must not be minecraft:air", holder) : DataResult.success(holder),
		ConfigurableItemData.CODEC);
	public static final MapCodec<RegistryConfigDataWrapper<EntityType<?>, ConfigurableEntityData>> ENTITIES_CODEC = RegistryConfigDataWrapper.mapCodec(BuiltInRegistries.ENTITY_TYPE,
		DataResult::success,
		ConfigurableEntityData.CODEC);

	public ItemConfig() {
		super(id("combatify-items-1.21-v3"));
	}

	@Override
	public void reloadFromDefault() {
		super.reloadFromDefault();
	}

	@Override
	public void handleExtraSync(AtlasCore.AtlasConfigPacket atlasConfigPacket, ClientPlayNetworking.Context context) {
		if (CONFIG.enableDebugLogging())
			LOGGER.info("Loading config details from buffer.");
	}

	@Override
	public void handleConfigInformation(AtlasCore.ClientInformPacket clientInformPacket, ServerPlayer serverPlayer, PacketSender packetSender) {

	}

	@Override
	protected void loadExtra(JsonObject object) {
		isModifying = true;
		if (!object.has("blocking_types"))
			object.add("blocking_types", new JsonArray());
		JsonElement defenders = object.get("blocking_types");
		registeredTypes = new HashMap<>(defaultTypes);
		List<BlockingType> altered = BlockingType.CODEC.listOf().orElse(Collections.emptyList()).parse(JsonOps.INSTANCE, defenders).getOrThrow();
		altered.forEach(Combatify::registerBlockingType);
		isModifying = false;
	}

	@Override
	public void defineConfigHolders() {
		items = createCodecBacked("items", DEFAULT_ITEMS, ITEMS_CODEC.codec().listOf());
		entities = createCodecBacked("entities", new ArrayList<>(), ENTITIES_CODEC.codec().listOf());
		armourCalcs = createCodecBacked("armor_calculation", new JSImpl("armor_calculations"), JSImpl.CODEC);
	}

	@Override
	public void resetExtraHolders() {
		registeredTypes = new HashMap<>(defaultTypes);
	}

	@Override
	public <T> void alertChange(ConfigValue<T> tConfigValue, T newValue) {

	}

	@Override
	public <T> void alertClientValue(ConfigValue<T> configValue, T t, T t1) {

	}

	@Override
	public AtlasConfig readClientConfigInformation(RegistryFriendlyByteBuf buf) {
		super.readClientConfigInformation(buf);
		readMap(buf, RESOURCE_NAME_STREAM_CODEC, BlockingType.FULL_STREAM_CODEC);
		return this;
	}

	public ItemConfig loadFromNetwork(RegistryFriendlyByteBuf buf) {
		super.loadFromNetwork(buf);
		registeredTypes = readMap(buf, RESOURCE_NAME_STREAM_CODEC, BlockingType.FULL_STREAM_CODEC);
		return this;
	}

	public void saveToNetwork(RegistryFriendlyByteBuf buf) {
		super.saveToNetwork(buf);
		writeMap(buf, registeredTypes, RESOURCE_NAME_STREAM_CODEC, BlockingType.FULL_STREAM_CODEC);
	}

	@Override
	public JsonElement saveExtra(JsonElement jsonElement) {
		JsonElement defenders = new JsonArray();
		ArrayList<BlockingType> blockingTypes = new ArrayList<>(registeredTypes.values());
		blockingTypes.removeIf(defaultTypes::containsValue);
		defenders = BlockingType.CODEC.listOf().encode(blockingTypes, JsonOps.INSTANCE, defenders).getOrThrow();
		JsonObject result = jsonElement.getAsJsonObject();
		result.add("blocking_types", defenders);
		return result;
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
	@Environment(EnvType.CLIENT)
	public Screen createScreen(Screen prevScreen) {
		return null;
	}
	public static void noNamePresent(RegistryConfigDataWrapper<?, ?> invalid, String stage) {
		LOGGER.error("No name is present: " + invalid + ", no changes will occur. This may be due to an incorrectly written config file. " + errorStage(stage));
	}
	public static String errorStage(String stage) {
		return "[Config Stage]: " + stage;
	}
	public interface ConfigDataWrapper<V, U> {
		boolean matches(V test);
		default U match(V test) {
			if (matches(test)) {
				return configurableData();
			}
			return null;
		}
		U configurableData();
	}
	// Unused, but we ignore it since we don't really care?
	public record RawConfigDataWrapper<T, U>(List<T> objects, U configurableData) implements ConfigDataWrapper<T, U> {
		public static <T, U> MapCodec<RawConfigDataWrapper<T, U>> mapCodec(MapCodec<List<T>> codec, MapCodec<U> mapCodec) {
			return RecordCodecBuilder.mapCodec(instance ->
				instance.group(codec.forGetter(RawConfigDataWrapper::objects),
						mapCodec.forGetter(RawConfigDataWrapper::configurableData))
					.apply(instance, RawConfigDataWrapper::new));
		}
		public static <T, U> StreamCodec<RegistryFriendlyByteBuf, RawConfigDataWrapper<T, U>> streamCodec(StreamCodec<RegistryFriendlyByteBuf, T> inputCodec, StreamCodec<RegistryFriendlyByteBuf, U> dataCodec) {
			return StreamCodec.composite(ByteBufCodecs.collection(ArrayList::new, inputCodec), RawConfigDataWrapper::objects,
				dataCodec, RawConfigDataWrapper::configurableData,
				RawConfigDataWrapper::new);
		}
		@Override
		public boolean matches(T test) {
			return objects.contains(test);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof RawConfigDataWrapper<?, ?> that)) return false;
            return Objects.equals(objects, that.objects) && Objects.equals(configurableData, that.configurableData);
		}

		@Override
		public int hashCode() {
			return Objects.hash(objects, configurableData);
		}

		@Override
		public String toString() {
			return "RawConfigDataWrapper{" +
				"objects=" + objects +
				", configurableData=" + configurableData +
				'}';
		}
	}
	public record RegistryConfigDataWrapper<T, U>(HolderSet.Direct<T> holders, List<TagKey<T>> tagKeys, U configurableData) implements ConfigDataWrapper<Holder<T>, U> {
		public static final RegistryConfigDataWrapper<Item, ConfigurableItemData> EMPTY_ITEM = new RegistryConfigDataWrapper<>(HolderSet.direct(), Collections.emptyList(), ConfigurableItemData.EMPTY);

        public boolean matches(Holder<T> test) {
			return holders.contains(test) || tagKeys.stream().anyMatch(test::is);
		}
		public static <T, U> RegistryConfigDataWrapper<T, U> build(HolderSet.Direct<T> holders, List<TagKey<T>> tagKeys, U configurableData) {
			RegistryConfigDataWrapper<T, U> result = new RegistryConfigDataWrapper<>(holders, tagKeys, configurableData);
			if (holders.size() == 0 && tagKeys.isEmpty()) noNamePresent(result, "Configuring Registry");
			return result;
		}
		public static <T, U> MapCodec<RegistryConfigDataWrapper<T, U>> mapCodec(Registry<T> registry, Function<Holder<T>, DataResult<Holder<T>>> validator, MapCodec<U> mapCodec) {
			Codec<Holder<T>> holderCodec = registry.holderByNameCodec().validate(validator);
			Codec<TagKey<T>> tagKeyCodec = Codec.withAlternative(TagKey.codec(registry.key()), TagKey.hashedCodec(registry.key()));
			return RecordCodecBuilder.mapCodec(instance ->
				instance.group(Codec.withAlternative(holderCodec.listOf(), holderCodec, Collections::singletonList)
							.xmap(HolderSet::direct, holders -> holders.stream().toList()).optionalFieldOf("name", HolderSet.direct())
							.forGetter(RegistryConfigDataWrapper::holders),
						Codec.withAlternative(tagKeyCodec.listOf(), tagKeyCodec, Collections::singletonList)
							.optionalFieldOf("tag", Collections.emptyList()).forGetter(RegistryConfigDataWrapper::tagKeys),
						mapCodec.forGetter(RegistryConfigDataWrapper::configurableData))
					.apply(instance, RegistryConfigDataWrapper::build));
		}
		public static <T, U> StreamCodec<RegistryFriendlyByteBuf, RegistryConfigDataWrapper<T, U>> streamCodec(ResourceKey<? extends Registry<T>> registry, StreamCodec<? super ByteBuf, U> streamCodec) {
			return StreamCodec.composite(ByteBufCodecs.holderSet(registry).map(holders -> (HolderSet.Direct<T>) holders, holders -> holders), RegistryConfigDataWrapper::holders,
			ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.fromCodecWithRegistriesTrusted(TagKey.codec(registry))), RegistryConfigDataWrapper::tagKeys,
				streamCodec, RegistryConfigDataWrapper::configurableData,
				RegistryConfigDataWrapper::build);
		}
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof RegistryConfigDataWrapper<?, ?> that)) return false;
            return Objects.equals(holders, that.holders) && Objects.equals(tagKeys, that.tagKeys) && Objects.equals(configurableData, that.configurableData);
		}

		@Override
		public int hashCode() {
			return Objects.hash(holders, tagKeys, configurableData);
		}

		@Override
		public String toString() {
			return "RegistryConfigDataWrapper{" +
				"holders=" + holders +
				", tagKeys=" + tagKeys +
				", configurableData=" + configurableData +
				'}';
		}
	}
}
