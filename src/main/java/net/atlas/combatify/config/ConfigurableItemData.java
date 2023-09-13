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
	public final Double blockStrength;
	public final Double blockKbRes;
	public final Integer enchantability;
	ConfigurableItemData(Double attackDamage, Double attackSpeed, Double attackReach, Double chargedReach, Integer stackSize, Integer cooldown, Boolean cooldownAfter, WeaponType weaponType, BlockingType blockingType, Double blockStrength, Double blockKbRes, Integer enchantability) {
		damage = clamp(attackDamage, -10, 1000);
		speed = clamp(attackSpeed, -1, 7.5);
		reach = clamp(attackReach, 0, 1024);
		this.chargedReach = clamp(chargedReach, 0, 10);
		this.stackSize = clamp(stackSize, 1, 64);
		this.cooldown = clamp(cooldown, 1, 1000);
		this.cooldownAfter = cooldownAfter;
		type = weaponType;
		this.blockingType = blockingType;
		this.blockStrength = clamp(blockStrength, 0, 1000);
		this.blockKbRes = clamp(blockKbRes, 0, 1);
		this.enchantability = clamp(enchantability, 0, 1000);
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
