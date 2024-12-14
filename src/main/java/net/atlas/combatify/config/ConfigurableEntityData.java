package net.atlas.combatify.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;
import java.util.Optional;

import static net.atlas.combatify.config.ConfigurableItemData.clamp;

public record ConfigurableEntityData(Optional<Integer> optionalAttackInterval, Optional<Double> optionalShieldDisableTime, Optional<Boolean> optionalIsMiscEntity) {
	public static final ConfigurableEntityData EMPTY = new ConfigurableEntityData((Integer) null, null, null);
	public static final Codec<ConfigurableEntityData> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(Codec.INT.optionalFieldOf("attack_interval").forGetter(ConfigurableEntityData::optionalAttackInterval),
			Codec.DOUBLE.optionalFieldOf("shield_disable_time").forGetter(ConfigurableEntityData::optionalShieldDisableTime),
			Codec.BOOL.optionalFieldOf("is_misc_entity").forGetter(ConfigurableEntityData::optionalIsMiscEntity))
			.apply(instance, ConfigurableEntityData::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, ConfigurableEntityData> ENTITY_DATA_STREAM_CODEC = StreamCodec.of((buf, configurableEntityData) -> {
		buf.writeVarInt(configurableEntityData.optionalAttackInterval.orElse(-10));
		buf.writeDouble(configurableEntityData.optionalShieldDisableTime.orElse(-10D));
		buf.writeInt(configurableEntityData.optionalIsMiscEntity.map(aBoolean -> aBoolean ? 1 : 0).orElse(-10));
	}, buf -> {
		Integer attackInterval = buf.readVarInt();
		Double shieldDisableTime = buf.readDouble();
		int isMiscEntityAsInt = buf.readInt();
		Boolean isMiscEntity = null;
		if (attackInterval == -10)
			attackInterval = null;
		if (shieldDisableTime == -10)
			shieldDisableTime = null;
		if (isMiscEntityAsInt != -10)
			isMiscEntity = isMiscEntityAsInt == 1;
		return new ConfigurableEntityData(attackInterval, shieldDisableTime, isMiscEntity);
	});

	public ConfigurableEntityData(Integer optionalAttackInterval, Double optionalShieldDisableTime, Boolean optionalIsMiscEntity) {
		this(Optional.ofNullable(optionalAttackInterval), Optional.ofNullable(optionalShieldDisableTime), Optional.ofNullable(optionalIsMiscEntity));
	}

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
