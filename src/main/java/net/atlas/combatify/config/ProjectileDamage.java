package net.atlas.combatify.config;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.entries.DoubleListEntry;
import net.atlas.atlascore.config.AtlasConfig;
import net.atlas.atlascore.util.ConfigRepresentable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.atlas.atlascore.config.AtlasConfig.getDouble;

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
            return new ProjectileDamage((AtlasConfig.ConfigHolder<ProjectileDamage, RegistryFriendlyByteBuf>) config.valueNameToConfigHolderMap.get(registryFriendlyByteBuf.readUtf()), registryFriendlyByteBuf.readDouble(), registryFriendlyByteBuf.readDouble(), registryFriendlyByteBuf.readDouble(), registryFriendlyByteBuf.readDouble());
        }
    };
	public AtlasConfig.ConfigHolder<ProjectileDamage, RegistryFriendlyByteBuf> owner;
	public Double eggDamage;
	public Double snowballDamage;
	public Double windChargeDamage;
	public Double thrownTridentDamage;
	public static final Map<String, Field> fields = Util.make(new HashMap<>(), (hashMap) -> {
		try {
			hashMap.put("eggDamage", ProjectileDamage.class.getDeclaredField("eggDamage"));
			hashMap.put("snowballDamage", ProjectileDamage.class.getDeclaredField("snowballDamage"));
			hashMap.put("windChargeDamage", ProjectileDamage.class.getDeclaredField("windChargeDamage"));
			hashMap.put("thrownTridentDamage", ProjectileDamage.class.getDeclaredField("thrownTridentDamage"));
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
	public static final BiFunction<AtlasConfig.ConfigHolder<ProjectileDamage, RegistryFriendlyByteBuf>, JsonObject, ProjectileDamage> decoder = (objectHolder, jsonObject) -> {
		Double eggDamage = 0.0;
		Double snowballDamage = 0.0;
		Double windChargeDamage = 1.0;
		Double thrownTridentDamage = 8.0;
		if (jsonObject.has("eggDamage")) {
			eggDamage = getDouble(jsonObject, "eggDamage");
		}
		if (jsonObject.has("snowballDamage")) {
			snowballDamage = getDouble(jsonObject, "snowballDamage");
		}
		if (jsonObject.has("windChargeDamage")) {
			windChargeDamage = getDouble(jsonObject, "windChargeDamage");
		}
		if (jsonObject.has("thrownTridentDamage")) {
			thrownTridentDamage = getDouble(jsonObject, "thrownTridentDamage");
		}

		return new ProjectileDamage(objectHolder, eggDamage, snowballDamage, windChargeDamage, thrownTridentDamage);
	};
	public static final BiConsumer<JsonWriter, ProjectileDamage> encoder = (jsonWriter, testClass) -> fields.forEach((string, field) -> {
        try {
            jsonWriter.name(string);
            Object value = field.get(testClass);
            switch (value) {
                case Double d -> jsonWriter.value(d);
                case null, default -> throw new IllegalStateException("Unexpected value: " + value);
            }

        } catch (IllegalAccessException | IOException var11) {
            throw new RuntimeException(var11);
        }
    });
	public Supplier<Component> resetTranslation = null;

	public ProjectileDamage(AtlasConfig.ConfigHolder<ProjectileDamage, RegistryFriendlyByteBuf> owner, Double eggDamage, Double snowballDamage, Double windChargeDamage, Double thrownTridentDamage) {
		this.owner = owner;
		this.eggDamage = Mth.clamp(eggDamage, 0, 40D);
		this.snowballDamage = Mth.clamp(snowballDamage, 0, 40D);
		this.windChargeDamage = Mth.clamp(windChargeDamage, 0, 40D);
		this.thrownTridentDamage = Mth.clamp(thrownTridentDamage, 0, 40D);
	}
	@Override
	public void setOwnerHolder(AtlasConfig.ConfigHolder<ProjectileDamage, RegistryFriendlyByteBuf> owner) {
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
		entries.add(new DoubleListEntry(convertFieldToNameComponent.apply(this, "eggDamage"), this.eggDamage, this.resetTranslation.get(), () -> 0.0, (damage) -> this.eggDamage = Mth.clamp(damage, 0.0, 40.0), Optional::empty, false));
		entries.add(new DoubleListEntry(convertFieldToNameComponent.apply(this, "snowballDamage"), this.snowballDamage, this.resetTranslation.get(), () -> 0.0, (damage) -> this.snowballDamage = Mth.clamp(damage, 0.0, 40.0), Optional::empty, false));
		entries.add(new DoubleListEntry(convertFieldToNameComponent.apply(this, "windChargeDamage"), this.windChargeDamage, this.resetTranslation.get(), () -> 1.0, (damage) -> this.windChargeDamage = Mth.clamp(damage, 0.0, 40.0), Optional::empty, false));
		entries.add(new DoubleListEntry(convertFieldToNameComponent.apply(this, "thrownTridentDamage"), this.thrownTridentDamage, this.resetTranslation.get(), () -> 8.0, (damage) -> this.thrownTridentDamage = Mth.clamp(damage, 0.0, 40.0), Optional::empty, false));
		entries.forEach((entry) -> entry.setEditable(!this.owner.serverManaged));
		return entries;
	}
}