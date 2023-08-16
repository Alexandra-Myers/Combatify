package net.atlas.combat_enhanced.item;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;

import static net.atlas.combat_enhanced.CombatEnhanced.id;

public class ItemRegistry {
	public static final Item WOODEN_KNIFE = registerItem(id("wooden_knife"), new KnifeItem(Tiers.WOOD, new Item.Properties()));
	public static final Item WOODEN_LONGSWORD = registerItem(id("wooden_longsword"), new LongSwordItem(Tiers.WOOD, new Item.Properties()));
	public static final Item STONE_KNIFE = registerItem(id("stone_knife"), new KnifeItem(Tiers.STONE, new Item.Properties()));
	public static final Item STONE_LONGSWORD = registerItem(id("stone_longsword"), new LongSwordItem(Tiers.STONE, new Item.Properties()));
	public static final Item IRON_KNIFE = registerItem(id("iron_knife"), new KnifeItem(Tiers.IRON, new Item.Properties()));
	public static final Item IRON_LONGSWORD = registerItem(id("iron_longsword"), new LongSwordItem(Tiers.IRON, new Item.Properties()));
	public static final Item GOLD_KNIFE = registerItem(id("golden_knife"), new KnifeItem(Tiers.GOLD, new Item.Properties()));
	public static final Item GOLD_LONGSWORD = registerItem(id("golden_longsword"), new LongSwordItem(Tiers.GOLD, new Item.Properties()));
	public static final Item DIAMOND_KNIFE = registerItem(id("diamond_knife"), new KnifeItem(Tiers.DIAMOND, new Item.Properties()));
	public static final Item DIAMOND_LONGSWORD = registerItem(id("diamond_longsword"), new LongSwordItem(Tiers.DIAMOND, new Item.Properties()));
	public static final Item NETHERITE_KNIFE = registerItem(id("netherite_knife"), new KnifeItem(Tiers.NETHERITE, new Item.Properties().fireResistant()));
	public static final Item NETHERITE_LONGSWORD = registerItem(id("netherite_longsword"), new LongSwordItem(Tiers.NETHERITE, new Item.Properties().fireResistant()));
	public static Item registerItem(ResourceLocation resourceLocation, Item item) {
		if (item instanceof BlockItem) {
			((BlockItem)item).registerBlocks(Item.BY_BLOCK, item);
		}

		return Registry.register(BuiltInRegistries.ITEM, resourceLocation, item);
	}
	public static void registerWeapons() {

	}
}
