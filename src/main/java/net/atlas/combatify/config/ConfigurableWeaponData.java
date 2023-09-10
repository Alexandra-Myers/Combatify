package net.atlas.combatify.config;

public class ConfigurableWeaponData {
	public final Double damageOffset;
	public final Double speed;
	public final Double reach;
	public final Double chargedReach;
	public final Boolean tierable;
	ConfigurableWeaponData(Double attackDamage, Double attackSpeed, Double attackReach, Double chargedReach, Boolean tiered) {
		damageOffset = clamp(attackDamage, 0, 1000);
		speed = clamp(attackSpeed, -1, 7.5);
		reach = clamp(attackReach, 0, 1024);
		this.chargedReach = clamp(chargedReach, 0, 10);
		tierable = tiered;
	}

	public static Double clamp(Double d, double e, double f) {
		if (d == null)
			return d;
		return d < e ? e : Math.min(d, f);
	}
}
