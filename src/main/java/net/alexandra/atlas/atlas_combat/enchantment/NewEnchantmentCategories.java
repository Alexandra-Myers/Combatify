package net.alexandra.atlas.atlas_combat.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.block.Block;

public enum NewEnchantmentCategories {
	ARMOR {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof ArmorItem;
		}
	},
	ARMOR_FEET {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof ArmorItem && ((ArmorItem)item).getSlot() == EquipmentSlot.FEET;
		}
	},
	ARMOR_LEGS {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof ArmorItem && ((ArmorItem)item).getSlot() == EquipmentSlot.LEGS;
		}
	},
	ARMOR_CHEST {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof ArmorItem && ((ArmorItem)item).getSlot() == EquipmentSlot.CHEST;
		}
	},
	ARMOR_HEAD {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof ArmorItem && ((ArmorItem)item).getSlot() == EquipmentSlot.HEAD;
		}
	},
	WEAPON {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof SwordItem || item instanceof AxeItem || item instanceof HoeItem;
		}
	},
	DIGGER {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof DiggerItem;
		}
	},
	FISHING_ROD {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof FishingRodItem;
		}
	},
	TRIDENT {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof TridentItem;
		}
	},
	BREAKABLE {
		@Override
		public boolean canEnchant(Item item) {
			return item.canBeDepleted();
		}
	},
	BOW {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof BowItem;
		}
	},
	WEARABLE {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof Wearable || Block.byItem(item) instanceof Wearable;
		}
	},
	CROSSBOW {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof CrossbowItem;
		}
	},
	VANISHABLE {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof Vanishable || Block.byItem(item) instanceof Vanishable || BREAKABLE.canEnchant(item);
		}
	},
	SWORD {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof SwordItem;
		}
	},
	HOE {
		@Override
		public boolean canEnchant(Item item) {
			return item instanceof HoeItem;
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
