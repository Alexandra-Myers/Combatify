package net.atlas.combatify.config;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.entries.DoubleListEntry;
import net.atlas.atlascore.config.AtlasConfig;
import net.atlas.atlascore.util.Codecs;
import net.atlas.atlascore.util.ConfigRepresentable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ProjectileDamage implements ConfigRepresentable<ProjectileDamage> {
	public static final ProjectileDamage DEFAULT = new ProjectileDamage(null, 0.0, 0.0, 1.0, 8.0);
	public static final StreamCodec<RegistryFriendlyByteBuf, ProjectileDamage> STREAM_CODEC = new StreamCodec<>() {
        public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, ProjectileDamage projectileDamage) {
            registryFriendlyByteBuf.writeResourceLocation(projectileDamage.owner.heldValue.owner().name);
            registryFriendlyByteBuf.writeUtf(projectileDamage.owner.heldValue.name());
            registryFriendlyByteBuf.writeDouble(projectileDamage.eggDamage);
			registryFriendlyByteBuf.writeDouble(projectileDamage.snowballDamage);
			registryFriendlyByteBuf.writeDouble(projectileDamage.windChargeDamage);
			registryFriendlyByteBuf.writeDouble(projectileDamage.thrownTridentDamage);
        }

        @NotNull
		@SuppressWarnings("unchecked")
        public ProjectileDamage decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            AtlasConfig config = AtlasConfig.configs.get(registryFriendlyByteBuf.readResourceLocation());
            return new ProjectileDamage((AtlasConfig.ConfigHolder<ProjectileDamage>) config.valueNameToConfigHolderMap.get(registryFriendlyByteBuf.readUtf()), registryFriendlyByteBuf.readDouble(), registryFriendlyByteBuf.readDouble(), registryFriendlyByteBuf.readDouble(), registryFriendlyByteBuf.readDouble());
        }
    };
	public AtlasConfig.ConfigHolder<ProjectileDamage> owner;
	public Double eggDamage;
	public Double snowballDamage;
	public Double windChargeDamage;
	public Double thrownTridentDamage;
	public Double eggDamage() {
		return eggDamage;
	}
	public Double snowballDamage() {
		return snowballDamage;
	}
	public Double windChargeDamage() {
		return windChargeDamage;
	}
	public Double thrownTridentDamage() {
		return thrownTridentDamage;
	}
	public static final Map<String, Field> fields = Util.make(new HashMap<>(), (hashMap) -> {
		try {
			hashMap.put("egg_damage", ProjectileDamage.class.getDeclaredField("eggDamage"));
			hashMap.put("snowball_damage", ProjectileDamage.class.getDeclaredField("snowballDamage"));
			hashMap.put("wind_charge_damage", ProjectileDamage.class.getDeclaredField("windChargeDamage"));
			hashMap.put("thrown_trident_damage", ProjectileDamage.class.getDeclaredField("thrownTridentDamage"));
		} catch (NoSuchFieldException ignored) {
		}

	});
	public static final BiFunction<ProjectileDamage, String, Component> convertFieldToComponent = (projectileDamage, string) -> {
		try {
			return Component.translatable(projectileDamage.owner.getTranslationKey() + "." + string).append(Component.literal(": ")).append(Component.literal(String.valueOf(projectileDamage.fieldRepresentingHolder(string).get(projectileDamage))));
		} catch (IllegalAccessException var3) {
			return Component.translatable(projectileDamage.owner.getTranslationKey() + "." + string);
		}
	};
	public static final BiFunction<ProjectileDamage, String, Component> convertFieldToNameComponent = (projectileDamage, string) -> Component.translatable(projectileDamage.owner.getTranslationKey() + "." + string);
	public static final BiFunction<ProjectileDamage, String, Component> convertFieldToValueComponent = (projectileDamage, string) -> {
		try {
			return Component.literal(String.valueOf(projectileDamage.fieldRepresentingHolder(string).get(projectileDamage)));
		} catch (IllegalAccessException var3) {
			return Component.translatable(projectileDamage.owner.getTranslationKey() + "." + string);
		}
	};
	public Supplier<Component> resetTranslation = null;

	public ProjectileDamage(AtlasConfig.ConfigHolder<ProjectileDamage> owner, Double eggDamage, Double snowballDamage, Double windChargeDamage, Double thrownTridentDamage) {
		this.owner = owner;
		this.eggDamage = Mth.clamp(eggDamage, 0, 40D);
		this.snowballDamage = Mth.clamp(snowballDamage, 0, 40D);
		this.windChargeDamage = Mth.clamp(windChargeDamage, 0, 40D);
		this.thrownTridentDamage = Mth.clamp(thrownTridentDamage, 0, 40D);
	}

	@Override
	public Codec<ProjectileDamage> getCodec(AtlasConfig.ConfigHolder<ProjectileDamage> configHolder) {
		return RecordCodecBuilder.create(instance ->
			instance.group(Codecs.doubleRange(0, 40).optionalFieldOf("egg_damage", 0.0).forGetter(ProjectileDamage::eggDamage),
					Codecs.doubleRange(0, 40).optionalFieldOf("snowball_damage", 0.0).forGetter(ProjectileDamage::snowballDamage),
					Codecs.doubleRange(0, 40).optionalFieldOf("wind_charge_damage", 1.0).forGetter(ProjectileDamage::windChargeDamage),
					Codecs.doubleRange(0, 40).optionalFieldOf("thrown_trident_damage", 8.0).forGetter(ProjectileDamage::thrownTridentDamage))
				.apply(instance, (eggDamage, snowballDamage, windChargeDamage, thrownTridentDamage) -> new ProjectileDamage(configHolder, eggDamage, snowballDamage, windChargeDamage, thrownTridentDamage)));
	}

	@Override
	public void setOwnerHolder(AtlasConfig.ConfigHolder<ProjectileDamage> owner) {
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
			case Double ignored -> DoubleArgumentType.doubleArg(0.0, 40.0);
			case null, default -> null;
		};
	}

	@Override
	@Environment(EnvType.CLIENT)
	@SuppressWarnings("all")
	public List<AbstractConfigListEntry<?>> transformIntoConfigEntries() {
		if (this.resetTranslation == null) this.resetTranslation = () -> Component.translatable(this.owner.getTranslationResetKey());
		List<AbstractConfigListEntry<?>> entries = new ArrayList<>();
		entries.add(new DoubleListEntry(convertFieldToNameComponent.apply(this, "egg_damage"), this.eggDamage, this.resetTranslation.get(), () -> 0.0, (damage) -> this.eggDamage = Mth.clamp(damage, 0.0, 40.0), Optional::empty, false));
		entries.add(new DoubleListEntry(convertFieldToNameComponent.apply(this, "snowball_damage"), this.snowballDamage, this.resetTranslation.get(), () -> 0.0, (damage) -> this.snowballDamage = Mth.clamp(damage, 0.0, 40.0), Optional::empty, false));
		entries.add(new DoubleListEntry(convertFieldToNameComponent.apply(this, "wind_charge_damage"), this.windChargeDamage, this.resetTranslation.get(), () -> 1.0, (damage) -> this.windChargeDamage = Mth.clamp(damage, 0.0, 40.0), Optional::empty, false));
		entries.add(new DoubleListEntry(convertFieldToNameComponent.apply(this, "thrown_trident_damage"), this.thrownTridentDamage, this.resetTranslation.get(), () -> 8.0, (damage) -> this.thrownTridentDamage = Mth.clamp(damage, 0.0, 40.0), Optional::empty, false));
		entries.forEach((entry) -> entry.setEditable(!this.owner.serverManaged));
		return entries;
	}
}
