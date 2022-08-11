package net.alexandra.atlas.atlas_combat.extensions;

import net.minecraft.world.item.ItemStack;

public interface IShieldItem {
	float getShieldKnockbackResistanceValue(ItemStack itemStack);
	public float getShieldBlockDamageValue(ItemStack itemStack);
}
