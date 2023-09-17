package net.atlas.combatify.item;

import net.atlas.combatify.Combatify;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static net.atlas.combatify.Combatify.id;

public class ItemRegistry {
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Combatify.MOD_ID);
	public static final RegistryObject<Item> WOODEN_KNIFE = registerItem(id("wooden_knife"), new KnifeItem(Tiers.WOOD, new Item.Properties()));
	public static final RegistryObject<Item> WOODEN_LONGSWORD = registerItem(id("wooden_longsword"), new LongSwordItem(Tiers.WOOD, new Item.Properties()));
	public static final RegistryObject<Item> STONE_KNIFE = registerItem(id("stone_knife"), new KnifeItem(Tiers.STONE, new Item.Properties()));
	public static final RegistryObject<Item> STONE_LONGSWORD = registerItem(id("stone_longsword"), new LongSwordItem(Tiers.STONE, new Item.Properties()));
	public static final RegistryObject<Item> IRON_KNIFE = registerItem(id("iron_knife"), new KnifeItem(Tiers.IRON, new Item.Properties()));
	public static final RegistryObject<Item> IRON_LONGSWORD = registerItem(id("iron_longsword"), new LongSwordItem(Tiers.IRON, new Item.Properties()));
	public static final RegistryObject<Item> GOLD_KNIFE = registerItem(id("golden_knife"), new KnifeItem(Tiers.GOLD, new Item.Properties()));
	public static final RegistryObject<Item> GOLD_LONGSWORD = registerItem(id("golden_longsword"), new LongSwordItem(Tiers.GOLD, new Item.Properties()));
	public static final RegistryObject<Item> DIAMOND_KNIFE = registerItem(id("diamond_knife"), new KnifeItem(Tiers.DIAMOND, new Item.Properties()));
	public static final RegistryObject<Item> DIAMOND_LONGSWORD = registerItem(id("diamond_longsword"), new LongSwordItem(Tiers.DIAMOND, new Item.Properties()));
	public static final RegistryObject<Item> NETHERITE_KNIFE = registerItem(id("netherite_knife"), new KnifeItem(Tiers.NETHERITE, new Item.Properties().fireResistant()));
	public static final RegistryObject<Item> NETHERITE_LONGSWORD = registerItem(id("netherite_longsword"), new LongSwordItem(Tiers.NETHERITE, new Item.Properties().fireResistant()));
	public static RegistryObject<Item> registerItem(ResourceLocation resourceLocation, Item item) {
		if (item instanceof BlockItem) {
			((BlockItem)item).registerBlocks(Item.BY_BLOCK, item);
		}

		return ITEMS.register(resourceLocation.getPath(), () -> item);
	}
	public static void registerWeapons(IEventBus bus) {
		ITEMS.register(bus);
	}
}
