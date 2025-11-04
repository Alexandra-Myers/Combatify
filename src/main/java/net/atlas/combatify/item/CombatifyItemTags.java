package net.atlas.combatify.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class CombatifyItemTags {
	public static final TagKey<Item> AXE_ENCHANTABLE = bind("enchantable/axe");
	public static final TagKey<Item> BREACH_ENCHANTABLE = bind("enchantable/breach");
	public static final TagKey<Item> PIGLIN_SAFE_HELD_ITEMS = bind("piglin_safe_held_items");
	private static TagKey<Item> bind(String string) {
		return TagKey.create(Registries.ITEM, Identifier.tryParse(string));
	}
	public static void init() {

	}
}
