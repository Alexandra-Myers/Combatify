package net.alexandra.atlas.atlas_combat.item;

import com.google.common.collect.ImmutableMultimap;
import net.alexandra.atlas.atlas_combat.extensions.ItemExtensions;
import net.alexandra.atlas.atlas_combat.extensions.PlayerExtensions;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public enum WeaponType {
    SWORD,
    AXE,
    PICKAXE,
    HOE,
    SHOVEL,
    TRIDENT;

    public static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    public static final UUID BASE_ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    public static final UUID BASE_ATTACK_REACH_UUID = UUID.fromString("26cb07a3-209d-4110-8e10-1010243614c8");
	public static final UUID BASE_BLOCK_REACH_UUID = UUID.fromString("7f6fa63f-0fbd-4fa8-9acc-69c45c8f68ed");

    WeaponType() {
    }

    public void addCombatAttributes(Tier var1, ImmutableMultimap.Builder<Attribute, AttributeModifier> var2) {
        float var3 = this.getSpeed(var1);
        float var4 = this.getDamage(var1);
        float var5 = this.getReach();
		float var6 = this.getBlockReach();
        var2.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", var4, AttributeModifier.Operation.ADDITION));
        var2.put(NewAttributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", var3, AttributeModifier.Operation.ADDITION));
        if (var5 != 0.0F) {
            var2.put(NewAttributes.ATTACK_REACH, new AttributeModifier(BASE_ATTACK_REACH_UUID, "Weapon modifier", var5, AttributeModifier.Operation.ADDITION));
        }
		if (var6 != 0.0F) {
			var2.put(NewAttributes.BLOCK_REACH, new AttributeModifier(BASE_BLOCK_REACH_UUID, "Weapon modifier", var5, AttributeModifier.Operation.ADDITION));
		}


    }
	public static boolean isWithinAttackRange(final Player player, final Entity entity) {
		return player.distanceToSqr(entity) <= Mth.square(((ItemExtensions)player.getItemInHand(InteractionHand.MAIN_HAND).getItem()).getAttackReach(player));
	}

    public float getDamage(Tier var1) {
		float var2 = var1.getAttackDamageBonus();
        switch (this) {
            case SWORD:
				if(var1 != Tiers.WOOD && var1 != Tiers.GOLD) {
                	return var2 - 1 + 2.0F;
				}else {
					return var2 + 2.0F;
				}
            case AXE:
				if(var1 != Tiers.WOOD && var1 != Tiers.GOLD) {
					return var2 - 1 + 3.0F;
				}else {
					return var2 + 3.0F;
				}
            case PICKAXE:
				if(var1 != Tiers.WOOD && var1 != Tiers.GOLD) {
					return var2 - 1 + 1.0F;
				}else {
					return var2 + 1.0F;
				}
            case HOE:
                if (var1 != Tiers.IRON && var1 != Tiers.DIAMOND) {
                    if (var1 == Tiers.NETHERITE) {
                        return 2.0F;
                    }

                    return 0.0F;
                }

                return 1.0F;
            case SHOVEL:
                return var2;
            case TRIDENT:
                return 5.0F;
            default:
                return 0.0F;
        }
    }

    public float getSpeed(Tier var1) {
        switch (this) {
            case SWORD:
                return 0.5F;
            case AXE:
			case TRIDENT:
				return -0.5F;
			case HOE:
                if (var1 == Tiers.WOOD) {
                    return -0.5F;
                } else if (var1 == Tiers.IRON) {
                    return 0.5F;
                } else if (var1 == Tiers.DIAMOND) {
                    return 1.0F;
                } else if (var1 == Tiers.GOLD) {
                    return 1.0F;
                } else {
                    if (var1 == Tiers.NETHERITE) {
                        return 2.0F;
                    }

                    return 0.0F;
                }
            default:
                return 0.0F;
        }
    }

    public float getReach() {
        switch (this) {
            case SWORD:
                return 0.5F;
			case HOE:
			case TRIDENT:
				return 1.0F;
			default:
                return 0.0F;
        }
    }

	public float getBlockReach() {
		switch (this) {
			case SWORD:
			case AXE:
				return 1.5F;
			case PICKAXE:
			case SHOVEL:
				return 1.0F;
			case HOE:
			case TRIDENT:
				return 2.0F;
			default:
				return 0.0F;
		}
	}
}
