package net.alexandra.atlas.atlas_combat.enchantment;

import net.alexandra.atlas.atlas_combat.item.KnifeItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.jetbrains.annotations.NotNull;

public class StabbingEnchantment extends Enchantment {
	public int level;

	public StabbingEnchantment() {
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
		return 2;
	}

	@Override
	public float getDamageBonus(int level, @NotNull MobType group) {
		return level * 0.5F;
	}

	@Override
	public boolean canEnchant(ItemStack stack) {
		return stack.getItem() instanceof KnifeItem;
	}
}
