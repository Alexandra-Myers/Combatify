package net.atlas.combatify.config;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.util.BlockingType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public class ConfigurableWeaponData {
	public static final StreamCodec<RegistryFriendlyByteBuf, ConfigurableWeaponData> WEAPON_DATA_STREAM_CODEC = StreamCodec.of((buf, configurableWeaponData) -> {
		buf.writeDouble(configurableWeaponData.damageOffset == null ? -10 : configurableWeaponData.damageOffset);
		buf.writeDouble(configurableWeaponData.speed == null ? -10 : configurableWeaponData.speed);
		buf.writeDouble(configurableWeaponData.reach == null ? -10 : configurableWeaponData.reach);
		buf.writeDouble(configurableWeaponData.chargedReach == null ? -10 : configurableWeaponData.chargedReach);
		buf.writeInt(configurableWeaponData.tierable == null ? -10 : configurableWeaponData.tierable ? 1 : 0);
		buf.writeUtf(configurableWeaponData.blockingType == null ? "blank" : configurableWeaponData.blockingType.getName());
		buf.writeDouble(configurableWeaponData.piercingLevel == null ? -10 : configurableWeaponData.piercingLevel);
		buf.writeInt(configurableWeaponData.canSweep == null ? -10 : configurableWeaponData.canSweep ? 1 : 0);
	}, buf -> {
		Double damageOffset = buf.readDouble();
		Double speed = buf.readDouble();
		Double reach = buf.readDouble();
		Double chargedReach = buf.readDouble();
		int tierableAsInt = buf.readInt();
		Boolean tierable = null;
		String blockingType = buf.readUtf();
		BlockingType bType = Combatify.registeredTypes.get(blockingType);
		Double piercingLevel = buf.readDouble();
		int canSweepAsInt = buf.readInt();
		Boolean canSweep = null;
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
		if (canSweepAsInt != -10)
			canSweep = canSweepAsInt == 1;
		return new ConfigurableWeaponData(damageOffset, speed, reach, chargedReach, tierable, bType, piercingLevel, canSweep);
	});
	public final Double damageOffset;
	public final Double speed;
	public final Double reach;
	public final Double chargedReach;
	public final Boolean tierable;
	public final BlockingType blockingType;
	public final Double piercingLevel;
	public final Boolean canSweep;
	public ConfigurableWeaponData(Double attackDamage, Double attackSpeed, Double attackReach, Double chargedReach, Boolean tiered, BlockingType blockingType, Double piercingLevel, Boolean canSweep) {
		damageOffset = clamp(attackDamage, 0, 1000);
		speed = clamp(attackSpeed, -1, 7.5);
		reach = clamp(attackReach, 0, 1024);
		this.chargedReach = clamp(chargedReach, 0, 10);
		tierable = tiered;
		this.blockingType = blockingType;
		this.piercingLevel = clamp(piercingLevel, 0, 1);
        this.canSweep = canSweep;
    }

	public static Double clamp(Double value, double min, double max) {
		if (value == null)
			return null;
		return value < min ? min : Math.min(value, max);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ConfigurableWeaponData that)) return false;
        return Objects.equals(damageOffset, that.damageOffset) && Objects.equals(speed, that.speed) && Objects.equals(reach, that.reach) && Objects.equals(chargedReach, that.chargedReach) && Objects.equals(tierable, that.tierable) && Objects.equals(blockingType, that.blockingType) && Objects.equals(piercingLevel, that.piercingLevel) && Objects.equals(canSweep, that.canSweep);
	}

	@Override
	public int hashCode() {
		return Objects.hash(damageOffset, speed, reach, chargedReach, tierable, blockingType, piercingLevel, canSweep);
	}
}
