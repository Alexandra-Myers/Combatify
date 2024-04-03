package net.atlas.combatify.extensions;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.BlockingType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public interface ItemExtensions {

	default ItemAttributeModifiers modifyAttributeModifiers(ItemAttributeModifiers original) {
		return original;
	}

	default double getChargedAttackBonus() {
		Item item = self();
		double chargedBonus = 1.0;
		if(Combatify.ITEMS.configuredItems.containsKey(item)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(item);
			WeaponType type;
			if ((type = configurableItemData.type) != null)
				chargedBonus = type.getChargedReach();
			if (configurableItemData.chargedReach != null)
				chargedBonus = configurableItemData.chargedReach;
		}
		return chargedBonus;
	}

	default boolean canSweep() {
		Item item = self();
		boolean canSweep = false;
		if(Combatify.ITEMS.configuredItems.containsKey(item)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(item);
			WeaponType type;
			if ((type = configurableItemData.type) != null)
				canSweep = type.canSweep();
			if (configurableItemData.canSweep != null)
				canSweep = configurableItemData.canSweep;
		}
		return canSweep;
	}

	default BlockingType getBlockingType() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(self())) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(self());
			if (configurableItemData.blockingType != null)
				return configurableItemData.blockingType;
			WeaponType type;
			if ((type = configurableItemData.type) != null && Combatify.ITEMS.configuredWeapons.containsKey(type)) {
				BlockingType blockingType = Combatify.ITEMS.configuredWeapons.get(type).blockingType;
				if (blockingType != null)
					return blockingType;
			}
		}
		return Combatify.EMPTY;
	}

	default double getPiercingLevel() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(self())) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(self());
			if (configurableItemData.piercingLevel != null)
				return configurableItemData.piercingLevel;
			WeaponType type;
			if ((type = configurableItemData.type) != null && Combatify.ITEMS.configuredWeapons.containsKey(type)) {
				Double piercingLevel = Combatify.ITEMS.configuredWeapons.get(type).piercingLevel;
				if (piercingLevel != null)
					return piercingLevel;
			}
		}
		return 0;
	}

	Item self();

	default Tier getConfigTier() {
		Tier tier = getTierFromConfig();
		if (tier != null) return tier;
		return Tiers.DIAMOND;
	}
	default Tier getTierFromConfig() {
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(self()))
            return Combatify.ITEMS.configuredItems.get(self()).tier;
		return null;
	}
}
