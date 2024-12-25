package net.atlas.combatify.config;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.DoubleListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;
import net.atlas.atlascore.config.AtlasConfig;
import net.atlas.atlascore.util.Codecs;
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

import static net.atlas.atlascore.config.AtlasConfig.ConfigHolder;

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
            return new CritControls((AtlasConfig.ConfigHolder<CritControls>) config.valueNameToConfigHolderMap.get(registryFriendlyByteBuf.readUtf()), registryFriendlyByteBuf.readBoolean(), registryFriendlyByteBuf.readBoolean(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readDouble(), registryFriendlyByteBuf.readDouble());
        }
    };
	public AtlasConfig.ConfigHolder<CritControls> owner;
	public Boolean sprintCritsEnabled;
	public Boolean chargedOrUncharged;
	public Integer minCharge;
	public Integer chargedCritCharge;
	public Double unchargedCritMultiplier;
	public Double fullCritMultiplier;
	public Boolean sprintCritsEnabled() {
		return sprintCritsEnabled;
	}
	public Boolean chargedOrUncharged() {
		return chargedOrUncharged;
	}
	public Integer minCharge() {
		return minCharge;
	}
	public Integer chargedCritCharge() {
		return chargedCritCharge;
	}
	public Double unchargedCritMultiplier() {
		return unchargedCritMultiplier;
	}
	public Double fullCritMultiplier() {
		return fullCritMultiplier;
	}
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
	public Supplier<Component> resetTranslation = null;

	public CritControls(AtlasConfig.ConfigHolder<CritControls> owner, Boolean sprintCritsEnabled, Boolean chargedOrUncharged, Integer minCharge, Integer chargedCritCharge, Double unchargedCritMultiplier, Double fullCritMultiplier) {
		this.owner = owner;
		this.sprintCritsEnabled = sprintCritsEnabled;
		this.chargedOrUncharged = chargedOrUncharged;
		this.minCharge = Mth.clamp(minCharge, 0, 200);
		this.chargedCritCharge = Mth.clamp(chargedCritCharge, 0, 200);
		this.unchargedCritMultiplier = Mth.clamp(unchargedCritMultiplier, 1, 4);
		this.fullCritMultiplier = Mth.clamp(fullCritMultiplier, 1, 4);
	}

	@Override
	public Codec<CritControls> getCodec(ConfigHolder<CritControls> configHolder) {
		return RecordCodecBuilder.create(instance ->
			instance.group(Codec.BOOL.optionalFieldOf("sprintCritsEnabled", true).forGetter(CritControls::sprintCritsEnabled),
				Codec.BOOL.optionalFieldOf("chargedOrUncharged", false).forGetter(CritControls::chargedOrUncharged),
				ExtraCodecs.intRange(0, 200).optionalFieldOf("minCharge", 0).forGetter(CritControls::minCharge),
				ExtraCodecs.intRange(0, 200).optionalFieldOf("chargedCritCharge", 195).forGetter(CritControls::chargedCritCharge),
				Codecs.doubleRange(1, 4).optionalFieldOf("unchargedCritMultiplier", 1.25).forGetter(CritControls::unchargedCritMultiplier),
				Codecs.doubleRange(1, 4).optionalFieldOf("fullCritMultiplier", 1.5).forGetter(CritControls::fullCritMultiplier))
				.apply(instance, (sprintCritsEnabled, chargedOrUncharged, minCharge, chargedCritCharge, unchargedCritMultiplier, fullCritMultiplier) -> new CritControls(configHolder, sprintCritsEnabled, chargedOrUncharged, minCharge, chargedCritCharge, unchargedCritMultiplier, fullCritMultiplier)));
	}

	@Override
	public void setOwnerHolder(AtlasConfig.ConfigHolder<CritControls> owner) {
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
