package net.atlas.combatify.util;

import net.atlas.combatify.config.ConfigurableItemData;
import net.minecraft.world.item.ItemStack;

public class NonBannerShieldBlockingType extends ShieldBlockingType {
	public NonBannerShieldBlockingType(String name) {
		super(name);
	}
	@Override
	public float getShieldBlockDamageValue(ItemStack stack) {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(stack.getItem());
		if (configurableItemData != null) {
			if (configurableItemData.blockStrength != null) {
				return configurableItemData.blockStrength.floatValue();
			}
		}

        return 5.0F;
	}

	@Override
	public double getShieldKnockbackResistanceValue(ItemStack stack) {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(stack.getItem());
		if (configurableItemData != null) {
			if (configurableItemData.blockKbRes != null) {
				return configurableItemData.blockKbRes;
			}
		}

		return 0.5;
	}
}
