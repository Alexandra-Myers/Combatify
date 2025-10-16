package net.atlas.combatify.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.registries.DeferredItem;

import static net.atlas.combatify.item.CombatifyItems.registerItem;

public class ItemRegistry {
	public static final DeferredItem<Item> WOODEN_KNIFE = registerItem("wooden_knife", properties -> new KnifeItem(Tiers.WOOD, properties), new Item.Properties());
	public static final DeferredItem<Item> WOODEN_LONGSWORD = registerItem("wooden_longsword", properties -> new LongSwordItem(Tiers.WOOD, 0, properties), new Item.Properties());
	public static final DeferredItem<Item> STONE_KNIFE = registerItem("stone_knife", properties -> new KnifeItem(Tiers.STONE, properties), new Item.Properties());
	public static final DeferredItem<Item> STONE_LONGSWORD = registerItem("stone_longsword", properties -> new LongSwordItem(Tiers.STONE, 1, properties), new Item.Properties());
	public static final DeferredItem<Item> IRON_KNIFE = registerItem("iron_knife", properties -> new KnifeItem(Tiers.IRON, properties), new Item.Properties());
	public static final DeferredItem<Item> IRON_LONGSWORD = registerItem("iron_longsword", properties -> new LongSwordItem(Tiers.IRON, 2, properties), new Item.Properties());
	public static final DeferredItem<Item> GOLD_KNIFE = registerItem("golden_knife", properties -> new KnifeItem(Tiers.GOLD, properties), new Item.Properties());
	public static final DeferredItem<Item> GOLD_LONGSWORD = registerItem("golden_longsword", properties -> new LongSwordItem(Tiers.GOLD, 0, properties), new Item.Properties());
	public static final DeferredItem<Item> DIAMOND_KNIFE = registerItem("diamond_knife", properties -> new KnifeItem(Tiers.DIAMOND, properties), new Item.Properties());
	public static final DeferredItem<Item> DIAMOND_LONGSWORD = registerItem("diamond_longsword", properties -> new LongSwordItem(Tiers.DIAMOND, 3, properties), new Item.Properties());
	public static final DeferredItem<Item> NETHERITE_KNIFE = registerItem("netherite_knife", properties -> new KnifeItem(Tiers.NETHERITE, properties), new Item.Properties().fireResistant());
	public static final DeferredItem<Item> NETHERITE_LONGSWORD = registerItem("netherite_longsword", properties -> new LongSwordItem(Tiers.NETHERITE, 4, properties), new Item.Properties().fireResistant());

	public static void registerWeapons() {

	}
}
