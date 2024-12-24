package net.atlas.combatify.util;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record FakeUseItem(ItemStack stack, InteractionHand useHand, boolean isReal) {
	public Item getItem() {
		return stack.getItem();
	}
}
