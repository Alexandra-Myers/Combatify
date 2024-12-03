package net.atlas.combatify.extensions;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.BlockingType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public interface WeaponWithType extends ItemExtensions {
	@Override
	default ItemAttributeModifiers modifyAttributeModifiers(ItemAttributeModifiers original) {
		if (Combatify.originalModifiers.get(self()).equals(ItemAttributeModifiers.EMPTY) && !original.equals(ItemAttributeModifiers.EMPTY))
			Combatify.originalModifiers.put(self(), original);
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
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(self())) {
			BlockingType blockingType = Combatify.ITEMS.configuredItems.get(self()).blockingType;
			if (blockingType != null)
				return blockingType;
		}
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredWeapons.containsKey(getWeaponType())) {
			BlockingType blockingType = Combatify.ITEMS.configuredWeapons.get(getWeaponType()).blockingType;
			if (blockingType != null)
				return blockingType;
		}
		return Combatify.EMPTY;
	}

	@Override
	default double getPiercingLevel() {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(self())) {
			Double piercingLevel = Combatify.ITEMS.configuredItems.get(self()).piercingLevel;
			if (piercingLevel != null)
				return piercingLevel;
		}
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredWeapons.containsKey(getWeaponType())) {
			Double piercingLevel = Combatify.ITEMS.configuredWeapons.get(getWeaponType()).piercingLevel;
			if (piercingLevel != null)
				return piercingLevel;
		}
		return 0;
	}
}
