package net.atlas.combatify.config;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ConfigurableEntityData {
	public static final StreamCodec<RegistryFriendlyByteBuf, ConfigurableEntityData> ENTITY_DATA_STREAM_CODEC = StreamCodec.of((buf, configurableEntityData) -> {
		buf.writeVarInt(configurableEntityData.attackInterval == null ? -10 : configurableEntityData.attackInterval);
	}, buf -> {
		Integer attackInterval = buf.readVarInt();
		if (attackInterval == -10)
			attackInterval = null;
		return new ConfigurableEntityData(attackInterval);
	});
	public final Integer attackInterval;

    ConfigurableEntityData(Integer attackInterval) {
		this.attackInterval = clamp(attackInterval, -10, 1000);
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
}
