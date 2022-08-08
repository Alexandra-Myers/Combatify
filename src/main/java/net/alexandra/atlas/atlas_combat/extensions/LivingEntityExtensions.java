package net.alexandra.atlas.atlas_combat.extensions;

import net.minecraft.world.item.ItemStack;

public interface LivingEntityExtensions {

	ItemStack getBlockingItem();

	boolean isItemOnCooldown(ItemStack var1);

	boolean hasEnabledShieldOnCrouch();

}
