package net.atlas.combatify.item;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableWeaponData;
import net.atlas.combatify.extensions.ExtendedTier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.Objects;

import static net.minecraft.world.item.Item.BASE_ATTACK_DAMAGE_ID;

public class WeaponType {
	public static final StreamCodec<RegistryFriendlyByteBuf, WeaponType> STREAM_CODEC = StreamCodec.of((buf, weaponType) -> buf.writeUtf(weaponType.name), buf -> WeaponType.fromID(buf.readUtf()));
	public static final WeaponType EMPTY = createBasicUntierable("empty", 0, 0, 0);
    public static final WeaponType SWORD = createBasic("sword", 2, 0.5, 0.5);
	public static final WeaponType MACE = createBasic("mace", 1, -1.5, 0);
	public static final WeaponType LONGSWORD = createWithHoeDamageFormula("longsword", 0, 0.5, 1);
    public static final WeaponType AXE = createAxe("axe", 3, -0.5, 0);
    public static final WeaponType PICKAXE = createBasic("pickaxe", 1, 0, 0);
    public static final WeaponType HOE = createWithHoeDamageFormulaAndSpeed("hoe", 0, 1, 1);
    public static final WeaponType SHOVEL = createBasic("shovel", 0, -0.5, 0);
	public static final WeaponType KNIFE = createBasic("knife", 1, 1, 0.25);
    public static final WeaponType TRIDENT = createWithAxeDamageFormula("trident", 3, -0.5, 1);
	public final String name;
	public final double damageOffset;
	public final double speed;
	public final double reach;
	public final boolean useAxeDamage;
	public final boolean useHoeDamage;
	public final boolean useHoeSpeed;
	public final boolean tierable;

    public static final ResourceLocation BASE_ATTACK_SPEED_CTS_ID = ResourceLocation.withDefaultNamespace("base_attack_speed_cts");
    public static final ResourceLocation BASE_ATTACK_REACH_ID = ResourceLocation.withDefaultNamespace("base_attack_reach");
	public WeaponType(String name, double damageOffset, double speed, double reach, boolean useAxeDamage, boolean useHoeDamage, boolean useHoeSpeed, boolean tierable) {
		this(name, damageOffset, speed, reach, useAxeDamage, useHoeDamage, useHoeSpeed, tierable, false);
	}
	public WeaponType(String name, double damageOffset, double speed, double reach, boolean useAxeDamage, boolean useHoeDamage, boolean useHoeSpeed, boolean tierable, boolean duringSync) {
		this.name = name;
		this.damageOffset = damageOffset;
		this.speed = speed;
		this.reach = reach;
		this.useAxeDamage = useAxeDamage;
		this.useHoeDamage = useHoeDamage;
		this.useHoeSpeed = useHoeSpeed;
		this.tierable = tierable;
		if (!Objects.equals(name, "empty") && !duringSync)
			Combatify.registerWeaponType(this);
	}
	public static WeaponType createBasic(String name, double damageOffset, double speed, double reach) {
		return new WeaponType(name, damageOffset, speed, reach, false, false, false, true);
	}
	public static WeaponType createBasicUntierable(String name, double damageOffset, double speed, double reach) {
		return new WeaponType(name, damageOffset, speed, reach, false, false, false, false);
	}
	public static WeaponType createWithAxeDamageFormula(String name, double damageOffset, double speed, double reach) {
		return new WeaponType(name, damageOffset, speed, reach, true, false, false, true);
	}
	public static WeaponType createAxe(String name, double damageOffset, double speed, double reach) {
		return new WeaponType(name, damageOffset, speed, reach, true, false, false, true);
	}
	public static WeaponType createWithHoeDamageFormula(String name, double damageOffset, double speed, double reach) {
		return new WeaponType(name, damageOffset, speed, reach, false, true, false, true);
	}
	public static WeaponType createWithHoeDamageFormulaAndSpeed(String name, double damageOffset, double speed, double reach) {
		return new WeaponType(name, damageOffset, speed, reach, false, true, true, true);
	}

