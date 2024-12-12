package net.atlas.combatify.config;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public class ConfigurableEntityData {
	public static final StreamCodec<RegistryFriendlyByteBuf, ConfigurableEntityData> ENTITY_DATA_STREAM_CODEC = StreamCodec.of((buf, configurableEntityData) -> {
		buf.writeVarInt(configurableEntityData.attackInterval == null ? -10 : configurableEntityData.attackInterval);
		buf.writeDouble(configurableEntityData.shieldDisableTime == null ? -10 : configurableEntityData.shieldDisableTime);
		buf.writeInt(configurableEntityData.isMiscEntity == null ? -10 : configurableEntityData.isMiscEntity ? 1 : 0);
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
	public final Integer attackInterval;
	public final Double shieldDisableTime;
	public final Boolean isMiscEntity;

    public ConfigurableEntityData(Integer attackInterval, Double shieldDisableTime, Boolean isMiscEntity) {
		this.attackInterval = clamp(attackInterval, 0, 1000);
		this.shieldDisableTime = clamp(shieldDisableTime, 0, 10);
		this.isMiscEntity = isMiscEntity;
    }
	public static Integer max(Integer value, int min) {
		if (value == null)
			return null;
		return Math.max(value, min);
	}

	public static Double max(Double value, double min) {
		if (value == null)
			return null;
		return Math.max(value, min);
	}

	public static Integer clamp(Integer value, int min, int max) {
		if (value == null)
			return null;
		return Math.min(Math.max(value, min), max);
	}

	public static Double clamp(Double value, double min, double max) {
		if (value == null)
			return null;
		return value < min ? min : Math.min(value, max);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ConfigurableEntityData that)) return false;
        return Objects.equals(attackInterval, that.attackInterval);
	}

	@Override
	public int hashCode() {
		return Objects.hash(attackInterval);
	}
}
