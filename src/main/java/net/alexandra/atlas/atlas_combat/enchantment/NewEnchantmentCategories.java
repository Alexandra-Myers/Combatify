package net.alexandra.atlas.atlas_combat.enchantment;

import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.block.Block;

public enum NewEnchantmentCategories {
	WEAPON {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof SwordItem || item instanceof AxeItem || item instanceof HoeItem;
		}
	},
	AXE {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof AxeItem;
		}
	};

	public abstract boolean canEnchant(Item item);
}