    public void addCombatAttributes(Tier tier, ItemAttributeModifiers.Builder attributeModifiers) {
		if (isEmpty())
			return;
        double speed = this.getSpeed(tier);
        double damage = this.getDamage(tier);
        double reach = this.getReach();
        attributeModifiers.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, damage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
		if (!Combatify.CONFIG.instaAttack())
			attributeModifiers.add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_CTS_ID, speed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        if (reach != 0.0F && Combatify.CONFIG.attackReach())
            attributeModifiers.add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(BASE_ATTACK_REACH_ID, reach, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
    }

	public double getDamage(Tier tier) {
		int modifier = Combatify.CONFIG.fistDamage() ? 1 : 0;
		double damageBonus = tier.getAttackDamageBonus() + modifier;
		boolean isNotTier1 = tier != Tiers.WOOD && tier != Tiers.GOLD && ExtendedTier.getLevel(tier) > 0;
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredWeapons.containsKey(this)) {
			ConfigurableWeaponData configurableWeaponData = Combatify.ITEMS.configuredWeapons.get(this);
			if (configurableWeaponData.damageOffset != null) {
				if (configurableWeaponData.tierable)
					return damageBonus + configurableWeaponData.damageOffset;
				else
					return modifier + configurableWeaponData.damageOffset;
			}
		}
		return damageFormula(damageBonus, tier, isNotTier1, modifier);
    }

    public double getSpeed(Tier tier) {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredWeapons.containsKey(this)) {
			ConfigurableWeaponData configurableWeaponData = Combatify.ITEMS.configuredWeapons.get(this);
			if (configurableWeaponData.speed != null)
				return configurableWeaponData.speed - Combatify.CONFIG.baseHandAttackSpeed();
		}
		return speedFormula(tier);
    }

    public double getReach() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredWeapons.containsKey(this)) {
			ConfigurableWeaponData configurableWeaponData = Combatify.ITEMS.configuredWeapons.get(this);
			if (configurableWeaponData.reach != null)
				return configurableWeaponData.reach - 2.5;
		}
		return reach;
    }
	public double getChargedReach() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredWeapons.containsKey(this)) {
			ConfigurableWeaponData configurableWeaponData = Combatify.ITEMS.configuredWeapons.get(this);
			if (configurableWeaponData.chargedReach != null)
				return configurableWeaponData.chargedReach;
		}
		return 1.0;
	}
	public boolean canSweep() {
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredWeapons.containsKey(this)) {
			ConfigurableWeaponData configurableWeaponData = Combatify.ITEMS.configuredWeapons.get(this);
			if (configurableWeaponData.canSweep != null)
				return configurableWeaponData.canSweep;
		}
		return false;
	}
	public boolean isEmpty() {
		return this == EMPTY;
	}
	public static WeaponType fromID(String id) {
		return Combatify.registeredWeaponTypes.get(id);
	}
	public double damageFormula(double damageBonus, Tier tier, boolean isNotTier1, double modifier) {
		if (!tierable)
			return modifier + damageOffset;
		else if (useAxeDamage) {
			if (!Combatify.CONFIG.ctsAttackBalancing())
				return (isNotTier1 ? tier == Tiers.NETHERITE || ExtendedTier.getLevel(tier) >= 4 ? 8 : 7 : 5) + modifier + (ExtendedTier.getLevel(tier) >= 4 && tier != Tiers.NETHERITE ? damageBonus - 3 : 0);
			else
				return damageBonus + 3.0;
		} else if (useHoeDamage) {
			if (tier != Tiers.IRON && tier != Tiers.DIAMOND && ExtendedTier.getLevel(tier) != 2 && ExtendedTier.getLevel(tier) != 3) {
				if (tier == Tiers.NETHERITE || ExtendedTier.getLevel(tier) >= 4)
					return tier == Tiers.NETHERITE ? 2 + modifier : 2 + damageBonus - 3;

				return modifier;
			}
			return 1 + modifier;
		}
		return damageBonus + damageOffset;
	}
	public double speedFormula(Tier tier) {
		if (useHoeSpeed) {
			if (tier == Tiers.WOOD)
				return -0.5;
			else if (tier == Tiers.IRON)
				return 0.5;
			else if (tier == Tiers.DIAMOND || tier == Tiers.GOLD || tier == Tiers.NETHERITE || ExtendedTier.getLevel(tier) >= 4)
				return 1.0;
			else
				return 0.0;
		}
		return speed;
	}
	public static void init() {
		Combatify.defineDefaultWeaponType(SWORD);
		Combatify.defineDefaultWeaponType(MACE);
		Combatify.defineDefaultWeaponType(LONGSWORD);
		Combatify.defineDefaultWeaponType(AXE);
		Combatify.defineDefaultWeaponType(PICKAXE);
		Combatify.defineDefaultWeaponType(HOE);
		Combatify.defineDefaultWeaponType(SHOVEL);
		Combatify.defineDefaultWeaponType(KNIFE);
		Combatify.defineDefaultWeaponType(TRIDENT);
	}
}
