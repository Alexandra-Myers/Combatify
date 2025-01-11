package net.atlas.combatify.extensions;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public interface WeaponWithType extends ItemExtensions {
	@Override
	@SuppressWarnings("deprecation")
	default ItemAttributeModifiers modifyAttributeModifiers(ItemAttributeModifiers original) {
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
}
