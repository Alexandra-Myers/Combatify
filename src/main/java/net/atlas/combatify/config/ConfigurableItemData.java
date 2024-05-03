package net.atlas.combatify.config;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.BlockingType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

import static net.atlas.combatify.Combatify.registeredWeaponTypes;
import static net.atlas.combatify.config.ItemConfig.getTier;
import static net.atlas.combatify.config.ItemConfig.getTierName;

public class ConfigurableItemData {
	public static final StreamCodec<RegistryFriendlyByteBuf, ConfigurableItemData> ITEM_DATA_STREAM_CODEC = StreamCodec.of((buf, configurableItemData) -> {
		buf.writeDouble(configurableItemData.damage == null ? -10 : configurableItemData.damage);
		buf.writeDouble(configurableItemData.speed == null ? -10 : configurableItemData.speed);
		buf.writeDouble(configurableItemData.reach == null ? -10 : configurableItemData.reach);
		buf.writeDouble(configurableItemData.chargedReach == null ? -10 : configurableItemData.chargedReach);
		buf.writeVarInt(configurableItemData.stackSize == null ? -10 : configurableItemData.stackSize);
		buf.writeVarInt(configurableItemData.durability == null ? -10 : configurableItemData.durability);
		buf.writeInt(configurableItemData.cooldown == null ? -10 : configurableItemData.cooldown);
		if(configurableItemData.cooldown != null)
			buf.writeBoolean(configurableItemData.cooldownAfter);
		buf.writeUtf(configurableItemData.type == null ? "empty" : configurableItemData.type.name);
		buf.writeUtf(configurableItemData.blockingType == null ? "blank" : configurableItemData.blockingType.getName());
		buf.writeDouble(configurableItemData.blockStrength == null ? -10 : configurableItemData.blockStrength);
		buf.writeDouble(configurableItemData.blockKbRes == null ? -10 : configurableItemData.blockKbRes);
		buf.writeInt(configurableItemData.enchantability == null ? -10 : configurableItemData.enchantability);
		buf.writeInt(configurableItemData.isEnchantable == null ? -10 : configurableItemData.isEnchantable ? 1 : 0);
		buf.writeInt(configurableItemData.hasSwordEnchants == null ? -10 : configurableItemData.hasSwordEnchants ? 1 : 0);
		buf.writeInt(configurableItemData.isPrimaryForSwordEnchants == null ? -10 : configurableItemData.isPrimaryForSwordEnchants ? 1 : 0);
		buf.writeInt(configurableItemData.useDuration == null ? -10 : configurableItemData.useDuration);
		buf.writeDouble(configurableItemData.piercingLevel == null ? -10 : configurableItemData.piercingLevel);
		buf.writeInt(configurableItemData.canSweep == null ? -10 : configurableItemData.canSweep ? 1 : 0);
		buf.writeUtf(configurableItemData.tier == null ? "empty" : getTierName(configurableItemData.tier));
		buf.writeInt(configurableItemData.defense == null ? -10 : configurableItemData.defense);
		buf.writeDouble(configurableItemData.toughness == null ? -10 : configurableItemData.toughness);
		buf.writeDouble(configurableItemData.armourKbRes == null ? -10 : configurableItemData.armourKbRes);
		buf.writeBoolean(configurableItemData.repairIngredient == null);
		if (configurableItemData.repairIngredient != null)
			Ingredient.CONTENTS_STREAM_CODEC.encode(buf, configurableItemData.repairIngredient);
	}, buf -> {
		Double damage = buf.readDouble();
		Double speed = buf.readDouble();
		Double reach = buf.readDouble();
		Double chargedReach = buf.readDouble();
		Integer stackSize = buf.readVarInt();
		Integer durability = buf.readVarInt();
		Integer cooldown = buf.readInt();
		Boolean cooldownAfter = null;
		if (cooldown != -10)
			cooldownAfter = buf.readBoolean();
		String weaponType = buf.readUtf();
		WeaponType type = null;
		String blockingType = buf.readUtf();
		BlockingType bType = Combatify.registeredTypes.get(blockingType);
		Double blockStrength = buf.readDouble();
		Double blockKbRes = buf.readDouble();
		Integer enchantlevel = buf.readInt();
		int isEnchantableAsInt = buf.readInt();
		int hasSwordEnchantsAsInt = buf.readInt();
		int isPrimaryForSwordEnchantsAsInt = buf.readInt();
		Boolean isEnchantable = null;
		Boolean hasSwordEnchants = null;
		Boolean isPrimaryForSwordEnchants = null;
		Integer useDuration = buf.readInt();
		Double piercingLevel = buf.readDouble();
		int canSweepAsInt = buf.readInt();
		Boolean canSweep = null;
		Tier tier = getTier(buf.readUtf());
		Integer defense = buf.readInt();
		Double toughness = buf.readDouble();
		Double armourKbRes = buf.readDouble();
		boolean repairIngredientAbsent = buf.readBoolean();
		Ingredient ingredient = repairIngredientAbsent ? null : Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
		if (damage == -10)
			damage = null;
		if (speed == -10)
			speed = null;
		if (reach == -10)
			reach = null;
		if (chargedReach == -10)
			chargedReach = null;
		if (stackSize == -10)
			stackSize = null;
		if (durability == -10)
			durability = null;
		if (cooldown == -10)
			cooldown = null;
		if (blockStrength == -10)
			blockStrength = null;
		if (blockKbRes == -10)
			blockKbRes = null;
		if (enchantlevel == -10)
			enchantlevel = null;
		if (isEnchantableAsInt != -10)
			isEnchantable = isEnchantableAsInt == 1;
		if (hasSwordEnchantsAsInt != -10)
			hasSwordEnchants = hasSwordEnchantsAsInt == 1;
		if (isPrimaryForSwordEnchantsAsInt != -10)
			isPrimaryForSwordEnchants = isPrimaryForSwordEnchantsAsInt == 1;
		if (useDuration == -10)
			useDuration = null;
		if (piercingLevel == -10)
			piercingLevel = null;
		if (canSweepAsInt != -10)
			canSweep = canSweepAsInt == 1;
		if (registeredWeaponTypes.containsKey(weaponType))
			type = WeaponType.fromID(weaponType);
		if (defense == -10)
			defense = null;
		if (toughness == -10)
			toughness = null;
		if (armourKbRes == -10)
			armourKbRes = null;
		return new ConfigurableItemData(damage, speed, reach, chargedReach, stackSize, cooldown, cooldownAfter, type, bType, blockStrength, blockKbRes, enchantlevel, isEnchantable, hasSwordEnchants, isPrimaryForSwordEnchants, useDuration, piercingLevel, canSweep, tier, durability, defense, toughness, armourKbRes, ingredient);
	});
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
	public final Boolean isPrimaryForSwordEnchants;
	public final Integer useDuration;
	public final Double piercingLevel;
	public final Boolean canSweep;
	public final Tier tier;
	public final Integer defense;
	public final Double toughness;
	public final Double armourKbRes;
	public final Ingredient repairIngredient;
	ConfigurableItemData(Double attackDamage, Double attackSpeed, Double attackReach, Double chargedReach, Integer stackSize, Integer cooldown, Boolean cooldownAfter, WeaponType weaponType, BlockingType blockingType, Double blockStrength, Double blockKbRes, Integer enchantability, Boolean isEnchantable, Boolean hasSwordEnchants, Boolean isPrimaryForSwordEnchants, Integer useDuration, Double piercingLevel, Boolean canSweep, Tier tier, Integer durability, Integer defense, Double toughness, Double armourKbRes, Ingredient repairIngredient) {
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
		this.isPrimaryForSwordEnchants = isPrimaryForSwordEnchants;
		this.useDuration = clamp(useDuration, 1, 1000);
		this.piercingLevel = clamp(piercingLevel, 0, 1);
		this.canSweep = canSweep;
        this.tier = tier;
        this.defense = max(defense, 1);
        this.toughness = max(toughness, 0);
        this.armourKbRes = clamp(armourKbRes, 0, 1);
        this.repairIngredient = repairIngredient;
    }
	public static Integer max(Integer value, int min) {
		if (value == null)
			return null;
		return Math.max(value, min);
	}

	public static Double max(Double value, double min) {
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
