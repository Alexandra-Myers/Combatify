package net.atlas.combatify.config;

public class ConfigurableItemData {
	public final Double damage;
	public final Double speed;
	public final Double reach;
	public final Double chargedReach;
	public final Integer stackSize;
	ConfigurableItemData(Double attackDamage, Double attackSpeed, Double attackReach, Double chargedReach, Integer stackSize) {
		damage = clamp(attackDamage, 0, 1000);
		speed = clamp(attackSpeed, -1, 7.5);
		reach = clamp(attackReach, 0, 1024);
		this.chargedReach = clamp(chargedReach, 0, 10);
		this.stackSize = clamp(stackSize, 1, 64);
	}
	public static Integer clamp(Integer i, int j, int k) {
		if (i == null)
			return i;
		return Math.min(Math.max(i, j), k);
	}

	public static Double clamp(Double d, double e, double f) {
		if (d == null)
			return d;
		return d < e ? e : Math.min(d, f);
	}
}
