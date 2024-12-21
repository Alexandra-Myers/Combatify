package net.atlas.combatify.util;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public class NonBannerShieldBlockingType extends ShieldBlockingType {

	public NonBannerShieldBlockingType(String name, boolean crouchable, boolean blockHit, boolean canDisable, boolean needsFullCharge, boolean defaultKbMechanics, boolean hasDelay) {
		super(name, crouchable, blockHit, canDisable, needsFullCharge, defaultKbMechanics, hasDelay);
	}

	@Override
	public Factory<? extends BlockingType> factory() {
		return Combatify.NON_BANNER_SHIELD_BLOCKING_TYPE_FACTORY;
	}

	@Override
	public float getShieldBlockDamageValue(ItemStack stack, RandomSource random) {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(stack.getItem());
		if (configurableItemData != null) {
			if (configurableItemData.blocker().blockStrength() != null) {
				return CustomEnchantmentHelper.modifyShieldEffectiveness(stack, random, configurableItemData.blocker().blockStrength().floatValue());
			}
		}

        return CustomEnchantmentHelper.modifyShieldEffectiveness(stack, random, 5.0F);
	}

	@Override
	public double getShieldKnockbackResistanceValue(ItemStack stack) {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(stack.getItem());
		if (configurableItemData != null) {
			if (configurableItemData.blocker().blockKbRes() != null) {
				return configurableItemData.blocker().blockKbRes();
			}
		}

		return 0.5;
	}
}
