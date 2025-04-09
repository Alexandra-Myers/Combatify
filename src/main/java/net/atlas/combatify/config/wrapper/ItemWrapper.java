package net.atlas.combatify.config.wrapper;

import net.minecraft.world.item.Item;

public record ItemWrapper(Item value) implements GenericAPIWrapper<Item> {
	public String getID() {
		return value.toString();
	}

	@Override
	public Item unwrap() {
		return value;
	}
}
