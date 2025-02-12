package net.atlas.combatify.extensions;

import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.world.item.Item;

public interface ItemExtensions {
	default double getChargedAttackBonus() {
        double chargedBonus = 1.0;
		ConfigurableItemData configurableItemData = MethodHandler.forItem(combatify$self());
		if (configurableItemData != null) {
			if (configurableItemData.weaponStats().chargedReach() != null)
				chargedBonus = configurableItemData.weaponStats().chargedReach();
		}
		return chargedBonus;
	}

	default Item combatify$self() {
		throw new IllegalStateException("Extension has not been applied");
	}
}
