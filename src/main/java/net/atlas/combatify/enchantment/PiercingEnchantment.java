package net.atlas.combatify.enchantment;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;

import static net.atlas.combatify.Combatify.id;

public class PiercingEnchantment extends Enchantment {
	public static final Enchantment PIERCER = EnchantmentRegistry.registerEnchant(id("piercer"), new PiercingEnchantment());

	public PiercingEnchantment() {
		super(Enchantment.definition(ItemTags.DURABILITY_ENCHANTABLE, 2, 3, Enchantment.dynamicCost(25, 25), Enchantment.dynamicCost(75, 25), 3, EquipmentSlot.MAINHAND));
	}

	@Override
	public boolean canEnchant(ItemStack stack) {
		return stack.getItem() instanceof TieredItem || stack.getItem() instanceof TridentItem;
	}

	@Override
	protected boolean checkCompatibility(Enchantment enchantment) {
		return super.checkCompatibility(enchantment) && !(enchantment instanceof CleavingEnchantment || enchantment instanceof DamageEnchantment);
	}

	public static void registerEnchants() {

	}
}
