package net.atlas.combatify.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record ConfigurableItemData(
	Optional<Double> optionalUseDuration
	//? <=1.21.1 {
	, Optional<Double> optionalCooldownSeconds
	//?}
) {

	//? <=1.21.1 {
	public ConfigurableItemData(final Double useDuration, final Double cooldownSeconds) {
		this(Optional.ofNullable(useDuration), Optional.ofNullable(cooldownSeconds));
	}
	//?}
	public static final ConfigurableItemData EMPTY = new ConfigurableItemData((Double) null);
	public static final MapCodec<ConfigurableItemData> CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(Codec.doubleRange(1.0 / 20.0, 50).optionalFieldOf("use_seconds").forGetter(ConfigurableItemData::optionalUseDuration)
				//? <=1.21.1 {
				, Codec.doubleRange(1.0 / 20.0, 50).optionalFieldOf("cooldown_seconds").forGetter(ConfigurableItemData::optionalCooldownSeconds)
				//?}
			).apply(instance, ConfigurableItemData::new));

	public ConfigurableItemData(Double useDuration) {
		//? >1.21.1 {
		/*this(Optional.ofNullable(useDuration));
		*///?} <=1.21.1 {
		this(useDuration, null);
		//?}
	}

	public Double useDuration() {
		return optionalUseDuration.orElse(null);
	}

	//? <=1.21.1 {
	public Double cooldownSeconds() {
		return optionalCooldownSeconds.orElse(null);
	}
	//?}

	public static Optional<Integer> max(Optional<Integer> value, int min) {
        return value.map(integer -> Math.max(integer, min));
    }

	public static Optional<Double> max(Optional<Double> value, double min) {
		return value.map(val -> Math.max(val, min));
	}

	public static Optional<Integer> clamp(Optional<Integer> value, int min, int max) {
		return value.map(integer -> Math.min(Math.max(integer, min), max));
	}

	public static Optional<Double> clamp(Optional<Double> value, double min, double max) {
		return value.map(val -> Math.min(Math.max(val, min), max));
	}
}
