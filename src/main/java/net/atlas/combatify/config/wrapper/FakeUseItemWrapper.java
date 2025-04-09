package net.atlas.combatify.config.wrapper;

import net.atlas.combatify.util.FakeUseItem;

public record FakeUseItemWrapper(ItemStackWrapper stack, String useHand, boolean isReal) {
	public FakeUseItemWrapper(FakeUseItem wrapped) {
		this(new ItemStackWrapper(wrapped.stack()), wrapped.useHand().name(), wrapped.isReal());
	}
	public ItemWrapper getItem() {
		return stack.getItem();
	}
}
