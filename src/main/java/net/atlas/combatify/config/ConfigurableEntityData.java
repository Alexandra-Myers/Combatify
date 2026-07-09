package net.atlas.combatify.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

import static net.atlas.combatify.config.ConfigurableItemData.clamp;

public record ConfigurableEntityData(Optional<Integer> optionalAttackInterval, Optional<Boolean> optionalIsMiscEntity) {
	public static final ConfigurableEntityData EMPTY = new ConfigurableEntityData(Optional.empty(), Optional.empty());
	public static final MapCodec<ConfigurableEntityData> CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(Codec.INT.optionalFieldOf("attack_interval").forGetter(ConfigurableEntityData::optionalAttackInterval),
			Codec.BOOL.optionalFieldOf("is_misc_entity").forGetter(ConfigurableEntityData::optionalIsMiscEntity))
			.apply(instance, ConfigurableEntityData::new));

	public ConfigurableEntityData(Optional<Integer> optionalAttackInterval, Optional<Boolean> optionalIsMiscEntity) {
		this.optionalAttackInterval = clamp(optionalAttackInterval, 0, 1000);
		this.optionalIsMiscEntity = optionalIsMiscEntity;
	}

	public Integer attackInterval() {
		return optionalAttackInterval.orElse(null);
	}

	public Boolean isMiscEntity() {
		return optionalIsMiscEntity.orElse(null);
	}
}
