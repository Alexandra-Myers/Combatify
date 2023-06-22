package net.alexandra.atlas.atlas_combat.item;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;

public class ItemRegistry {
	public static final Item WOODEN_KNIFE = registerItem(new ResourceLocation(AtlasCombat.MOD_ID, "wooden_knife"), new KnifeItem(Tiers.WOOD, new Item.Properties()));
	public static final Item WOODEN_LONGSWORD = registerItem(new ResourceLocation(AtlasCombat.MOD_ID, "wooden_longsword"), new LongSwordItem(Tiers.WOOD, new Item.Properties()));
	public static final Item STONE_KNIFE = registerItem(new ResourceLocation(AtlasCombat.MOD_ID, "stone_knife"), new KnifeItem(Tiers.STONE, new Item.Properties()));
	public static final Item STONE_LONGSWORD = registerItem(new ResourceLocation(AtlasCombat.MOD_ID, "stone_longsword"), new LongSwordItem(Tiers.STONE, new Item.Properties()));
	public static final Item IRON_KNIFE = registerItem(new ResourceLocation(AtlasCombat.MOD_ID, "iron_knife"), new KnifeItem(Tiers.IRON, new Item.Properties()));
	public static final Item IRON_LONGSWORD = registerItem(new ResourceLocation(AtlasCombat.MOD_ID, "iron_longsword"), new LongSwordItem(Tiers.IRON, new Item.Properties()));
	public static final Item GOLD_KNIFE = registerItem(new ResourceLocation(AtlasCombat.MOD_ID, "golden_knife"), new KnifeItem(Tiers.GOLD, new Item.Properties()));
	public static final Item GOLD_LONGSWORD = registerItem(new ResourceLocation(AtlasCombat.MOD_ID, "golden_longsword"), new LongSwordItem(Tiers.GOLD, new Item.Properties()));
	public static final Item DIAMOND_KNIFE = registerItem(new ResourceLocation(AtlasCombat.MOD_ID, "diamond_knife"), new KnifeItem(Tiers.DIAMOND, new Item.Properties()));
	public static final Item DIAMOND_LONGSWORD = registerItem(new ResourceLocation(AtlasCombat.MOD_ID, "diamond_longsword"), new LongSwordItem(Tiers.DIAMOND, new Item.Properties()));
	public static final Item NETHERITE_KNIFE = registerItem(new ResourceLocation(AtlasCombat.MOD_ID, "netherite_knife"), new KnifeItem(Tiers.NETHERITE, new Item.Properties().fireResistant()));
	public static final Item NETHERITE_LONGSWORD = registerItem(new ResourceLocation(AtlasCombat.MOD_ID, "netherite_longsword"), new LongSwordItem(Tiers.NETHERITE, new Item.Properties().fireResistant()));
	public static Item registerItem(ResourceLocation resourceLocation, Item item) {
		if (item instanceof BlockItem) {
			((BlockItem)item).registerBlocks(Item.BY_BLOCK, item);
		}

		return Registry.register(BuiltInRegistries.ITEM, resourceLocation, item);
	}
	public static void registerWeapons() {

	}
}
