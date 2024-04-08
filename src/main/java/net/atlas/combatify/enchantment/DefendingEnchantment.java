package net.atlas.combatify.enchantment;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.ItemExtensions;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import static net.atlas.combatify.Combatify.id;

public class DefendingEnchantment extends Enchantment {
	public static final Enchantment DEFENDER = EnchantmentRegistry.registerEnchant(id("defender"), new DefendingEnchantment());

	public DefendingEnchantment() {
		super(Enchantment.definition(ItemTags.WEAPON_ENCHANTABLE, 1, 1, Enchantment.dynamicCost(25, 25), Enchantment.dynamicCost(75, 25), 3, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND));
	}

	@Override
	public boolean canEnchant(ItemStack stack) {
		return !((ItemExtensions) stack.getItem()).getBlockingType().isEmpty() && (!((ItemExtensions)stack.getItem()).getBlockingType().requiresSwordBlocking() || Combatify.CONFIG.swordBlocking());
	}

	public static void registerEnchants() {

	}
}
