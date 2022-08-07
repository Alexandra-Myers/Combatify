package net.alexandra.atlas.atlas_combat.mixin;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

public class AxeCategory extends EnchantmentTargetMixin {
	@Override
	public boolean canEnchant(Item item) {
		return item instanceof AxeItem;
	}
}

@Mixin(EnchantmentCategory.class)
abstract class EnchantmentTargetMixin {
	@Shadow
	abstract boolean canEnchant(Item item);
}
