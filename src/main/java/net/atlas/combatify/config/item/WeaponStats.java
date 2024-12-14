package net.atlas.combatify.config.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.combatify.item.WeaponType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;
import java.util.Optional;

import static net.atlas.combatify.Combatify.registeredWeaponTypes;
import static net.atlas.combatify.config.ConfigurableItemData.clamp;

public record WeaponStats(Optional<Double> optionalDamage, Optional<Double> optionalSpeed, Optional<Double> optionalReach, Optional<Double> optionalChargedReach, Optional<Double> optionalPiercingLevel, Optional<WeaponType> optionalWeaponType, Optional<Boolean> optionalCanSweep) {
	public static final WeaponStats EMPTY = new WeaponStats((Double) null, null, null, null, null, null, null);
	public static final Codec<WeaponStats> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(Codec.DOUBLE.optionalFieldOf("damage").forGetter(WeaponStats::optionalDamage),
				Codec.DOUBLE.optionalFieldOf("speed").forGetter(WeaponStats::optionalSpeed),
				Codec.DOUBLE.optionalFieldOf("reach").forGetter(WeaponStats::optionalReach),
				Codec.DOUBLE.optionalFieldOf("charged_reach").forGetter(WeaponStats::optionalChargedReach),
				Codec.DOUBLE.optionalFieldOf("armor_piercing").forGetter(WeaponStats::optionalPiercingLevel),
				WeaponType.SIMPLE_CODEC.optionalFieldOf("weapon_type").forGetter(WeaponStats::optionalWeaponType),
				Codec.BOOL.optionalFieldOf("can_sweep").forGetter(WeaponStats::optionalCanSweep))
			.apply(instance, WeaponStats::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, WeaponStats> STREAM_CODEC = StreamCodec.of((buf, weaponStats) -> {
		buf.writeDouble(weaponStats.optionalDamage.orElse(-10D));
		buf.writeDouble(weaponStats.optionalSpeed.orElse(-10D));
		buf.writeDouble(weaponStats.optionalReach.orElse(-10D));
		buf.writeDouble(weaponStats.optionalChargedReach.orElse(-10D));
		buf.writeDouble(weaponStats.optionalPiercingLevel.orElse(-10D));
		buf.writeUtf(weaponStats.optionalWeaponType.isEmpty() ? "empty" : weaponStats.optionalWeaponType.get().name());
		buf.writeInt(weaponStats.optionalCanSweep.map(aBoolean -> aBoolean ? 1 : 0).orElse(-10));
	}, buf -> {
		Double attackDamage = buf.readDouble();
		Double attackSpeed = buf.readDouble();
		Double attackReach = buf.readDouble();
		Double chargedReach = buf.readDouble();
		Double piercingLevel = buf.readDouble();
		String weaponType = buf.readUtf();
		WeaponType type = null;
		int canSweepAsInt = buf.readInt();
		Boolean canSweep = null;
		if (attackDamage == -10)
			attackDamage = null;
		if (attackSpeed == -10)
			attackSpeed = null;
		if (attackReach == -10)
			attackReach = null;
		if (chargedReach == -10)
			chargedReach = null;
		if (piercingLevel == -10)
			piercingLevel = null;
		if (registeredWeaponTypes.containsKey(weaponType))
			type = WeaponType.fromID(weaponType);
		if (canSweepAsInt != -10)
			canSweep = canSweepAsInt == 1;
		return new WeaponStats(attackDamage, attackSpeed, attackReach, chargedReach, piercingLevel, type, canSweep);
	});
	public WeaponStats(Double optionalDamage, Double optionalSpeed, Double optionalReach, Double optionalChargedReach, Double optionalPiercingLevel, WeaponType optionalWeaponType, Boolean optionalCanSweep) {
		this(Optional.ofNullable(optionalDamage), Optional.ofNullable(optionalSpeed), Optional.ofNullable(optionalReach), Optional.ofNullable(optionalChargedReach), Optional.ofNullable(optionalPiercingLevel), Optional.ofNullable(optionalWeaponType), Optional.ofNullable(optionalCanSweep));
	}
	public WeaponStats(Optional<Double> optionalDamage, Optional<Double> optionalSpeed, Optional<Double> optionalReach, Optional<Double> optionalChargedReach, Optional<Double> optionalPiercingLevel, Optional<WeaponType> optionalWeaponType, Optional<Boolean> optionalCanSweep) {
		this.optionalDamage = clamp(optionalDamage, 0, 1000);
		this.optionalSpeed = clamp(optionalSpeed, -1, 7.5);
		this.optionalReach = clamp(optionalReach, 0, 1024);
		this.optionalChargedReach = clamp(optionalChargedReach, 0, 10);
		this.optionalPiercingLevel = clamp(optionalPiercingLevel, 0, 1);
		this.optionalWeaponType = optionalWeaponType;
		this.optionalCanSweep = optionalCanSweep;
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

	public WeaponType weaponType() {
		return optionalWeaponType.orElse(null);
	}

	public Boolean canSweep() {
		return optionalCanSweep.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof WeaponStats that)) return false;
        return Objects.equals(optionalDamage, that.optionalDamage) && Objects.equals(optionalSpeed, that.optionalSpeed) && Objects.equals(optionalReach, that.optionalReach) && Objects.equals(optionalChargedReach, that.optionalChargedReach) && Objects.equals(optionalPiercingLevel, that.optionalPiercingLevel) && Objects.equals(optionalWeaponType, that.optionalWeaponType) && Objects.equals(optionalCanSweep, that.optionalCanSweep);
	}

	@Override
	public int hashCode() {
		return Objects.hash(optionalDamage, optionalSpeed, optionalReach, optionalChargedReach, optionalPiercingLevel, optionalWeaponType, optionalCanSweep);
	}
}
