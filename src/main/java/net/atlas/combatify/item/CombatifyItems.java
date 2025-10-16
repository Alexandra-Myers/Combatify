package net.atlas.combatify.item;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class CombatifyItems {
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("combatify");
	public static DeferredItem<Item> registerItem(String name, Item.Properties properties) {
		return registerItem(name, Item::new, properties);
	}
	public static DeferredItem<Item> registerItem(String name, Function<Item.Properties, Item> function, Item.Properties properties) {
		return CombatifyItems.ITEMS.registerItem(name, function, properties);
	}
	public static void register(IEventBus eventBus) {
		ITEMS.register(eventBus);
	}
}
