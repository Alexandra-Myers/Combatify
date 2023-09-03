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
		if (!Combatify.CONFIG.instaAttack())
			var2.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", var3, AttributeModifier.Operation.ADDITION));
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
			case KNIFE, PICKAXE -> {
				if (isCTSNotT1) {
					return var2;
				} else {
					return var2 + 1.0F;
				}
			}
			case SWORD -> {
				if (isCTSNotT1) {
					return var2 + 1.0F;
				} else {
					return var2 + 2.0F;
				}
			}
			case AXE -> {
				if(!Combatify.CONFIG.ctsAttackBalancing()) {
					return !isNotTier1 ? var1 == Tiers.NETHERITE ? 10 : 9 : 7;
				} else if (isCTSNotT1) {
					return var2 + 2;
				} else {
					return var2 + 3.0F;
				}
			}
			case LONGSWORD, HOE -> {
				if (var1 != Tiers.IRON && var1 != Tiers.DIAMOND) {
					if (var1 == Tiers.NETHERITE || var1.getLevel() >= 4) {
						return var1 == Tiers.NETHERITE ? 2 + modifier : 2 + var2 - 4 + modifier;
					}

					return modifier;
				}
				return 1 + modifier;
			}
			case SHOVEL -> {
				return var2;
			}
			case TRIDENT -> {
				return 5 + modifier + (Combatify.CONFIG.ctsAttackBalancing() ? 0 : 2);
			}
			default -> {
				return 0.0F + modifier;
			}
		}
    }

    public float getSpeed(Tier var1) {
		switch (this) {
			case KNIFE -> {
				return 1.0F;
			}
			case LONGSWORD, SWORD -> {
				return 0.5F;
			}
			case AXE, SHOVEL, TRIDENT -> {
				return -0.5F;
			}
			case HOE -> {
				if (var1 == Tiers.WOOD) {
					return -0.5F;
				} else if (var1 == Tiers.IRON) {
					return 0.5F;
				} else if (var1 == Tiers.DIAMOND || var1 == Tiers.GOLD) {
					return 1.0F;
				} else {
					if (var1 == Tiers.NETHERITE || var1.getLevel() >= 4) {
						return 1.0F;
					}

					return 0F;
				}
			}
			default -> {
				return 0.0F;
			}
		}
    }

    public float getReach() {
		return switch (this) {
			case KNIFE -> 0.25F;
			case SWORD -> 0.5F;
			case LONGSWORD, HOE, TRIDENT -> 1F;
			default -> 0F;
		};
    }
}
