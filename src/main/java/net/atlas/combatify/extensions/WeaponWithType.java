package net.atlas.combatify.extensions;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.config.ConfigurableWeaponData;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.BlockingType;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public interface WeaponWithType extends ItemExtensions {
	@Override
	default ItemAttributeModifiers modifyAttributeModifiers(ItemAttributeModifiers original) {
		if (Combatify.originalModifiers.get(combatify$self()).equals(ItemAttributeModifiers.EMPTY) && !original.equals(ItemAttributeModifiers.EMPTY))
			Combatify.originalModifiers.put(combatify$self(), original);
		if (combatify$getWeaponType().isEmpty() || !Combatify.CONFIG.weaponTypesEnabled())
			return original;
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		combatify$getWeaponType().addCombatAttributes(getConfigTier(), builder);
		original.modifiers().forEach(entry -> {
			boolean bl = entry.attribute().is(Attributes.ATTACK_DAMAGE)
				|| entry.attribute().is(Attributes.ATTACK_SPEED)
				|| entry.attribute().is(Attributes.ENTITY_INTERACTION_RANGE);
			if (!bl)
				builder.add(entry.attribute(), entry.modifier(), entry.slot());
		});
		return builder.build();
	}
	default WeaponType combatify$getWeaponType() {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(combatify$self());
		if (configurableItemData != null) {
			WeaponType type = configurableItemData.weaponStats().weaponType();
			if (type != null)
				return type;
		}
		return WeaponType.EMPTY;
	}

	@Override
	default double getChargedAttackBonus() {
		double chargedBonus = combatify$getWeaponType().getChargedReach();
		ConfigurableItemData configurableItemData = MethodHandler.forItem(combatify$self());
		if (configurableItemData != null) {
			if (configurableItemData.weaponStats().chargedReach() != null)
				chargedBonus = configurableItemData.weaponStats().chargedReach();
		}
		return chargedBonus;
	}

	@Override
	default boolean canSweep() {
		boolean canSweep = combatify$getWeaponType().canSweep();
		ConfigurableItemData configurableItemData = MethodHandler.forItem(combatify$self());
		if (configurableItemData != null) {
			if (configurableItemData.weaponStats().canSweep() != null)
				canSweep = configurableItemData.weaponStats().canSweep();
		}
		return canSweep;
	}

	@Override
	default BlockingType combatify$getBlockingType() {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(combatify$self());
		if (configurableItemData != null) {
			BlockingType blockingType = configurableItemData.blocker().blockingType();
			if (blockingType != null)
				return blockingType;
		}
		ConfigurableWeaponData configurableWeaponData = MethodHandler.forWeapon(combatify$getWeaponType());
		if (configurableWeaponData != null) {
			BlockingType blockingType = configurableWeaponData.blockingType();
			if (blockingType != null)
				return blockingType;
		}
		return Combatify.EMPTY;
	}

	@Override
	default double getPiercingLevel() {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(combatify$self());
		if (configurableItemData != null) {
			Double piercingLevel = configurableItemData.weaponStats().piercingLevel();
			if (piercingLevel != null)
				return piercingLevel;
		}
		ConfigurableWeaponData configurableWeaponData = MethodHandler.forWeapon(combatify$getWeaponType());
		if (configurableWeaponData != null) {
			Double piercingLevel = configurableWeaponData.piercingLevel();
			if (piercingLevel != null)
				return piercingLevel;
		}
		return 0;
	}
}
