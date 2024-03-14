package net.atlas.combatify.extensions;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.config.ConfigurableWeaponData;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.BlockingType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public interface WeaponWithType extends ItemExtensions {
	@Override
	default ItemAttributeModifiers modifyAttributeModifiers(ItemAttributeModifiers original) {
		if (getWeaponType().isEmpty() || !Combatify.CONFIG.weaponTypesEnabled())
			return original;
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		getWeaponType().addCombatAttributes(getConfigTier(), builder);
		original.modifiers().forEach(entry -> {
			boolean bl = entry.attribute().is(Attributes.ATTACK_DAMAGE)
				|| entry.attribute().is(Attributes.ATTACK_SPEED)
				|| entry.attribute().is(Attributes.ENTITY_INTERACTION_RANGE);
			if (!bl)
				builder.add(entry.attribute(), entry.modifier(), entry.slot());
		});
		return builder.build();
	}
	default WeaponType getWeaponType() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(self())) {
			WeaponType type = Combatify.ITEMS.configuredItems.get(self()).type;
			if (type != null)
				return type;
		}
		return WeaponType.EMPTY;
	}

	@Override
	default double getChargedAttackBonus() {
		Item item = self();
		double chargedBonus = getWeaponType().getChargedReach();
		if(Combatify.ITEMS.configuredItems.containsKey(item)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(item);
			if (configurableItemData.chargedReach != null)
				chargedBonus = configurableItemData.chargedReach;
		}
		return chargedBonus;
	}

	@Override
	default boolean canSweep() {
		Item item = self();
		boolean canSweep = getWeaponType().canSweep();
		if(Combatify.ITEMS.configuredItems.containsKey(item)) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(item);
			if (configurableItemData.canSweep != null)
				canSweep = configurableItemData.canSweep;
		}
		return canSweep;
	}

	@Override
	default BlockingType getBlockingType() {
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredWeapons.containsKey(getWeaponType())) {
			ConfigurableWeaponData configurableWeaponData = Combatify.ITEMS.configuredWeapons.get(getWeaponType());
			if (configurableWeaponData.blockingType != null)
				return configurableWeaponData.blockingType;
		}
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(self())) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(self());
			if (configurableItemData.blockingType != null)
				return configurableItemData.blockingType;
		}
		return Combatify.EMPTY;
	}

	@Override
	default double getPiercingLevel() {
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredWeapons.containsKey(getWeaponType())) {
			ConfigurableWeaponData configurableWeaponData = Combatify.ITEMS.configuredWeapons.get(getWeaponType());
			if (configurableWeaponData.piercingLevel != null) {
				return configurableWeaponData.piercingLevel;
			}
		}
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(self())) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(self());
			if (configurableItemData.piercingLevel != null) {
				return configurableItemData.piercingLevel;
			}
		}
		return 0;
	}
}
