package net.atlas.combatify.util;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.minecraft.world.item.ItemStack;

public class NonBannerShieldBlockingType extends ShieldBlockingType {
	public NonBannerShieldBlockingType(String name) {
		super(name);
	}
	@Override
	public float getShieldBlockDamageValue(ItemStack stack) {
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(stack.getItem())) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(stack.getItem());
			if (configurableItemData.blockStrength != null) {
				return configurableItemData.blockStrength.floatValue();
			}
		}

        return 5.0F;
	}

	@Override
	public double getShieldKnockbackResistanceValue(ItemStack stack) {
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(stack.getItem())) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(stack.getItem());
			if (configurableItemData.blockKbRes != null) {
				return configurableItemData.blockKbRes;
			}
		}

		return 0.5;
	}
}
