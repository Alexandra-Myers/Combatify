package net.atlas.combatify.extensions;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.config.ConfigurableWeaponData;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.BlockingType;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public interface ItemExtensions {

	default ItemAttributeModifiers modifyAttributeModifiers(ItemAttributeModifiers original) {
		return original;
	}

	default double getChargedAttackBonus() {
        double chargedBonus = 1.0;
		ConfigurableItemData configurableItemData = MethodHandler.forItem(combatify$self());
		if (configurableItemData != null) {
			WeaponType type;
			if ((type = configurableItemData.type) != null)
				chargedBonus = type.getChargedReach();
			if (configurableItemData.chargedReach != null)
				chargedBonus = configurableItemData.chargedReach;
		}
		return chargedBonus;
	}

	default boolean canSweep() {
        boolean canSweep = false;
		ConfigurableItemData configurableItemData = MethodHandler.forItem(combatify$self());
		if (configurableItemData != null) {
			WeaponType type;
			if ((type = configurableItemData.type) != null)
				canSweep = type.canSweep();
			if (configurableItemData.canSweep != null)
				canSweep = configurableItemData.canSweep;
		}
		return canSweep;
	}

	default BlockingType combatify$getBlockingType() {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(combatify$self());
		if (configurableItemData != null) {
			if (configurableItemData.blockingType != null)
				return configurableItemData.blockingType;
			WeaponType type;
			ConfigurableWeaponData configurableWeaponData;
			if ((type = configurableItemData.type) != null && (configurableWeaponData = MethodHandler.forWeapon(type)) != null) {
				BlockingType blockingType = configurableWeaponData.blockingType;
				if (blockingType != null)
					return blockingType;
			}
		}
		return Combatify.EMPTY;
	}

	default double getPiercingLevel() {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(combatify$self());
		if (configurableItemData != null) {
			if (configurableItemData.piercingLevel != null)
				return configurableItemData.piercingLevel;
			WeaponType type;
			ConfigurableWeaponData configurableWeaponData;
			if ((type = configurableItemData.type) != null && (configurableWeaponData = MethodHandler.forWeapon(type)) != null) {
				Double piercingLevel = configurableWeaponData.piercingLevel;
				if (piercingLevel != null)
					return piercingLevel;
			}
		}
		return 0;
	}

	Item combatify$self();

	default Tier getConfigTier() {
		Tier tier = getTierFromConfig();
		if (tier != null) return tier;
		return Tiers.DIAMOND;
	}
	default Tier getTierFromConfig() {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(combatify$self());
		if (configurableItemData != null)
            return configurableItemData.tier;
		return null;
	}
	default boolean canRepairThroughConfig(ItemStack stack) {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(combatify$self());
		if (configurableItemData != null) {
			if (configurableItemData.repairIngredient != null)
				return configurableItemData.repairIngredient.test(stack);
		}
		return false;
	}
}
