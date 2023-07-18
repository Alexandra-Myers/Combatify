package net.alexandra.atlas.atlas_combat.enchantment;

import net.alexandra.atlas.atlas_combat.extensions.CustomEnchantment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import static net.alexandra.atlas.atlas_combat.AtlasCombat.id;

public class PiercingEnchantment extends Enchantment implements CustomEnchantment {
	public static final Enchantment PIERCER = EnchantmentRegistry.registerEnchant(id("piercer"), new PiercingEnchantment());

	public PiercingEnchantment() {
		super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
	}

	@Override
	public int getMinCost(int level) {
		return level * 25;
	}

	@Override
	public int getMaxCost(int level) {
		return this.getMinCost(level) + 50;
	}

	@Override
	public boolean canEnchant(ItemStack stack) {
		return stack.getItem() instanceof TieredItem;
	}

	@Override
	public boolean isAcceptibleConditions(ItemStack stack) {
		return this.canEnchant(stack);
	}

	@Override
	public boolean isAcceptibleAnvil(ItemStack stack) {
		return this.canEnchant(stack);
	}

	@Override
	protected boolean checkCompatibility(Enchantment enchantment) {
		return !(enchantment instanceof CleavingEnchantment);
	}

	public static void registerEnchants() {

	}
}
