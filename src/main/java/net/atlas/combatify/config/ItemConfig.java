package net.atlas.combatify.config;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.atlas.atlascore.AtlasCore;
import net.atlas.atlascore.config.AtlasConfig;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.util.blocking.BlockingType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;

import static net.atlas.combatify.Combatify.*;

public class ItemConfig extends AtlasConfig {
	public boolean isModifying = false;
	public TagHolder<List<RegistryConfigDataWrapper<EntityType<?>, ConfigurableEntityData>>> entities;
	public TagHolder<List<RegistryConfigDataWrapper<Item, ConfigurableItemData>>> items;
	public static final StreamCodec<RegistryFriendlyByteBuf, String> NAME_STREAM_CODEC = StreamCodec.of(RegistryFriendlyByteBuf::writeUtf, RegistryFriendlyByteBuf::readUtf);
	public static final StreamCodec<RegistryFriendlyByteBuf, ResourceLocation> RESOURCE_NAME_STREAM_CODEC = StreamCodec.of(RegistryFriendlyByteBuf::writeResourceLocation, RegistryFriendlyByteBuf::readResourceLocation);
	public static final MapCodec<RegistryConfigDataWrapper<Item, ConfigurableItemData>> ITEMS_CODEC = RegistryConfigDataWrapper.mapCodec(BuiltInRegistries.ITEM,
		holder -> holder.is(Items.AIR.builtInRegistryHolder()) ? DataResult.error(() -> "Item must not be minecraft:air", holder) : DataResult.success(holder),
		ConfigurableItemData.CODEC);
	public static final MapCodec<RegistryConfigDataWrapper<EntityType<?>, ConfigurableEntityData>> ENTITIES_CODEC = RegistryConfigDataWrapper.mapCodec(BuiltInRegistries.ENTITY_TYPE,
		DataResult::success,
		ConfigurableEntityData.CODEC);
	public static Formula armourCalcs = null;

	public ItemConfig() {
		super(id("combatify-items-v3"));
	}

	public static Formula getArmourCalcs() {
		return armourCalcs;
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
		armourCalcs = Formula.CODEC.lenientOptionalFieldOf("armor_calculation").codec().parse(JsonOps.INSTANCE, object).getOrThrow().orElse(null);
		isModifying = false;
	}
	@Override
	protected InputStream getDefaultedConfig() {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream("combatify-items-v3.json");
	}

	@Override
	public void defineConfigHolders() {
		items = createCodecBacked("items", new ArrayList<>(), ITEMS_CODEC.codec().listOf());
		entities = createCodecBacked("entities", new ArrayList<>(), ENTITIES_CODEC.codec().listOf());
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
		String formula = buf.readUtf();
		if (!formula.equals("empty"))
			armourCalcs = new Formula(formula);
		return this;
	}

	public ItemConfig loadFromNetwork(RegistryFriendlyByteBuf buf) {
		super.loadFromNetwork(buf);
		registeredTypes = readMap(buf, RESOURCE_NAME_STREAM_CODEC, BlockingType.FULL_STREAM_CODEC);
		String formula = buf.readUtf();
		if (!formula.equals("empty"))
			armourCalcs = new Formula(formula);
		return this;
	}

	public void saveToNetwork(RegistryFriendlyByteBuf buf) {
		super.saveToNetwork(buf);
		writeMap(buf, Combatify.registeredTypes, RESOURCE_NAME_STREAM_CODEC, BlockingType.FULL_STREAM_CODEC);
		buf.writeUtf(armourCalcs == null ? "empty" : armourCalcs.written());
	}

	@Override
	public JsonElement saveExtra(JsonElement jsonElement) {
		JsonElement defenders = new JsonArray();
		ArrayList<BlockingType> blockingTypes = new ArrayList<>(registeredTypes.values());
		blockingTypes.removeIf(defaultTypes::containsValue);
		defenders = BlockingType.CODEC.listOf().encode(blockingTypes, JsonOps.INSTANCE, defenders).getOrThrow();
		JsonObject result = jsonElement.getAsJsonObject();
		result.add("blocking_types", defenders);
		return Formula.CODEC.lenientOptionalFieldOf("armor_calculation").codec().encode(Optional.ofNullable(armourCalcs), JsonOps.INSTANCE, result).getOrThrow();
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
			ByteBufCodecs.collection(ArrayList::new, TagKey.streamCodec(registry)), RegistryConfigDataWrapper::tagKeys,
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
