package net.atlas.combatify.config;

import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.BlockingType;
import net.minecraft.world.item.Tier;

public class ConfigurableItemData {
	public final Double damage;
	public final Double speed;
	public final Double reach;
	public final Double chargedReach;
	public final Integer stackSize;
	public final Integer durability;
	public final Integer cooldown;
	public final Boolean cooldownAfter;
	public final WeaponType type;
	public final BlockingType blockingType;
	public final Double blockStrength;
	public final Double blockKbRes;
	public final Integer enchantability;
	public final Boolean isEnchantable;
	public final Boolean hasSwordEnchants;
	public final Integer useDuration;
	public final Double piercingLevel;
	public final Boolean canSweep;
	public final Tier tier;
	ConfigurableItemData(Double attackDamage, Double attackSpeed, Double attackReach, Double chargedReach, Integer stackSize, Integer cooldown, Boolean cooldownAfter, WeaponType weaponType, BlockingType blockingType, Double blockStrength, Double blockKbRes, Integer enchantability, Boolean isEnchantable, Boolean hasSwordEnchants, Integer useDuration, Double piercingLevel, Boolean canSweep, Tier tier, Integer durability) {
		damage = clamp(attackDamage, -10, 1000);
		speed = clamp(attackSpeed, -1, 7.5);
		reach = clamp(attackReach, 0, 1024);
		this.chargedReach = clamp(chargedReach, 0, 10);
		this.stackSize = clamp(stackSize, 1, 128);
		this.durability = max(durability, 1);
		this.cooldown = clamp(cooldown, 1, 1000);
		this.cooldownAfter = cooldownAfter;
		type = weaponType;
		this.blockingType = blockingType;
		this.blockStrength = clamp(blockStrength, 0, 1000);
		this.blockKbRes = clamp(blockKbRes, 0, 1);
		this.enchantability = clamp(enchantability, 0, 1000);
		this.isEnchantable = isEnchantable;
		this.hasSwordEnchants = hasSwordEnchants;
		this.useDuration = clamp(useDuration, 1, 1000);
		this.piercingLevel = clamp(piercingLevel, 0, 1);
		this.canSweep = canSweep;
        this.tier = tier;
    }
	public static Integer max(Integer value, int min) {
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
