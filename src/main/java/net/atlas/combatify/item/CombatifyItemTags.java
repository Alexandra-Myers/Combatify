package net.atlas.combatify.item;

import net.atlas.combatify.util.IDUtils;
import net.minecraft.core.registries.Registries;
//? >=1.21.11 {
/*import net.minecraft.resources.ResourceLocation;
*///?} <1.21.11 {
//?}
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class CombatifyItemTags {
	public static final TagKey<@NotNull Item> AXE_ENCHANTABLE = bind("enchantable/axe");
	public static final TagKey<@NotNull Item> BREACH_ENCHANTABLE = bind("enchantable/breach");
	public static final TagKey<@NotNull Item> PROJECTILES_WITH_COOLDOWNS = bind("combatify:projectiles_with_cooldowns");
	public static final TagKey<@NotNull Item> FAST_DRINKABLES = bind("combatify:fast_drinkables");
	public static final TagKey<@NotNull Item> PIGLIN_SAFE_HELD_ITEMS = bind("piglin_safe_held_items");
	private static TagKey<@NotNull Item> bind(String string) {
		return TagKey.create(Registries.ITEM, IDUtils.parse(string));
	}
	public static void init() {

	}
}
