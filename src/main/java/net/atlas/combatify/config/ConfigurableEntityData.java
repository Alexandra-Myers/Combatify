package net.atlas.combatify.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;
import java.util.Optional;

import static net.atlas.combatify.config.ConfigurableItemData.clamp;

public record ConfigurableEntityData(Optional<Integer> optionalAttackInterval, Optional<Double> optionalShieldDisableTime, Optional<Boolean> optionalIsMiscEntity) {
	public static final ConfigurableEntityData EMPTY = new ConfigurableEntityData(Optional.empty(), Optional.empty(), Optional.empty());
	public static final MapCodec<ConfigurableEntityData> CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(Codec.INT.optionalFieldOf("attack_interval").forGetter(ConfigurableEntityData::optionalAttackInterval),
			Codec.DOUBLE.optionalFieldOf("shield_disable_time").forGetter(ConfigurableEntityData::optionalShieldDisableTime),
			Codec.BOOL.optionalFieldOf("is_misc_entity").forGetter(ConfigurableEntityData::optionalIsMiscEntity))
			.apply(instance, ConfigurableEntityData::new));
	public static final StreamCodec<? super ByteBuf, ConfigurableEntityData> ENTITY_DATA_STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.optional(ByteBufCodecs.VAR_INT), ConfigurableEntityData::optionalAttackInterval,
		ByteBufCodecs.optional(ByteBufCodecs.DOUBLE), ConfigurableEntityData::optionalShieldDisableTime,
		ByteBufCodecs.optional(ByteBufCodecs.BOOL), ConfigurableEntityData::optionalIsMiscEntity,
		ConfigurableEntityData::new);

	public ConfigurableEntityData(Optional<Integer> optionalAttackInterval, Optional<Double> optionalShieldDisableTime, Optional<Boolean> optionalIsMiscEntity) {
		this.optionalAttackInterval = clamp(optionalAttackInterval, 0, 1000);
		this.optionalShieldDisableTime = clamp(optionalShieldDisableTime, 0, 10);
		this.optionalIsMiscEntity = optionalIsMiscEntity;
	}

	public Integer attackInterval() {
		return optionalAttackInterval.orElse(null);
	}

	public Double shieldDisableTime() {
		return optionalShieldDisableTime.orElse(null);
	}

	public Boolean isMiscEntity() {
		return optionalIsMiscEntity.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ConfigurableEntityData that)) return false;
        return Objects.equals(optionalAttackInterval, that.optionalAttackInterval) && Objects.equals(optionalShieldDisableTime, that.optionalShieldDisableTime) && Objects.equals(optionalIsMiscEntity, that.optionalIsMiscEntity);
	}

	@Override
	public int hashCode() {
		return Objects.hash(optionalAttackInterval, optionalShieldDisableTime, optionalIsMiscEntity);
	}
}
