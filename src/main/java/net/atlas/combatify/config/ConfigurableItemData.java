package net.atlas.combatify.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;
import java.util.Optional;

public record ConfigurableItemData(Optional<Double> optionalUseDuration, Optional<Double> optionalCooldownSeconds) {

	public static final ConfigurableItemData EMPTY = new ConfigurableItemData((Double) null, null);
	public static final MapCodec<ConfigurableItemData> CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(Codec.doubleRange(1.0 / 20.0, 50).optionalFieldOf("use_seconds").forGetter(ConfigurableItemData::optionalUseDuration),
				Codec.doubleRange(1.0 / 20.0, 50).optionalFieldOf("cooldown_seconds").forGetter(ConfigurableItemData::optionalCooldownSeconds))
			.apply(instance, ConfigurableItemData::new));

	public ConfigurableItemData(Double useDuration, Double cooldownSeconds) {
		this(Optional.ofNullable(useDuration), Optional.ofNullable(cooldownSeconds));
	}

	public Double useDuration() {
		return optionalUseDuration.orElse(null);
	}

	public Double cooldownSeconds() {
		return optionalCooldownSeconds.orElse(null);
	}

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ConfigurableItemData that)) return false;
        return Objects.equals(optionalUseDuration, that.optionalUseDuration) && Objects.equals(optionalCooldownSeconds, that.optionalCooldownSeconds);
	}

	@Override
	public int hashCode() {
		return Objects.hash(optionalUseDuration, optionalCooldownSeconds);
	}
}
