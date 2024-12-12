package net.atlas.combatify.config;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.BlockingType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.Objects;

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
		ArmourVariable.ARMOUR_VARIABLE_STREAM_CODEC.encode(buf, configurableItemData.durability);
		buf.writeVarInt(configurableItemData.cooldown == null ? -10 : configurableItemData.cooldown);
		if(configurableItemData.cooldown != null)
			buf.writeBoolean(configurableItemData.cooldownAfter);
		buf.writeUtf(configurableItemData.type == null ? "empty" : configurableItemData.type.name);
		buf.writeUtf(configurableItemData.blockingType == null ? "blank" : configurableItemData.blockingType.getName());
		buf.writeDouble(configurableItemData.blockStrength == null ? -10 : configurableItemData.blockStrength);
		buf.writeDouble(configurableItemData.blockKbRes == null ? -10 : configurableItemData.blockKbRes);
		buf.writeVarInt(configurableItemData.enchantability == null ? -10 : configurableItemData.enchantability);
		buf.writeInt(configurableItemData.isEnchantable == null ? -10 : configurableItemData.isEnchantable ? 1 : 0);
		buf.writeVarInt(configurableItemData.useDuration == null ? -10 : configurableItemData.useDuration);
		buf.writeDouble(configurableItemData.piercingLevel == null ? -10 : configurableItemData.piercingLevel);
		buf.writeInt(configurableItemData.canSweep == null ? -10 : configurableItemData.canSweep ? 1 : 0);
		buf.writeUtf(configurableItemData.tier == null ? "empty" : getTierName(configurableItemData.tier));
		ArmourVariable.ARMOUR_VARIABLE_STREAM_CODEC.encode(buf, configurableItemData.defense);
		buf.writeDouble(configurableItemData.toughness == null ? -10 : configurableItemData.toughness);
		buf.writeDouble(configurableItemData.armourKbRes == null ? -10 : configurableItemData.armourKbRes);
		buf.writeBoolean(configurableItemData.repairIngredient == null);
		if (configurableItemData.repairIngredient != null)
			Ingredient.CONTENTS_STREAM_CODEC.encode(buf, configurableItemData.repairIngredient);
		buf.writeBoolean(configurableItemData.toolMineableTag == null);
		if (configurableItemData.toolMineableTag != null)
			buf.writeResourceLocation(configurableItemData.toolMineableTag.location());
	}, buf -> {
		Double damage = buf.readDouble();
		Double speed = buf.readDouble();
		Double reach = buf.readDouble();
		Double chargedReach = buf.readDouble();
		Integer stackSize = buf.readVarInt();
		ArmourVariable durability = ArmourVariable.ARMOUR_VARIABLE_STREAM_CODEC.decode(buf);
		Integer cooldown = buf.readVarInt();
		Boolean cooldownAfter = null;
		if (cooldown != -10)
			cooldownAfter = buf.readBoolean();
		String weaponType = buf.readUtf();
		WeaponType type = null;
		String blockingType = buf.readUtf();
		BlockingType bType = Combatify.registeredTypes.get(blockingType);
		Double blockStrength = buf.readDouble();
		Double blockKbRes = buf.readDouble();
		Integer enchantlevel = buf.readVarInt();
		int isEnchantableAsInt = buf.readInt();
		Boolean isEnchantable = null;
		Integer useDuration = buf.readVarInt();
		Double piercingLevel = buf.readDouble();
		int canSweepAsInt = buf.readInt();
		Boolean canSweep = null;
		Tier tier = getTier(buf.readUtf());
		ArmourVariable defense = ArmourVariable.ARMOUR_VARIABLE_STREAM_CODEC.decode(buf);
		Double toughness = buf.readDouble();
		Double armourKbRes = buf.readDouble();
		boolean repairIngredientAbsent = buf.readBoolean();
		Ingredient ingredient = repairIngredientAbsent ? null : Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
		boolean toolMineableAbsent = buf.readBoolean();
		TagKey<Block> toolMineable = toolMineableAbsent ? null : TagKey.create(Registries.BLOCK, buf.readResourceLocation());
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
		if (useDuration == -10)
			useDuration = null;
		if (piercingLevel == -10)
			piercingLevel = null;
		if (canSweepAsInt != -10)
			canSweep = canSweepAsInt == 1;
		if (registeredWeaponTypes.containsKey(weaponType))
			type = WeaponType.fromID(weaponType);
		if (toughness == -10)
			toughness = null;
		if (armourKbRes == -10)
			armourKbRes = null;
        return new ConfigurableItemData(damage, speed, reach, chargedReach, stackSize, cooldown, cooldownAfter, type, bType, blockStrength, blockKbRes, enchantlevel, isEnchantable, useDuration, piercingLevel, canSweep, tier, durability, defense, toughness, armourKbRes, ingredient, toolMineable);
	});
	public final Double damage;
	public final Double speed;
	public final Double reach;
	public final Double chargedReach;
	public final Integer stackSize;
	public final ArmourVariable durability;
	public final Integer cooldown;
	public final Boolean cooldownAfter;
	public final WeaponType type;
	public final BlockingType blockingType;
	public final Double blockStrength;
	public final Double blockKbRes;
	public final Integer enchantability;
	public final Boolean isEnchantable;
	public final Integer useDuration;
	public final Double piercingLevel;
	public final Boolean canSweep;
	public final Tier tier;
	public final ArmourVariable defense;
	public final Double toughness;
	public final Double armourKbRes;
	public final Ingredient repairIngredient;
	public final TagKey<Block> toolMineableTag;

    public ConfigurableItemData(Double attackDamage, Double attackSpeed, Double attackReach, Double chargedReach, Integer stackSize, Integer cooldown, Boolean cooldownAfter, WeaponType weaponType, BlockingType blockingType, Double blockStrength, Double blockKbRes, Integer enchantability, Boolean isEnchantable, Integer useDuration, Double piercingLevel, Boolean canSweep, Tier tier, ArmourVariable durability, ArmourVariable defense, Double toughness, Double armourKbRes, Ingredient repairIngredient, TagKey<Block> toolMineableTag) {
		damage = clamp(attackDamage, 0, 1000);
		speed = clamp(attackSpeed, -1, 7.5);
		reach = clamp(attackReach, 0, 1024);
		this.chargedReach = clamp(chargedReach, 0, 10);
		this.stackSize = clamp(stackSize, 1, 99);
		this.durability = durability;
		this.cooldown = clamp(cooldown, 1, 1000);
		this.cooldownAfter = cooldownAfter == null || cooldownAfter;
		type = weaponType;
		this.blockingType = blockingType;
		this.blockStrength = clamp(blockStrength, 0, 1000);
		this.blockKbRes = clamp(blockKbRes, 0, 1);
		this.enchantability = clamp(enchantability, 0, 1000);
		this.isEnchantable = isEnchantable;
		this.useDuration = clamp(useDuration, 1, 1000);
		this.piercingLevel = clamp(piercingLevel, 0, 1);
		this.canSweep = canSweep;
        this.tier = tier;
        this.defense = defense;
        this.toughness = max(toughness, 0);
        this.armourKbRes = clamp(armourKbRes, 0, 1);
        this.repairIngredient = repairIngredient;
        this.toolMineableTag = toolMineableTag;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ConfigurableItemData that)) return false;
        return Objects.equals(damage, that.damage) && Objects.equals(speed, that.speed) && Objects.equals(reach, that.reach) && Objects.equals(chargedReach, that.chargedReach) && Objects.equals(stackSize, that.stackSize) && Objects.equals(durability, that.durability) && Objects.equals(cooldown, that.cooldown) && Objects.equals(cooldownAfter, that.cooldownAfter) && Objects.equals(type, that.type) && Objects.equals(blockingType, that.blockingType) && Objects.equals(blockStrength, that.blockStrength) && Objects.equals(blockKbRes, that.blockKbRes) && Objects.equals(enchantability, that.enchantability) && Objects.equals(isEnchantable, that.isEnchantable) && Objects.equals(useDuration, that.useDuration) && Objects.equals(piercingLevel, that.piercingLevel) && Objects.equals(canSweep, that.canSweep) && Objects.equals(tier, that.tier) && Objects.equals(defense, that.defense) && Objects.equals(toughness, that.toughness) && Objects.equals(armourKbRes, that.armourKbRes) && Objects.equals(repairIngredient, that.repairIngredient) && Objects.equals(toolMineableTag, that.toolMineableTag);
	}

	@Override
	public int hashCode() {
		return Objects.hash(damage, speed, reach, chargedReach, stackSize, durability, cooldown, cooldownAfter, type, blockingType, blockStrength, blockKbRes, enchantability, isEnchantable, useDuration, piercingLevel, canSweep, tier, defense, toughness, armourKbRes, repairIngredient, toolMineableTag);
	}
}
