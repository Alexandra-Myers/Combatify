package net.atlas.combatify.item;

import com.google.common.collect.ImmutableMultimap;
import net.atlas.combatify.Combatify;
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
        if (var5 != 0.0F && Combatify.CONFIG.attackReach()) {
            var2.put(NewAttributes.ATTACK_REACH, new AttributeModifier(BASE_ATTACK_REACH_UUID, "Weapon modifier", var5, AttributeModifier.Operation.ADDITION));
        }


    }

	public float getDamage(Tier var1) {
		int modifier = Combatify.CONFIG.fistDamage() ? 1 : 0;
		float var2 = var1.getAttackDamageBonus() + modifier;
		boolean isNotTier1 = var1 != Tiers.WOOD && var1 != Tiers.GOLD && var2 != 0;
		boolean isCTSNotT1 = isNotTier1 && Combatify.CONFIG.ctsAttackBalancing();
		switch (this) {
			case KNIFE -> {
				if (isCTSNotT1) {
					return var2 + min(Combatify.CONFIG.knifeAttackDamage(), 0);
				} else {
					return var2 + min(Combatify.CONFIG.knifeAttackDamage(), 0) + 1.0F;
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
					return var2 + min(Combatify.CONFIG.swordAttackDamage(), 0);
				} else {
					return var2 + min(Combatify.CONFIG.swordAttackDamage(), 0) + 1.0F;
				}
			}
			case AXE -> {
				if(!Combatify.CONFIG.ctsAttackBalancing()) {
					return !isNotTier1 ? var1 == Tiers.NETHERITE ? 10 : 9 : 7;
				} else if (isCTSNotT1) {
					return var2 + min(Combatify.CONFIG.axeAttackDamage(), 0);
				} else {
					return var2 + min(Combatify.CONFIG.axeAttackDamage(), 0) + 1.0F;
				}
			}
			case LONGSWORD -> {
				if (var1 != Tiers.IRON && var1 != Tiers.DIAMOND) {
					if (var1 == Tiers.NETHERITE || var1.getLevel() >= 4) {
						return var1 == Tiers.NETHERITE ? min(Combatify.CONFIG.netheriteLongswordAttackDamage(), 0) + modifier : min(Combatify.CONFIG.netheriteLongswordAttackDamage(), 0) + var2 - 4 + modifier;
					}

					return min(Combatify.CONFIG.baseLongswordAttackDamage(), 0) + modifier;
				}
				return min(Combatify.CONFIG.ironDiaLongswordAttackDamage(), 0) + modifier;
			}
			case HOE -> {
				if (var1 != Tiers.IRON && var1 != Tiers.DIAMOND) {
					if (var1 == Tiers.NETHERITE || var1.getLevel() >= 4) {
						return var1 == Tiers.NETHERITE ? min(Combatify.CONFIG.netheriteHoeAttackDamage(), 0) + modifier : min(Combatify.CONFIG.netheriteHoeAttackDamage(), 0) + var2 - 4 + modifier;
					}

					return min(Combatify.CONFIG.baseHoeAttackDamage(), 0) + modifier;
				}
				return min(Combatify.CONFIG.ironDiaHoeAttackDamage(), 0) + modifier;
			}
			case SHOVEL -> {
				return var2;
			}
			case TRIDENT -> {
				return min(Combatify.CONFIG.tridentAttackDamage(), 0) + modifier + (Combatify.CONFIG.ctsAttackBalancing() ? 0 : 1);
			}
			default -> {
				return 0.0F + modifier;
			}
		}
    }

    public float getSpeed(Tier var1) {
		switch (this) {
			case KNIFE -> {
				return Combatify.CONFIG.knifeAttackSpeed();
			}
			case LONGSWORD -> {
				return Combatify.CONFIG.longswordAttackSpeed();
			}
			case SWORD -> {
				return Combatify.CONFIG.swordAttackSpeed();
			}
			case AXE, SHOVEL -> {
				return Combatify.CONFIG.axeAttackSpeed();
			}
			case TRIDENT -> {
				return Combatify.CONFIG.tridentAttackSpeed();
			}
			case HOE -> {
				if (var1 == Tiers.WOOD) {
					return Combatify.CONFIG.woodenHoeAttackSpeed();
				} else if (var1 == Tiers.IRON) {
					return Combatify.CONFIG.ironHoeAttackSpeed();
				} else if (var1 == Tiers.DIAMOND) {
					return Combatify.CONFIG.goldDiaNethHoeAttackSpeed();
				} else if (var1 == Tiers.GOLD) {
					return Combatify.CONFIG.goldDiaNethHoeAttackSpeed();
				} else {
					if (var1 == Tiers.NETHERITE || var1.getLevel() >= 4) {
						return Combatify.CONFIG.goldDiaNethHoeAttackSpeed();
					}

					return Combatify.CONFIG.stoneHoeAttackSpeed();
				}
			}
			default -> {
				return Combatify.CONFIG.defaultAttackSpeed();
			}
		}
    }

    public float getReach() {
		return switch (this) {
			case KNIFE -> Combatify.CONFIG.knifeAttackReach();
			case SWORD -> Combatify.CONFIG.swordAttackReach();
			case LONGSWORD -> Combatify.CONFIG.longswordAttackReach();
			case HOE -> Combatify.CONFIG.hoeAttackReach();
			case TRIDENT -> Combatify.CONFIG.tridentAttackReach();
			case AXE -> Combatify.CONFIG.axeAttackReach();
			default -> Combatify.CONFIG.defaultAttackReach();
		};
    }
	public static float min(float f, float j) {
		return Math.max(f, j);
	}
}
