package net.alexandra.atlas.atlas_combat.util;

import net.alexandra.atlas.atlas_combat.extensions.IShieldItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;

public class ShieldUtils {

	public static float getShieldBlockDamageValue(ItemStack blockingItem) {
		return blockingItem.getTagElement("BlockEntityTag") != null ? 10.0F : 5.0F;
	}

	public static float getShieldKnockbackResistanceValue(ItemStack blockingItem) {
		return blockingItem.getTagElement("BlockEntityTag") != null ? 0.8F : 0.5F;
	}

}
