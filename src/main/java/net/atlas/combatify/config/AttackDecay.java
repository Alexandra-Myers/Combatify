package net.atlas.combatify.config;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;
import net.atlas.atlascore.config.AtlasConfig;
import net.atlas.atlascore.util.ConfigRepresentable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AttackDecay implements ConfigRepresentable<AttackDecay> {
	public static final AttackDecay DEFAULT = new AttackDecay(null, false, 0, 100, 20, 100, 0, 100);
	public static final StreamCodec<RegistryFriendlyByteBuf, AttackDecay> STREAM_CODEC = new StreamCodec<>() {
        public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, AttackDecay projectileDamage) {
            registryFriendlyByteBuf.writeResourceLocation(projectileDamage.owner.heldValue.owner().name);
            registryFriendlyByteBuf.writeUtf(projectileDamage.owner.heldValue.name());
            registryFriendlyByteBuf.writeBoolean(projectileDamage.enabled);
			registryFriendlyByteBuf.writeVarInt(projectileDamage.minCharge);
			registryFriendlyByteBuf.writeVarInt(projectileDamage.maxCharge);
			registryFriendlyByteBuf.writeVarInt(projectileDamage.minPercentageBase);
			registryFriendlyByteBuf.writeVarInt(projectileDamage.maxPercentageBase);
			registryFriendlyByteBuf.writeVarInt(projectileDamage.minPercentageEnchants);
			registryFriendlyByteBuf.writeVarInt(projectileDamage.maxPercentageEnchants);
        }

        @NotNull
		@SuppressWarnings("unchecked")
        public AttackDecay decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            AtlasConfig config = AtlasConfig.configs.get(registryFriendlyByteBuf.readResourceLocation());
            return new AttackDecay((AtlasConfig.ConfigHolder<AttackDecay>) config.valueNameToConfigHolderMap.get(registryFriendlyByteBuf.readUtf()), registryFriendlyByteBuf.readBoolean(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt());
        }
    };
	public AtlasConfig.ConfigHolder<AttackDecay> owner;
	public Boolean enabled;
	public Integer minCharge;
	public Integer maxCharge;
	public Integer minPercentageBase;
	public Integer maxPercentageBase;
	public Integer minPercentageEnchants;
	public Integer maxPercentageEnchants;
	public Boolean enabled() {
		return enabled;
	}
	public Integer minCharge() {
		return minCharge;
	}
	public Integer maxCharge() {
		return maxCharge;
	}
	public Integer minPercentageBase() {
		return minPercentageBase;
	}
	public Integer maxPercentageBase() {
		return maxPercentageBase;
	}
	public Integer minPercentageEnchants() {
		return minPercentageEnchants;
	}
	public Integer maxPercentageEnchants() {
		return maxPercentageEnchants;
	}
	public static final Map<String, Field> fields = Util.make(new HashMap<>(), (hashMap) -> {
		try {
			hashMap.put("enabled", AttackDecay.class.getDeclaredField("enabled"));
			hashMap.put("minCharge", AttackDecay.class.getDeclaredField("minCharge"));
			hashMap.put("maxCharge", AttackDecay.class.getDeclaredField("maxCharge"));
			hashMap.put("minPercentageBase", AttackDecay.class.getDeclaredField("minPercentageBase"));
			hashMap.put("maxPercentageBase", AttackDecay.class.getDeclaredField("maxPercentageBase"));
			hashMap.put("minPercentageEnchants", AttackDecay.class.getDeclaredField("minPercentageEnchants"));
			hashMap.put("maxPercentageEnchants", AttackDecay.class.getDeclaredField("maxPercentageEnchants"));
		} catch (NoSuchFieldException ignored) {
		}

	});
	public static final BiFunction<AttackDecay, String, Component> convertFieldToComponent = (projectileDamage, string) -> {
		try {
			return Component.translatable(projectileDamage.owner.getTranslationKey() + "." + string).append(Component.literal(": ")).append(Component.literal(String.valueOf(projectileDamage.fieldRepresentingHolder(string).get(projectileDamage))));
		} catch (IllegalAccessException var3) {
			return Component.translatable(projectileDamage.owner.getTranslationKey() + "." + string);
		}
	};
	public static final BiFunction<AttackDecay, String, Component> convertFieldToNameComponent = (projectileDamage, string) -> Component.translatable(projectileDamage.owner.getTranslationKey() + "." + string);
	public static final BiFunction<AttackDecay, String, Component> convertFieldToValueComponent = (projectileDamage, string) -> {
		try {
			return Component.literal(String.valueOf(projectileDamage.fieldRepresentingHolder(string).get(projectileDamage)));
		} catch (IllegalAccessException var3) {
			return Component.translatable(projectileDamage.owner.getTranslationKey() + "." + string);
		}
	};
	public Supplier<Component> resetTranslation = null;

	public AttackDecay(AtlasConfig.ConfigHolder<AttackDecay> owner, Boolean enabled, Integer minCharge, Integer maxCharge, Integer minPercentageBase, Integer maxPercentageBase, Integer minPercentageEnchants, Integer maxPercentageEnchants) {
		this.owner = owner;
		this.enabled = enabled;
		this.minCharge = Mth.clamp(minCharge, 0, 200);
		this.maxCharge = Mth.clamp(maxCharge, 0, 200);
		this.minPercentageBase = Mth.clamp(minPercentageBase, 0, 200);
		this.maxPercentageBase = Mth.clamp(maxPercentageBase, 0, 200);
		this.minPercentageEnchants = Mth.clamp(minPercentageEnchants, 0, 200);
		this.maxPercentageEnchants = Mth.clamp(maxPercentageEnchants, 0, 200);
	}

	@Override
	public Codec<AttackDecay> getCodec(AtlasConfig.ConfigHolder<AttackDecay> configHolder) {
		return RecordCodecBuilder.create(instance ->
			instance.group(Codec.BOOL.optionalFieldOf("enabled", false).forGetter(AttackDecay::enabled),
					ExtraCodecs.intRange(0, 200).optionalFieldOf("minCharge", 0).forGetter(AttackDecay::minCharge),
					ExtraCodecs.intRange(0, 200).optionalFieldOf("maxCharge", 100).forGetter(AttackDecay::maxCharge),
					ExtraCodecs.intRange(0, 200).optionalFieldOf("minPercentageBase", 20).forGetter(AttackDecay::minPercentageBase),
					ExtraCodecs.intRange(0, 200).optionalFieldOf("maxPercentageBase", 100).forGetter(AttackDecay::maxPercentageBase),
					ExtraCodecs.intRange(0, 200).optionalFieldOf("minPercentageEnchants", 0).forGetter(AttackDecay::minPercentageEnchants),
					ExtraCodecs.intRange(0, 200).optionalFieldOf("maxPercentageEnchants", 100).forGetter(AttackDecay::maxPercentageEnchants))
				.apply(instance, (enabled, minCharge, maxCharge, minPercentageBase, maxPercentageBase, minPercentageEnchants, maxPercentageEnchants) -> new AttackDecay(configHolder, enabled, minCharge, maxCharge, minPercentageBase, maxPercentageBase, minPercentageEnchants, maxPercentageEnchants)));
	}

	@Override
	public void setOwnerHolder(AtlasConfig.ConfigHolder<AttackDecay> owner) {
		this.owner = owner;
	}

	@Override
	public List<String> fields() {
		return fields.keySet().stream().toList();
	}

	@Override
	public Component getFieldValue(String name) {
		return convertFieldToValueComponent.apply(this, name);
	}

	@Override
	public Component getFieldName(String name) {
		return convertFieldToNameComponent.apply(this, name);
	}

	@Override
	public void listField(String name, Consumer<Component> input) {
		input.accept(convertFieldToComponent.apply(this, name));
	}

	@Override
	public void listFields(Consumer<Component> input) {
		fields.keySet().forEach((string) -> input.accept(convertFieldToComponent.apply(this, string)));
	}

	@Override
	public Field fieldRepresentingHolder(String name) {
		return fields.get(name);
	}

	@Override
	public ArgumentType<?> argumentTypeRepresentingHolder(String name) {
        Object o;
        try {
            o = fields.get(name).get(this);
        } catch (IllegalAccessException e) {
            return null;
        }
        return switch (o) {
			case Boolean ignored -> BoolArgumentType.bool();
			case Integer ignored -> IntegerArgumentType.integer(0, 200);
			case null, default -> null;
		};
	}

	@Override
	@Environment(EnvType.CLIENT)
	@SuppressWarnings("all")
	public List<AbstractConfigListEntry<?>> transformIntoConfigEntries() {
		if (this.resetTranslation == null) this.resetTranslation = () -> Component.translatable(this.owner.getTranslationResetKey());
		List<AbstractConfigListEntry<?>> entries = new ArrayList<>();
		entries.add(new BooleanListEntry(convertFieldToNameComponent.apply(this, "enabled"), this.enabled, this.resetTranslation.get(), () -> false, (enabled) -> this.enabled = enabled, setupTooltip(1, "enabled"), false));
		entries.add(new IntegerListEntry(convertFieldToNameComponent.apply(this, "minCharge"), this.minCharge, this.resetTranslation.get(), () -> 0, (charge) -> this.minCharge = Mth.clamp(charge, 0, 200), setupTooltip(1, "minCharge"), false));
		entries.add(new IntegerListEntry(convertFieldToNameComponent.apply(this, "maxCharge"), this.maxCharge, this.resetTranslation.get(), () -> 100, (charge) -> this.maxCharge = Mth.clamp(charge, 0, 200), setupTooltip(1, "maxCharge"), false));
		entries.add(new IntegerListEntry(convertFieldToNameComponent.apply(this, "minPercentageBase"), this.minPercentageBase, this.resetTranslation.get(), () -> 20, (percent) -> this.minPercentageBase = Mth.clamp(percent, 0, 200), setupTooltip(3, "minPercentageBase"), false));
		entries.add(new IntegerListEntry(convertFieldToNameComponent.apply(this, "maxPercentageBase"), this.maxPercentageBase, this.resetTranslation.get(), () -> 100, (percent) -> this.maxPercentageBase = Mth.clamp(percent, 0, 200), setupTooltip(3, "maxPercentageBase"), false));
		entries.add(new IntegerListEntry(convertFieldToNameComponent.apply(this, "minPercentageEnchants"), this.minPercentageEnchants, this.resetTranslation.get(), () -> 0, (percent) -> this.minPercentageEnchants = Mth.clamp(percent, 0, 200), setupTooltip(2, "minPercentageEnchants"), false));
		entries.add(new IntegerListEntry(convertFieldToNameComponent.apply(this, "maxPercentageEnchants"), this.maxPercentageEnchants, this.resetTranslation.get(), () -> 100, (percent) -> this.maxPercentageEnchants = Mth.clamp(percent, 0, 200), setupTooltip(2, "maxPercentageEnchants"), false));
		entries.forEach((entry) -> entry.setEditable(!this.owner.serverManaged));
		return entries;
	}

	public Supplier<Optional<Component[]>> setupTooltip(int length, String field) {
		Component[] components = new Component[length];
		String key = owner.getTranslationKey() + "." + field;
		components[0] = Component.translatable(key + ".tooltip");

		for(int i = 1; i < length; ++i) {
			components[i] = Component.translatable(key + ".tooltip." + i);
		}

		return () -> Optional.of(components);
	}
}
