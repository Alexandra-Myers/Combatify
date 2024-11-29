package net.atlas.combatify.config;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.DoubleListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;
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

import static net.atlas.atlascore.config.AtlasConfig.*;

public class CritControls implements ConfigRepresentable<CritControls> {
	public static final CritControls DEFAULT = new CritControls(null, true, false, 0, 195, 1.25, 1.5);
	public static final StreamCodec<RegistryFriendlyByteBuf, CritControls> STREAM_CODEC = new StreamCodec<>() {
        public void encode(RegistryFriendlyByteBuf registryFriendlyByteBuf, CritControls projectileDamage) {
            registryFriendlyByteBuf.writeResourceLocation(projectileDamage.owner.heldValue.owner().name);
            registryFriendlyByteBuf.writeUtf(projectileDamage.owner.heldValue.name());
            registryFriendlyByteBuf.writeBoolean(projectileDamage.sprintCritsEnabled);
			registryFriendlyByteBuf.writeBoolean(projectileDamage.chargedOrUncharged);
			registryFriendlyByteBuf.writeVarInt(projectileDamage.minCharge);
			registryFriendlyByteBuf.writeVarInt(projectileDamage.chargedCritCharge);
			registryFriendlyByteBuf.writeDouble(projectileDamage.unchargedCritMultiplier);
			registryFriendlyByteBuf.writeDouble(projectileDamage.fullCritMultiplier);
        }

        @NotNull
		@SuppressWarnings("unchecked")
        public CritControls decode(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            AtlasConfig config = AtlasConfig.configs.get(registryFriendlyByteBuf.readResourceLocation());
            return new CritControls((AtlasConfig.ConfigHolder<CritControls, RegistryFriendlyByteBuf>) config.valueNameToConfigHolderMap.get(registryFriendlyByteBuf.readUtf()), registryFriendlyByteBuf.readBoolean(), registryFriendlyByteBuf.readBoolean(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readDouble(), registryFriendlyByteBuf.readDouble());
        }
    };
	public AtlasConfig.ConfigHolder<CritControls, RegistryFriendlyByteBuf> owner;
	public Boolean sprintCritsEnabled;
	public Boolean chargedOrUncharged;
	public Integer minCharge;
	public Integer chargedCritCharge;
	public Double unchargedCritMultiplier;
	public Double fullCritMultiplier;
	public static final Map<String, Field> fields = Util.make(new HashMap<>(), (hashMap) -> {
		try {
			hashMap.put("sprintCritsEnabled", CritControls.class.getDeclaredField("sprintCritsEnabled"));
			hashMap.put("chargedOrUncharged", CritControls.class.getDeclaredField("chargedOrUncharged"));
			hashMap.put("minCharge", CritControls.class.getDeclaredField("minCharge"));
			hashMap.put("chargedCritCharge", CritControls.class.getDeclaredField("chargedCritCharge"));
			hashMap.put("unchargedCritMultiplier", CritControls.class.getDeclaredField("unchargedCritMultiplier"));
			hashMap.put("fullCritMultiplier", CritControls.class.getDeclaredField("fullCritMultiplier"));
		} catch (NoSuchFieldException ignored) {
		}

	});
	public static final BiFunction<CritControls, String, Component> convertFieldToComponent = (projectileDamage, string) -> {
		try {
			return Component.translatable(projectileDamage.owner.getTranslationKey() + "." + string).append(Component.literal(": ")).append(Component.literal(String.valueOf(projectileDamage.fieldRepresentingHolder(string).get(projectileDamage))));
		} catch (IllegalAccessException var3) {
			return Component.translatable(projectileDamage.owner.getTranslationKey() + "." + string);
		}
	};
	public static final BiFunction<CritControls, String, Component> convertFieldToNameComponent = (projectileDamage, string) -> Component.translatable(projectileDamage.owner.getTranslationKey() + "." + string);
	public static final BiFunction<CritControls, String, Component> convertFieldToValueComponent = (projectileDamage, string) -> {
		try {
			return Component.literal(String.valueOf(projectileDamage.fieldRepresentingHolder(string).get(projectileDamage)));
		} catch (IllegalAccessException var3) {
			return Component.translatable(projectileDamage.owner.getTranslationKey() + "." + string);
		}
	};
	public static final BiFunction<AtlasConfig.ConfigHolder<CritControls, RegistryFriendlyByteBuf>, JsonObject, CritControls> decoder = (objectHolder, jsonObject) -> {
		Boolean sprintCritsEnabled = true;
		Boolean chargedOrUncharged = false;
		Integer minCharge = 0;
		Integer chargedCritCharge = 195;
		Double unchargedCritMultiplier = 1.25;
		Double fullCritMultiplier = 1.5;
		if (jsonObject.has("sprintCritsEnabled")) {
			sprintCritsEnabled = getBoolean(jsonObject, "sprintCritsEnabled");
		}
		if (jsonObject.has("chargedOrUncharged")) {
			chargedOrUncharged = getBoolean(jsonObject, "chargedOrUncharged");
		}
		if (jsonObject.has("minCharge")) {
			minCharge = getInt(jsonObject, "minCharge");
		}
		if (jsonObject.has("chargedCritCharge")) {
			chargedCritCharge = getInt(jsonObject, "chargedCritCharge");
		}
		if (jsonObject.has("unchargedCritMultiplier")) {
			unchargedCritMultiplier = getDouble(jsonObject, "unchargedCritMultiplier");
		}
		if (jsonObject.has("fullCritMultiplier")) {
			fullCritMultiplier = getDouble(jsonObject, "fullCritMultiplier");
		}

		return new CritControls(objectHolder, sprintCritsEnabled, chargedOrUncharged, minCharge, chargedCritCharge, unchargedCritMultiplier, fullCritMultiplier);
	};
	public static final BiConsumer<JsonWriter, CritControls> encoder = (jsonWriter, testClass) -> fields.forEach((string, field) -> {
        try {
            jsonWriter.name(string);
            Object value = field.get(testClass);
            switch (value) {
				case Boolean b -> jsonWriter.value(b);
                case Integer d -> jsonWriter.value(d);
				case Double d -> jsonWriter.value(d);
                case null, default -> throw new IllegalStateException("Unexpected value: " + value);
            }

        } catch (IllegalAccessException | IOException var11) {
            throw new RuntimeException(var11);
        }
    });
	public Supplier<Component> resetTranslation = null;

	public CritControls(AtlasConfig.ConfigHolder<CritControls, RegistryFriendlyByteBuf> owner, Boolean sprintCritsEnabled, Boolean chargedOrUncharged, Integer minCharge, Integer chargedCritCharge, Double unchargedCritMultiplier, Double fullCritMultiplier) {
		this.owner = owner;
		this.sprintCritsEnabled = sprintCritsEnabled;
		this.chargedOrUncharged = chargedOrUncharged;
		this.minCharge = Mth.clamp(minCharge, 0, 200);
		this.chargedCritCharge = Mth.clamp(chargedCritCharge, 0, 200);
		this.unchargedCritMultiplier = Mth.clamp(unchargedCritMultiplier, 1, 4);
		this.fullCritMultiplier = Mth.clamp(fullCritMultiplier, 1, 4);
	}
	@Override
	public void setOwnerHolder(AtlasConfig.ConfigHolder<CritControls, RegistryFriendlyByteBuf> owner) {
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
			case Double ignored -> DoubleArgumentType.doubleArg(1, 4);
			case null, default -> null;
		};
	}

