package net.atlas.combatify.config;

import net.atlas.combatify.util.BlockingType;

public class ConfigurableWeaponData {
	public final Double damageOffset;
	public final Double speed;
	public final Double reach;
	public final Double chargedReach;
	public final Boolean tierable;
	public final BlockingType blockingType;
	ConfigurableWeaponData(Double attackDamage, Double attackSpeed, Double attackReach, Double chargedReach, Boolean tiered, BlockingType blockingType) {
		damageOffset = clamp(attackDamage, 0, 1000);
		speed = clamp(attackSpeed, -1, 7.5);
		reach = clamp(attackReach, 0, 1024);
		this.chargedReach = clamp(chargedReach, 0, 10);
		tierable = tiered;
		this.blockingType = blockingType;
	}

	public static Double clamp(Double d, double e, double f) {
		if (d == null)
			return d;
		return d < e ? e : Math.min(d, f);
	}
}
