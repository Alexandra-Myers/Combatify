package net.atlas.combatify.enchantment;

import net.atlas.combatify.extensions.CustomEnchantment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.TridentImpalerEnchantment;

public class CleavingEnchantment extends Enchantment implements CustomEnchantment {
	public int level;

	public CleavingEnchantment() {
		super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
	}

	@Override
	public int getMinCost(int level) {
		return 5 + (level - 1) * 20;
	}

	@Override
	public int getMaxCost(int level) {
		return this.getMinCost(level) + 20;
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	@Override
	public float getDamageBonus(int level, MobType group) {
		this.level = level;
		return (float)1 + level;
	}

	@Override
	public boolean canEnchant(ItemStack stack) {
		return stack.getItem() instanceof AxeItem;
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
		return super.checkCompatibility(enchantment) && !(enchantment instanceof PiercingEnchantment || enchantment instanceof DamageEnchantment || enchantment instanceof TridentImpalerEnchantment);
	}
}