	@Override
	@Environment(EnvType.CLIENT)
	@SuppressWarnings("all")
	public List<AbstractConfigListEntry<?>> transformIntoConfigEntries() {
		if (this.resetTranslation == null) this.resetTranslation = () -> Component.translatable(this.owner.getTranslationResetKey());
		List<AbstractConfigListEntry<?>> entries = new ArrayList<>();
		entries.add(new BooleanListEntry(convertFieldToNameComponent.apply(this, "sprintCritsEnabled"), this.sprintCritsEnabled, this.resetTranslation.get(), () -> true, (sprintCritsEnabled) -> this.sprintCritsEnabled = sprintCritsEnabled, setupTooltip(1, "sprintCritsEnabled"), false));
		entries.add(new BooleanListEntry(convertFieldToNameComponent.apply(this, "chargedOrUncharged"), this.chargedOrUncharged, this.resetTranslation.get(), () -> false, (chargedOrUncharged) -> this.chargedOrUncharged = chargedOrUncharged, setupTooltip(1, "chargedOrUncharged"), false));
		entries.add(new IntegerListEntry(convertFieldToNameComponent.apply(this, "minCharge"), this.minCharge, this.resetTranslation.get(), () -> 0, (charge) -> this.minCharge = Mth.clamp(charge, 0, 200), setupTooltip(1, "minCharge"), false));
		entries.add(new IntegerListEntry(convertFieldToNameComponent.apply(this, "chargedCritCharge"), this.chargedCritCharge, this.resetTranslation.get(), () -> 195, (charge) -> this.chargedCritCharge = Mth.clamp(charge, 0, 200), setupTooltip(1, "chargedCritCharge"), false));
		entries.add(new DoubleListEntry(convertFieldToNameComponent.apply(this, "unchargedCritMultiplier"), this.unchargedCritMultiplier, this.resetTranslation.get(), () -> 1.25, (percent) -> this.unchargedCritMultiplier = Mth.clamp(percent, 1, 4), setupTooltip(1, "unchargedCritMultiplier"), false));
		entries.add(new DoubleListEntry(convertFieldToNameComponent.apply(this, "fullCritMultiplier"), this.fullCritMultiplier, this.resetTranslation.get(), () -> 1.5, (percent) -> this.fullCritMultiplier = Mth.clamp(percent, 1, 4), setupTooltip(1, "fullCritMultiplier"), false));
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
