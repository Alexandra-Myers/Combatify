package net.atlas.combatify.config;

import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.BlockingType;

public class ConfigurableItemData {
	public final Double damage;
	public final Double speed;
	public final Double reach;
	public final Double chargedReach;
	public final Integer stackSize;
	public final Integer cooldown;
	public final Boolean cooldownAfter;
	public final WeaponType type;
	public final BlockingType blockingType;
	ConfigurableItemData(Double attackDamage, Double attackSpeed, Double attackReach, Double chargedReach, Integer stackSize, Integer cooldown, Boolean cooldownAfter, WeaponType weaponType, BlockingType blockingType) {
		damage = clamp(attackDamage, -10, 1000);
		speed = clamp(attackSpeed, -1, 7.5);
		reach = clamp(attackReach, 0, 1024);
		this.chargedReach = clamp(chargedReach, 0, 10);
		this.stackSize = clamp(stackSize, 1, 64);
		this.cooldown = clamp(cooldown, 1, 1000);
		this.cooldownAfter = cooldownAfter;
		type = weaponType;
		this.blockingType = blockingType;
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
