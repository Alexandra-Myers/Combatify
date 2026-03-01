package net.atlas.combatify.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class CombatifyItemTags {
	public static final TagKey<@NotNull Item> AXE_ENCHANTABLE = bind("enchantable/axe");
	public static final TagKey<@NotNull Item> BREACH_ENCHANTABLE = bind("enchantable/breach");
	public static final TagKey<@NotNull Item> PIGLIN_SAFE_HELD_ITEMS = bind("piglin_safe_held_items");
	private static TagKey<@NotNull Item> bind(String string) {
		return TagKey.create(Registries.ITEM, Identifier.tryParse(string));
	}
	public static void init() {

	}
}
