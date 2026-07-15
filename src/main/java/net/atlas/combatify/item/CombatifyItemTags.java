package net.atlas.combatify.item;

import net.atlas.combatify.util.IDUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.NonNull;

public class CombatifyItemTags {
	public static final TagKey<@NonNull Item> AXE_ENCHANTABLE = bind("enchantable/axe");
	public static final TagKey<@NonNull Item> BREACH_ENCHANTABLE = bind("enchantable/breach");
	public static final TagKey<@NonNull Item> PROJECTILES_WITH_COOLDOWNS = bind("combatify:projectiles_with_cooldowns");
	public static final TagKey<@NonNull Item> FAST_DRINKABLES = bind("combatify:fast_drinkables");
	public static final TagKey<@NonNull Item> PIGLIN_SAFE_HELD_ITEMS = bind("piglin_safe_held_items");
	private static TagKey<@NonNull Item> bind(String string) {
		return TagKey.create(Registries.ITEM, IDUtils.parse(string));
	}
	public static void init() {

	}
}
