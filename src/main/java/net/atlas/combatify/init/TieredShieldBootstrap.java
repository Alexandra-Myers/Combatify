package net.atlas.combatify.init;

import net.atlas.combatify.component.custom.ExtendedBlockingData;
import net.atlas.combatify.item.TieredShieldItem;
import net.atlas.combatify.util.blocking.BlockingTypeInit;
import net.atlas.defaulted.init.registry.AbstractedHolder;
import net.atlas.defaulted.init.registry.Bootstrapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.BlocksAttacks.ItemDamageFunction;

import java.util.Collections;
import java.util.function.Function;

public class TieredShieldBootstrap extends Bootstrapper<Item> {
	public static final TieredShieldBootstrap INSTANCE = new TieredShieldBootstrap();
	public static final ItemDamageFunction SHIELD_DAMAGE_FUNCTION = new ItemDamageFunction(3.0F, 0.0F, 1.0F);
	public static final ItemDamageFunction GOLDEN_SHIELD_DAMAGE_FUNCTION = new ItemDamageFunction(4.0F, 0.0F, 0.5F);
	public static AbstractedHolder<Item, TieredShieldItem> IRON_SHIELD;
	public static AbstractedHolder<Item, TieredShieldItem> GOLD_SHIELD;
	//? >=1.21.9
	public static AbstractedHolder<Item, TieredShieldItem> COPPER_SHIELD;
	public static AbstractedHolder<Item, TieredShieldItem> DIAMOND_SHIELD;
	public static AbstractedHolder<Item, TieredShieldItem> NETHERITE_SHIELD;
	public TieredShieldBootstrap() {
		super(Registries.ITEM, "combatify", BuiltInRegistries.ITEM);
	}

	public <A extends Item> AbstractedHolder<Item, A> registerItem(String path, Function<Item.Properties, A> itemFunction, Item.Properties itemProperties) {
		//? >1.21.1
		ResourceKey<Item> key = ResourceKey.create(this.getKey(), Identifier.fromNamespaceAndPath("combatify", path));
		return register(path, () -> itemFunction.apply(itemProperties/*? >1.21.1 {*/.setId(key)/*?}*/));
	}

	@Override
	protected void bootstrap() {
		IRON_SHIELD = registerItem("iron_shield", properties -> new TieredShieldItem(ToolMaterial.IRON, properties, 0, 0.5F, 3, 3, ExtendedBlockingData.NEW_SHIELD, SHIELD_DAMAGE_FUNCTION), new Item.Properties());
		GOLD_SHIELD = registerItem("golden_shield", properties -> new TieredShieldItem(ToolMaterial.GOLD, properties, -0.2F, 0.5F, 1, 4, ExtendedBlockingData.NEW_SHIELD, GOLDEN_SHIELD_DAMAGE_FUNCTION), new Item.Properties());
		//? >=1.21.9
		COPPER_SHIELD = registerItem("copper_shield", properties -> new TieredShieldItem(ToolMaterial.COPPER, properties, 0.1F, 1.25F, 2, 3, ExtendedBlockingData.NEW_SHIELD.withKnockback(Collections.singletonList(BlockingTypeInit.WEAK_SHIELD_KNOCKBACK)).withProtection(Collections.singletonList(BlockingTypeInit.COPPER_SHIELD_PROTECTION)), SHIELD_DAMAGE_FUNCTION), new Item.Properties());
		DIAMOND_SHIELD = registerItem("diamond_shield", properties -> new TieredShieldItem(ToolMaterial.DIAMOND, properties, 0, 0.5F, 5, 2, ExtendedBlockingData.NEW_SHIELD, SHIELD_DAMAGE_FUNCTION),new Item.Properties());
		NETHERITE_SHIELD = registerItem("netherite_shield", properties -> new TieredShieldItem(ToolMaterial.NETHERITE, properties, 0, 1, 5, 2, ExtendedBlockingData.NEW_SHIELD.withKnockback(Collections.singletonList(BlockingTypeInit.SHIELD_KNOCKBACK)).withProtection(Collections.singletonList(BlockingTypeInit.NETHERITE_SHIELD_PROTECTION)), SHIELD_DAMAGE_FUNCTION), new Item.Properties().fireResistant());
	}
}
