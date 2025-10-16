package net.atlas.combatify.critereon;

import com.mojang.serialization.Codec;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemSubPredicateInit {
	private static final DeferredRegister<ItemSubPredicate.Type<?>> ITEM_SUB_PREDICATES = DeferredRegister.create(Registries.ITEM_SUB_PREDICATE_TYPE, "combatify");
	public static final DeferredHolder<ItemSubPredicate.Type<?>, ItemSubPredicate.Type<ItemBlockingLevelPredicate>> BLOCKING_LEVEL = register("blocking_level", ItemBlockingLevelPredicate.CODEC);
	public static final DeferredHolder<ItemSubPredicate.Type<?>, ItemSubPredicate.Type<ItemHasComponentPredicate>> HAS_COMPONENT = register(
		"has_component", ItemHasComponentPredicate.CODEC
	);
	private static <T extends ItemSubPredicate> DeferredHolder<ItemSubPredicate.Type<?>, ItemSubPredicate.Type<T>> register(String string, Codec<T> codec) {
		return ITEM_SUB_PREDICATES.register(string, () -> new ItemSubPredicate.Type<>(codec));
	}
	public static void init(IEventBus eventBus) {
		ITEM_SUB_PREDICATES.register(eventBus);
	}
	public static void postInit() {
		if (FabricLoader.getInstance().isModLoaded("polymer-core")) {
			RegistrySyncUtils.setServerEntry(BuiltInRegistries.ITEM_SUB_PREDICATE_TYPE, BLOCKING_LEVEL.get());
			RegistrySyncUtils.setServerEntry(BuiltInRegistries.ITEM_SUB_PREDICATE_TYPE, HAS_COMPONENT.get());
		}
	}
}
