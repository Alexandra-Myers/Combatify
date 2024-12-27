package net.atlas.combatify.critereon;

import com.mojang.serialization.Codec;
import net.atlas.combatify.Combatify;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class ItemSubPredicateInit {
	public static final ItemSubPredicate.Type<ItemBlockingLevelPredicate> BLOCKING_LEVEL = register("blocking_level", ItemBlockingLevelPredicate.CODEC);
	public static final ItemSubPredicate.Type<ItemHasComponentPredicate> HAS_COMPONENT = register(
		"has_component", ItemHasComponentPredicate.CODEC
	);
	private static <T extends ItemSubPredicate> ItemSubPredicate.Type<T> register(String string, Codec<T> codec) {
		return Registry.register(BuiltInRegistries.ITEM_SUB_PREDICATE_TYPE, Combatify.id(string), new ItemSubPredicate.Type<>(codec));
	}
	public static void init() {

	}
}
