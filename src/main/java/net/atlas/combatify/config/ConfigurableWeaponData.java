package net.atlas.combatify.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.combatify.component.custom.Blocker;
import net.atlas.combatify.component.custom.CanSweep;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;
import java.util.Optional;

import static net.atlas.combatify.config.ConfigurableItemData.clamp;

public record ConfigurableWeaponData(Optional<Double> optionalDamage, Optional<Double> optionalSpeed, Optional<Double> optionalReach, Optional<Double> optionalChargedReach, Optional<Double> optionalPiercingLevel, Optional<Boolean> optionalTiered, Optional<CanSweep> optionalCanSweep, Optional<Blocker> optionalBlocking) {
	public static final ConfigurableWeaponData EMPTY = new ConfigurableWeaponData((Double) null, null, null, null, null, null, null, null);
	public static final Codec<ConfigurableWeaponData> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(Codec.DOUBLE.optionalFieldOf("damage_offset").forGetter(ConfigurableWeaponData::optionalDamage),
				Codec.DOUBLE.optionalFieldOf("speed").forGetter(ConfigurableWeaponData::optionalSpeed),
				Codec.DOUBLE.optionalFieldOf("reach").forGetter(ConfigurableWeaponData::optionalReach),
				Codec.DOUBLE.optionalFieldOf("charged_reach").forGetter(ConfigurableWeaponData::optionalChargedReach),
				Codec.DOUBLE.optionalFieldOf("armor_piercing").forGetter(ConfigurableWeaponData::optionalPiercingLevel),
				Codec.BOOL.optionalFieldOf("tierable").forGetter(ConfigurableWeaponData::optionalTiered),
				CanSweep.CODEC.optionalFieldOf("can_sweep").forGetter(ConfigurableWeaponData::optionalCanSweep),
				Blocker.SIMPLE_CODEC.optionalFieldOf("blocker").forGetter(ConfigurableWeaponData::optionalBlocking))
			.apply(instance, ConfigurableWeaponData::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, ConfigurableWeaponData> WEAPON_DATA_STREAM_CODEC = StreamCodec.of((buf, configurableWeaponData) -> {
		buf.writeDouble(configurableWeaponData.optionalDamage.orElse(-10D));
		buf.writeDouble(configurableWeaponData.optionalSpeed.orElse(-10D));
		buf.writeDouble(configurableWeaponData.optionalReach.orElse(-10D));
		buf.writeDouble(configurableWeaponData.optionalChargedReach.orElse(-10D));
		buf.writeInt(configurableWeaponData.optionalTiered.map(aBoolean -> aBoolean ? 1 : 0).orElse(-10));
		buf.writeBoolean(configurableWeaponData.optionalBlocking.isPresent());
		configurableWeaponData.optionalBlocking.ifPresent(blocking -> Blocker.STREAM_CODEC.encode(buf, blocking));
		buf.writeDouble(configurableWeaponData.optionalPiercingLevel.orElse(-10D));
		buf.writeBoolean(configurableWeaponData.optionalCanSweep.isPresent());
		configurableWeaponData.optionalCanSweep.ifPresent(canSweep -> CanSweep.STREAM_CODEC.encode(buf, canSweep));
	}, buf -> {
		Double damageOffset = buf.readDouble();
		Double speed = buf.readDouble();
		Double reach = buf.readDouble();
		Double chargedReach = buf.readDouble();
		int tierableAsInt = buf.readInt();
		Boolean tierable = null;
		Boolean blockingPresent = buf.readBoolean();
		Blocker blocking = blockingPresent ? Blocker.STREAM_CODEC.decode(buf) : null;
		Double piercingLevel = buf.readDouble();
		boolean canSweepIsPresent = buf.readBoolean();
		CanSweep canSweep = canSweepIsPresent ? CanSweep.STREAM_CODEC.decode(buf) : null;
		if (damageOffset == -10)
			damageOffset = null;
		if (speed == -10)
			speed = null;
		if (reach == -10)
			reach = null;
		if (chargedReach == -10)
			chargedReach = null;
		if (tierableAsInt != -10)
			tierable = tierableAsInt == 1;
		if (piercingLevel == -10)
			piercingLevel = null;
		return new ConfigurableWeaponData(damageOffset, speed, reach, chargedReach, piercingLevel, tierable, canSweep, blocking);
	});
	public ConfigurableWeaponData(Double optionalDamage, Double optionalSpeed, Double optionalReach, Double optionalChargedReach, Double optionalPiercingLevel, Boolean optionalTiered, CanSweep optionalCanSweep, Blocker optionalBlocking) {
		this(Optional.ofNullable(optionalDamage), Optional.ofNullable(optionalSpeed), Optional.ofNullable(optionalReach), Optional.ofNullable(optionalChargedReach), Optional.ofNullable(optionalPiercingLevel), Optional.ofNullable(optionalTiered), Optional.ofNullable(optionalCanSweep), Optional.ofNullable(optionalBlocking));
	}
	public ConfigurableWeaponData(Optional<Double> optionalDamage, Optional<Double> optionalSpeed, Optional<Double> optionalReach, Optional<Double> optionalChargedReach, Optional<Double> optionalPiercingLevel, Optional<Boolean> optionalTiered, Optional<CanSweep> optionalCanSweep, Optional<Blocker> optionalBlocking) {
		this.optionalDamage = clamp(optionalDamage, 0, 1000);
		this.optionalSpeed = clamp(optionalSpeed, -1, 7.5);
		this.optionalReach = clamp(optionalReach, 0, 1024);
		this.optionalChargedReach = clamp(optionalChargedReach, 0, 10);
		this.optionalPiercingLevel = clamp(optionalPiercingLevel, 0, 1);
		this.optionalTiered = optionalTiered;
		this.optionalCanSweep = optionalCanSweep;
		this.optionalBlocking = optionalBlocking;
    }

	public Double attackDamage() {
		return optionalDamage.orElse(null);
	}

	public Double attackSpeed() {
		return optionalSpeed.orElse(null);
	}

	public Double attackReach() {
		return optionalReach.orElse(null);
	}

	public Double chargedReach() {
		return optionalChargedReach.orElse(null);
	}

	public Double piercingLevel() {
		return optionalPiercingLevel.orElse(null);
	}

	public Boolean tiered() {
		return optionalTiered.orElse(null);
	}

	public CanSweep canSweep() {
		return optionalCanSweep.orElse(null);
	}

	public Blocker blocking() {
		return optionalBlocking.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ConfigurableWeaponData that)) return false;
        return Objects.equals(optionalDamage, that.optionalDamage) && Objects.equals(optionalSpeed, that.optionalSpeed) && Objects.equals(optionalReach, that.optionalReach) && Objects.equals(optionalChargedReach, that.optionalChargedReach) && Objects.equals(optionalTiered, that.optionalTiered) && Objects.equals(optionalBlocking, that.optionalBlocking) && Objects.equals(optionalPiercingLevel, that.optionalPiercingLevel) && Objects.equals(optionalCanSweep, that.optionalCanSweep);
	}

	@Override
	public int hashCode() {
		return Objects.hash(optionalDamage, optionalSpeed, optionalReach, optionalChargedReach, optionalTiered, optionalBlocking, optionalPiercingLevel, optionalCanSweep);
	}
}
