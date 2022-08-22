package net.alexandra.atlas.atlas_combat.extensions;

import net.minecraft.world.item.ItemStack;

public interface LivingEntityExtensions {

    void invertedKnockback(float var1, double var2, double var4);

    ItemStack getBlockingItem();

	boolean isItemOnCooldown(ItemStack var1);

	boolean hasEnabledShieldOnCrouch();

	void newKnockback(float var1, double var2, double var4);

}
