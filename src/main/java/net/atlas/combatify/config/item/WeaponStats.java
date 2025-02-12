package net.atlas.combatify.config.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;
import java.util.Optional;

import static net.atlas.combatify.config.ConfigurableItemData.clamp;

public record WeaponStats(Optional<Double> optionalChargedReach) {
	public static final WeaponStats EMPTY = new WeaponStats((Double) null);
	public static final MapCodec<WeaponStats> CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(Codec.DOUBLE.optionalFieldOf("charged_reach").forGetter(WeaponStats::optionalChargedReach))
			.apply(instance, WeaponStats::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, WeaponStats> STREAM_CODEC = StreamCodec.of((buf, weaponStats) -> {
		buf.writeDouble(weaponStats.optionalChargedReach.orElse(-10D));
	}, buf -> {
		Double chargedReach = buf.readDouble();
		if (chargedReach == -10)
			chargedReach = null;
		return new WeaponStats(chargedReach);
	});
	public WeaponStats(Double optionalChargedReach) {
		this(Optional.ofNullable(optionalChargedReach));
	}
	public WeaponStats(Optional<Double> optionalChargedReach) {
		this.optionalChargedReach = clamp(optionalChargedReach, 0, 10);
	}

	public Double chargedReach() {
		return optionalChargedReach.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof WeaponStats that)) return false;
        return Objects.equals(optionalChargedReach, that.optionalChargedReach);
	}

	@Override
	public int hashCode() {
		return Objects.hash(optionalChargedReach);
	}
}
