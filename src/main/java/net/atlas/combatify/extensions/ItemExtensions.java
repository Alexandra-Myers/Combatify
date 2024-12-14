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

import java.util.Optional;

public interface ItemExtensions {

	default ItemAttributeModifiers modifyAttributeModifiers(ItemAttributeModifiers original) {
		return original;
	}

	default double getChargedAttackBonus() {
        double chargedBonus = 1.0;
		ConfigurableItemData configurableItemData = MethodHandler.forItem(combatify$self());
		if (configurableItemData != null) {
			WeaponType type;
			if ((type = configurableItemData.weaponStats().weaponType()) != null)
				chargedBonus = type.getChargedReach();
			if (configurableItemData.weaponStats().chargedReach() != null)
				chargedBonus = configurableItemData.weaponStats().chargedReach();
		}
		return chargedBonus;
	}

	default boolean canSweep() {
        boolean canSweep = false;
		ConfigurableItemData configurableItemData = MethodHandler.forItem(combatify$self());
		if (configurableItemData != null) {
			WeaponType type;
			if ((type = configurableItemData.weaponStats().weaponType()) != null)
				canSweep = type.canSweep();
			if (configurableItemData.weaponStats().canSweep() != null)
				canSweep = configurableItemData.weaponStats().canSweep();
		}
		return canSweep;
	}

	default BlockingType combatify$getBlockingType() {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(combatify$self());
		if (configurableItemData != null) {
			if (configurableItemData.blocker().blockingType() != null)
				return configurableItemData.blocker().blockingType();
			WeaponType type;
			ConfigurableWeaponData configurableWeaponData;
			if ((type = configurableItemData.weaponStats().weaponType()) != null && (configurableWeaponData = MethodHandler.forWeapon(type)) != null) {
				BlockingType blockingType = configurableWeaponData.blockingType();
				if (blockingType != null)
					return blockingType;
			}
		}
		return Combatify.EMPTY;
	}

	default double getPiercingLevel() {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(combatify$self());
		if (configurableItemData != null) {
			if (configurableItemData.weaponStats().piercingLevel() != null)
				return configurableItemData.weaponStats().piercingLevel();
			WeaponType type;
			ConfigurableWeaponData configurableWeaponData;
			if ((type = configurableItemData.weaponStats().weaponType()) != null && (configurableWeaponData = MethodHandler.forWeapon(type)) != null) {
				Double piercingLevel = configurableWeaponData.piercingLevel();
				if (piercingLevel != null)
					return piercingLevel;
			}
		}
		return 0;
	}

	Item combatify$self();

	default Tier getConfigTier() {
		Optional<Tier> tier = getTierFromConfig();
        return tier.orElse(Tiers.DIAMOND);
    }
	default Optional<Tier> getTierFromConfig() {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(combatify$self());
		if (configurableItemData != null)
            return configurableItemData.optionalTier();
		return Optional.empty();
	}
	default boolean canRepairThroughConfig(ItemStack stack) {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(combatify$self());
		if (configurableItemData != null) {
			if (configurableItemData.repairIngredient() != null)
				return configurableItemData.repairIngredient().test(stack);
		}
		return false;
	}
}
