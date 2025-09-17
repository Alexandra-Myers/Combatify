package net.atlas.combatify.item;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

import java.util.function.Function;

import static net.atlas.combatify.Combatify.id;

public class ItemRegistry {
	public static final Item WOODEN_KNIFE = registerItem(id("wooden_knife"), properties -> new KnifeItem(ToolMaterial.WOOD, properties), new Item.Properties());
	public static final Item WOODEN_LONGSWORD = registerItem(id("wooden_longsword"), properties -> new LongSwordItem(ToolMaterial.WOOD, 0, properties), new Item.Properties());
	public static final Item STONE_KNIFE = registerItem(id("stone_knife"), properties -> new KnifeItem(ToolMaterial.STONE, properties), new Item.Properties());
	public static final Item STONE_LONGSWORD = registerItem(id("stone_longsword"), properties -> new LongSwordItem(ToolMaterial.STONE, 1, properties), new Item.Properties());
	public static final Item IRON_KNIFE = registerItem(id("iron_knife"), properties -> new KnifeItem(ToolMaterial.IRON, properties), new Item.Properties());
	public static final Item IRON_LONGSWORD = registerItem(id("iron_longsword"), properties -> new LongSwordItem(ToolMaterial.IRON, 2, properties), new Item.Properties());
	public static final Item GOLD_KNIFE = registerItem(id("golden_knife"), properties -> new KnifeItem(ToolMaterial.GOLD, properties), new Item.Properties());
	public static final Item GOLD_LONGSWORD = registerItem(id("golden_longsword"), properties -> new LongSwordItem(ToolMaterial.GOLD, 0, properties), new Item.Properties());
	public static final Item DIAMOND_KNIFE = registerItem(id("diamond_knife"), properties -> new KnifeItem(ToolMaterial.DIAMOND, properties), new Item.Properties());
	public static final Item DIAMOND_LONGSWORD = registerItem(id("diamond_longsword"), properties -> new LongSwordItem(ToolMaterial.DIAMOND, 3, properties), new Item.Properties());
	public static final Item NETHERITE_KNIFE = registerItem(id("netherite_knife"), properties -> new KnifeItem(ToolMaterial.NETHERITE, properties), new Item.Properties().fireResistant());
	public static final Item NETHERITE_LONGSWORD = registerItem(id("netherite_longsword"), properties -> new LongSwordItem(ToolMaterial.NETHERITE, 4, properties), new Item.Properties().fireResistant());

	public static Item registerItem(ResourceLocation resourceLocation, Function<Item.Properties, Item> function, Item.Properties properties) {
		return registerItem(ResourceKey.create(Registries.ITEM, resourceLocation), function, properties);
	}
	public static Item registerItem(ResourceLocation resourceLocation, Item.Properties properties) {
		return registerItem(ResourceKey.create(Registries.ITEM, resourceLocation), Item::new, properties);
	}
	public static Item registerItem(ResourceKey<Item> resourceKey, Function<Item.Properties, Item> function, Item.Properties properties) {
		Item item = function.apply(properties.setId(resourceKey));
		if (item instanceof BlockItem blockItem) {
			blockItem.registerBlocks(Item.BY_BLOCK, item);
		}

		return Registry.register(BuiltInRegistries.ITEM, resourceKey, item);
	}
	public static void registerWeapons() {

	}
}
