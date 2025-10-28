package net.atlas.combatify.item;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableWeaponData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.IExtensibleEnum;

import java.util.UUID;

import static net.minecraft.world.item.Item.BASE_ATTACK_DAMAGE_UUID;

public enum WeaponType implements IExtensibleEnum {
	EMPTY,
    SWORD,
	LONGSWORD,
    AXE,
    PICKAXE,
    HOE,
    SHOVEL,
	KNIFE,
    TRIDENT;

	public static final UUID BASE_ATTACK_SPEED_CTS_UUID = UUID.fromString("FA233E1C-4110-4865-B01B-901007a318e1");
	public static final UUID BASE_ATTACK_REACH_UUID = UUID.fromString("26cb07a3-209d-4110-8e10-1010243614c8");

    WeaponType() {
    }

    public void addCombatAttributes(Tier tier, ImmutableMultimap.Builder<Attribute, AttributeModifier> attributeModifiers) {
		if (isEmpty()) return;
        double speed = this.getSpeed(tier);
        double damage = this.getDamage(tier);
        double reach = this.getReach();
        attributeModifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", damage, AttributeModifier.Operation.ADDITION));
		if (!Combatify.CONFIG.instaAttack.get())
			attributeModifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_CTS_UUID, "Weapon modifier", speed, AttributeModifier.Operation.ADDITION));
        if (reach != 0.0F && Combatify.CONFIG.attackReach.get()) {
            attributeModifiers.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(BASE_ATTACK_REACH_UUID, "Weapon modifier", reach, AttributeModifier.Operation.ADDITION));
        }
    }
	public void addCombatAttributes(Tier tier, ArrayListMultimap<Attribute, AttributeModifier> attributeModifiers) {
		if (isEmpty()) return;
		double speed = this.getSpeed(tier);
		double damage = this.getDamage(tier);
		double reach = this.getReach();
		attributeModifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", damage, AttributeModifier.Operation.ADDITION));
		if (!Combatify.CONFIG.instaAttack.get())
			attributeModifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_CTS_UUID, "Weapon modifier", speed, AttributeModifier.Operation.ADDITION));
		if (reach != 0.0F && Combatify.CONFIG.attackReach.get()) {
			attributeModifiers.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(BASE_ATTACK_REACH_UUID, "Weapon modifier", reach, AttributeModifier.Operation.ADDITION));
		}
	}

	public double getDamage(Tier tier) {
		int modifier = Combatify.CONFIG.fistDamage.get() ? 1 : 0;
		double damageBonus = tier.getAttackDamageBonus() + modifier;
		boolean isNotTier1 = tier != Tiers.WOOD && tier != Tiers.GOLD && damageBonus != (Combatify.CONFIG.fistDamage.get() ? 1 : 0);
		boolean isCTSNotT1 = isNotTier1 && Combatify.CONFIG.ctsAttackBalancing.get();
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredWeapons.containsKey(this)) {
			ConfigurableWeaponData configurableWeaponData = Combatify.ITEMS.configuredWeapons.get(this);
			if (configurableWeaponData.damageOffset != null) {
				if (configurableWeaponData.tierable) {
					if (isCTSNotT1)
						return damageBonus + configurableWeaponData.damageOffset;
					else
						return damageBonus + configurableWeaponData.damageOffset + 1.0;
				} else
					return modifier + configurableWeaponData.damageOffset;
			}
		}
		switch (this) {
			case KNIFE, PICKAXE -> {
				if (isCTSNotT1) {
					return damageBonus;
				} else {
					return damageBonus + 1.0;
				}
			}
			case SWORD -> {
				if (isCTSNotT1) {
					return damageBonus + 1.0;
				} else {
					return damageBonus + 2.0;
				}
			}
			case AXE -> {
				if (!Combatify.CONFIG.ctsAttackBalancing.get()) {
					return (isNotTier1 ? tier == Tiers.NETHERITE ? 8 : 7 : 5) + modifier;
				} else if (isCTSNotT1) {
					return damageBonus + 2;
				} else {
					return damageBonus + 3.0;
				}
			}
			case LONGSWORD, HOE -> {
				if (tier != Tiers.IRON && tier != Tiers.DIAMOND) {
					if (tier == Tiers.NETHERITE || tier.getLevel() >= 4)
						return tier == Tiers.NETHERITE ? 2 + modifier : 2 + damageBonus - 4 + modifier;

					return modifier;
				}
				return 1 + modifier;
			}
			case SHOVEL -> {
				return damageBonus;
			}
			case TRIDENT -> {
				return 5 + modifier + (Combatify.CONFIG.ctsAttackBalancing.get() ? 0 : 2);
			}
			default -> {
				return 0.0 + modifier;
			}
		}
    }

    public double getSpeed(Tier tier) {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredWeapons.containsKey(this)) {
			ConfigurableWeaponData configurableWeaponData = Combatify.ITEMS.configuredWeapons.get(this);
			if (configurableWeaponData.speed != null)
				return configurableWeaponData.speed - Combatify.CONFIG.baseHandAttackSpeed.get();
		}
		switch (this) {
			case KNIFE -> {
				return 1.0;
			}
			case LONGSWORD, SWORD -> {
				return 0.5;
			}
			case AXE, SHOVEL, TRIDENT -> {
				return -0.5;
			}
			case HOE -> {
				if (tier == Tiers.WOOD)
					return -0.5;
				else if (tier == Tiers.IRON)
					return 0.5;
				else if (tier == Tiers.DIAMOND || tier == Tiers.GOLD)
					return 1.0;
				else {
					if (tier == Tiers.NETHERITE || tier.getLevel() >= 4)
						return 1.0;

					return 0;
				}
			}
			default -> {
				return 0.0;
			}
		}
    }

    public double getReach() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredWeapons.containsKey(this)) {
			ConfigurableWeaponData configurableWeaponData = Combatify.ITEMS.configuredWeapons.get(this);
			if (configurableWeaponData.reach != null)
				return configurableWeaponData.reach - 2.5;
		}
		return switch (this) {
			case SWORD -> 0.5;
			case LONGSWORD, HOE, TRIDENT -> 1;
			default -> 0;
		};
    }
	public double getChargedReach() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredWeapons.containsKey(this)) {
			ConfigurableWeaponData configurableWeaponData = Combatify.ITEMS.configuredWeapons.get(this);
			if (configurableWeaponData.chargedReach != null)
				return configurableWeaponData.chargedReach;
		}
		return 1.0;
	}
	public boolean hasSwordEnchants() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredWeapons.containsKey(this)) {
			ConfigurableWeaponData configurableWeaponData = Combatify.ITEMS.configuredWeapons.get(this);
			if (configurableWeaponData.hasSwordEnchants != null)
				return configurableWeaponData.hasSwordEnchants;
		}
		return switch (this) {
			case SWORD, AXE, LONGSWORD, KNIFE -> true;
			default -> false;
		};
	}
	public boolean isEmpty() {
		return this == EMPTY;
	}
	public static WeaponType fromID(String id) {
		return valueOf(id);
	}

	@SuppressWarnings("unused")
	public static WeaponType create(String name) {
		throw new IllegalStateException("Enum not extended");
	}
}
