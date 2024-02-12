package net.atlas.combatify.enchantment;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.TridentImpalerEnchantment;

import static net.atlas.combatify.Combatify.id;

public class PiercingEnchantment extends Enchantment {
	public static final Enchantment PIERCER = EnchantmentRegistry.registerEnchant(id("piercer"), new PiercingEnchantment());

	public PiercingEnchantment() {
		super(Rarity.RARE, ItemTags.WEAPON_ENCHANTABLE, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
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
	public int getMaxLevel() {
		return 3;
	}

	@Override
	public boolean canEnchant(ItemStack stack) {
		return stack.getItem() instanceof TieredItem || stack.getItem() instanceof TridentItem;
	}

	@Override
	protected boolean checkCompatibility(Enchantment enchantment) {
		return super.checkCompatibility(enchantment) && !(enchantment instanceof CleavingEnchantment || enchantment instanceof DamageEnchantment || enchantment instanceof TridentImpalerEnchantment);
	}

	public static void registerEnchants() {

	}
}
