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
		super(Rarity.VERY_RARE, ItemTags.WEAPON_ENCHANTABLE, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
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
		return !((ItemExtensions) stack.getItem()).getBlockingType().isEmpty() && (!((ItemExtensions)stack.getItem()).getBlockingType().requiresSwordBlocking() || Combatify.CONFIG.swordBlocking());
	}

	public static void registerEnchants() {

	}
}
