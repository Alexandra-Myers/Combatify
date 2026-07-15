package net.atlas.combatify.init;

import net.atlas.combatify.item.KnifeItem;
import net.atlas.combatify.item.LongSwordItem;
import net.atlas.defaulted.init.registry.AbstractedHolder;
import net.atlas.defaulted.init.registry.Bootstrapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
//? >1.21.1 {
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
//?}
import net.minecraft.world.item.Item;
//? >=1.21.4
import net.minecraft.world.item.ToolMaterial;
//? <1.21.4
//import net.minecraft.world.item.ToolMaterials;

import java.util.function.Function;

public class ItemRegistry extends Bootstrapper<Item> {
	public static final ItemRegistry INSTANCE = new ItemRegistry();
	public static AbstractedHolder<Item, KnifeItem> WOODEN_KNIFE;
	public static AbstractedHolder<Item, LongSwordItem> WOODEN_LONGSWORD;
	public static AbstractedHolder<Item, KnifeItem> STONE_KNIFE;
	public static AbstractedHolder<Item, LongSwordItem> STONE_LONGSWORD;
	//? >=1.21.9 {
	public static AbstractedHolder<Item, KnifeItem> COPPER_KNIFE;
	public static AbstractedHolder<Item, LongSwordItem> COPPER_LONGSWORD;
	//?}
	public static AbstractedHolder<Item, KnifeItem> IRON_KNIFE;
	public static AbstractedHolder<Item, LongSwordItem> IRON_LONGSWORD;
	public static AbstractedHolder<Item, KnifeItem> GOLD_KNIFE;
	public static AbstractedHolder<Item, LongSwordItem> GOLD_LONGSWORD;
	public static AbstractedHolder<Item, KnifeItem> DIAMOND_KNIFE;
	public static AbstractedHolder<Item, LongSwordItem> DIAMOND_LONGSWORD;
	public static AbstractedHolder<Item, KnifeItem> NETHERITE_KNIFE;
	public static AbstractedHolder<Item, LongSwordItem> NETHERITE_LONGSWORD;

	public ItemRegistry() {
		super(Registries.ITEM, "combatify", BuiltInRegistries.ITEM);
	}


	public <A extends Item> AbstractedHolder<Item, A> registerItem(String path, Function<Item.Properties, A> itemFunction, Item.Properties itemProperties) {
		//? >1.21.1
		ResourceKey<Item> key = ResourceKey.create(this.getKey(), Identifier.fromNamespaceAndPath("combatify", path));
		return register(path, () -> itemFunction.apply(itemProperties/*? >1.21.1 {*/.setId(key)/*?}*/));
	}

	@Override
	protected void bootstrap() {
		WOODEN_KNIFE = registerItem("wooden_knife", properties -> new KnifeItem(ToolMaterial.WOOD, properties), new Item.Properties());
		WOODEN_LONGSWORD = registerItem("wooden_longsword", properties -> new LongSwordItem(ToolMaterial.WOOD, 0, properties), new Item.Properties());
		STONE_KNIFE = registerItem("stone_knife", properties -> new KnifeItem(ToolMaterial.STONE, properties), new Item.Properties());
		STONE_LONGSWORD = registerItem("stone_longsword", properties -> new LongSwordItem(ToolMaterial.STONE, 1, properties), new Item.Properties());
		//? >=1.21.9 {
		COPPER_KNIFE = registerItem("copper_knife", properties -> new KnifeItem(ToolMaterial.COPPER, properties), new Item.Properties());
		COPPER_LONGSWORD = registerItem("copper_longsword", properties -> new LongSwordItem(ToolMaterial.COPPER, 1, properties), new Item.Properties());
		//?}
		IRON_KNIFE = registerItem("iron_knife", properties -> new KnifeItem(ToolMaterial.IRON, properties), new Item.Properties());
		IRON_LONGSWORD = registerItem("iron_longsword", properties -> new LongSwordItem(ToolMaterial.IRON, 2, properties), new Item.Properties());
		GOLD_KNIFE = registerItem("golden_knife", properties -> new KnifeItem(ToolMaterial.GOLD, properties), new Item.Properties());
		GOLD_LONGSWORD = registerItem("golden_longsword", properties -> new LongSwordItem(ToolMaterial.GOLD, 0, properties), new Item.Properties());
		DIAMOND_KNIFE = registerItem("diamond_knife", properties -> new KnifeItem(ToolMaterial.DIAMOND, properties), new Item.Properties());
		DIAMOND_LONGSWORD = registerItem("diamond_longsword", properties -> new LongSwordItem(ToolMaterial.DIAMOND, 3, properties), new Item.Properties());
		NETHERITE_KNIFE = registerItem("netherite_knife", properties -> new KnifeItem(ToolMaterial.NETHERITE, properties), new Item.Properties().fireResistant());
		NETHERITE_LONGSWORD = registerItem("netherite_longsword", properties -> new LongSwordItem(ToolMaterial.NETHERITE, 4, properties), new Item.Properties().fireResistant());
	}
}
