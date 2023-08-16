package net.atlas.combat_enhanced.item;

import com.google.common.collect.ImmutableMultimap;
import net.atlas.combat_enhanced.CombatEnhanced;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

import java.util.UUID;

public enum WeaponType {
    SWORD,
	LONGSWORD,
    AXE,
    PICKAXE,
    HOE,
    SHOVEL,
	KNIFE,
    TRIDENT;

    public static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    public static final UUID BASE_ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    public static final UUID BASE_ATTACK_REACH_UUID = UUID.fromString("26cb07a3-209d-4110-8e10-1010243614c8");

    WeaponType() {
    }

    public void addCombatAttributes(Tier var1, ImmutableMultimap.Builder<Attribute, AttributeModifier> var2) {
        float var3 = this.getSpeed(var1);
        float var4 = this.getDamage(var1);
        float var5 = this.getReach();
        var2.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", var4, AttributeModifier.Operation.ADDITION));
		var2.put(NewAttributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", var3, AttributeModifier.Operation.ADDITION));
        if (var5 != 0.0F && CombatEnhanced.CONFIG.attackReach()) {
            var2.put(NewAttributes.ATTACK_REACH, new AttributeModifier(BASE_ATTACK_REACH_UUID, "Weapon modifier", var5, AttributeModifier.Operation.ADDITION));
        }


    }

	public float getDamage(Tier var1) {
		int modifier = CombatEnhanced.CONFIG.fistDamage() ? 1 : 0;
		float var2 = var1.getAttackDamageBonus() + modifier;
		boolean isNotTier1 = var1 != Tiers.WOOD && var1 != Tiers.GOLD && var2 != 0;
		boolean isCTSNotT1 = isNotTier1 && CombatEnhanced.CONFIG.ctsAttackBalancing();
		switch (this) {
			case KNIFE -> {
				if (isCTSNotT1) {
					return var2 + min(CombatEnhanced.CONFIG.knifeAttackDamage(), 0);
				} else {
					return var2 + min(CombatEnhanced.CONFIG.knifeAttackDamage(), 0) + 1.0F;
				}
			}
			case PICKAXE -> {
				if (isCTSNotT1) {
					return var2;
				} else {
					return var2 + 1.0F;
				}
			}
			case SWORD -> {
				if (isCTSNotT1) {
					return var2 + min(CombatEnhanced.CONFIG.swordAttackDamage(), 0);
				} else {
					return var2 + min(CombatEnhanced.CONFIG.swordAttackDamage(), 0) + 1.0F;
				}
			}
			case AXE -> {
				if(!CombatEnhanced.CONFIG.ctsAttackBalancing()) {
					return !isNotTier1 ? var1 == Tiers.NETHERITE ? 10 : 9 : 7;
				} else if (isCTSNotT1) {
					return var2 + min(CombatEnhanced.CONFIG.axeAttackDamage(), 0);
				} else {
					return var2 + min(CombatEnhanced.CONFIG.axeAttackDamage(), 0) + 1.0F;
				}
			}
			case LONGSWORD -> {
				if (var1 != Tiers.IRON && var1 != Tiers.DIAMOND) {
					if (var1 == Tiers.NETHERITE || var1.getLevel() >= 4) {
						return var1 == Tiers.NETHERITE ? min(CombatEnhanced.CONFIG.netheriteLongswordAttackDamage(), 0) + modifier : min(CombatEnhanced.CONFIG.netheriteLongswordAttackDamage(), 0) + var2 - 4 + modifier;
					}

					return min(CombatEnhanced.CONFIG.baseLongswordAttackDamage(), 0) + modifier;
				}
				return min(CombatEnhanced.CONFIG.ironDiaLongswordAttackDamage(), 0) + modifier;
			}
			case HOE -> {
				if (var1 != Tiers.IRON && var1 != Tiers.DIAMOND) {
					if (var1 == Tiers.NETHERITE || var1.getLevel() >= 4) {
						return var1 == Tiers.NETHERITE ? min(CombatEnhanced.CONFIG.netheriteHoeAttackDamage(), 0) + modifier : min(CombatEnhanced.CONFIG.netheriteHoeAttackDamage(), 0) + var2 - 4 + modifier;
					}

					return min(CombatEnhanced.CONFIG.baseHoeAttackDamage(), 0) + modifier;
				}
				return min(CombatEnhanced.CONFIG.ironDiaHoeAttackDamage(), 0) + modifier;
			}
			case SHOVEL -> {
				return var2;
			}
			case TRIDENT -> {
				return min(CombatEnhanced.CONFIG.tridentAttackDamage(), 0) + modifier + (CombatEnhanced.CONFIG.ctsAttackBalancing() ? 0 : 1);
			}
			default -> {
				return 0.0F + modifier;
			}
		}
    }

    public float getSpeed(Tier var1) {
		switch (this) {
			case KNIFE -> {
				return CombatEnhanced.CONFIG.knifeAttackSpeed();
			}
			case LONGSWORD -> {
				return CombatEnhanced.CONFIG.longswordAttackSpeed();
			}
			case SWORD -> {
				return CombatEnhanced.CONFIG.swordAttackSpeed();
			}
			case AXE, SHOVEL -> {
				return CombatEnhanced.CONFIG.axeAttackSpeed();
			}
			case TRIDENT -> {
				return CombatEnhanced.CONFIG.tridentAttackSpeed();
			}
			case HOE -> {
				if (var1 == Tiers.WOOD) {
					return CombatEnhanced.CONFIG.woodenHoeAttackSpeed();
				} else if (var1 == Tiers.IRON) {
					return CombatEnhanced.CONFIG.ironHoeAttackSpeed();
				} else if (var1 == Tiers.DIAMOND) {
					return CombatEnhanced.CONFIG.goldDiaNethHoeAttackSpeed();
				} else if (var1 == Tiers.GOLD) {
					return CombatEnhanced.CONFIG.goldDiaNethHoeAttackSpeed();
				} else {
					if (var1 == Tiers.NETHERITE || var1.getLevel() >= 4) {
						return CombatEnhanced.CONFIG.goldDiaNethHoeAttackSpeed();
					}

					return CombatEnhanced.CONFIG.stoneHoeAttackSpeed();
				}
			}
			default -> {
				return CombatEnhanced.CONFIG.defaultAttackSpeed();
			}
		}
    }

    public float getReach() {
		return switch (this) {
			case KNIFE -> CombatEnhanced.CONFIG.knifeAttackReach();
			case SWORD -> CombatEnhanced.CONFIG.swordAttackReach();
			case LONGSWORD -> CombatEnhanced.CONFIG.longswordAttackReach();
			case HOE -> CombatEnhanced.CONFIG.hoeAttackReach();
			case TRIDENT -> CombatEnhanced.CONFIG.tridentAttackReach();
			case AXE -> CombatEnhanced.CONFIG.axeAttackReach();
			default -> CombatEnhanced.CONFIG.defaultAttackReach();
		};
    }
	public static float min(float f, float j) {
		return Math.max(f, j);
	}
}
