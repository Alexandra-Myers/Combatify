package net.atlas.combatify.critereon;

import com.mojang.serialization.Codec;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.atlas.combatify.Combatify;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;

public class DataComponentPredicateInit {
	public static final DataComponentPredicate.Type<ItemBlockingLevelPredicate> BLOCKING_LEVEL = register("blocking_level", ItemBlockingLevelPredicate.CODEC);
	public static final DataComponentPredicate.Type<ItemHasComponentPredicate> HAS_COMPONENT = register(
		"has_component", ItemHasComponentPredicate.CODEC
	);
	private static <T extends DataComponentPredicate> DataComponentPredicate.Type<T> register(String string, Codec<T> codec) {
		return Registry.register(BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE, Combatify.id(string), new DataComponentPredicate.Type<>(codec));
	}
	public static void init() {
		if (FabricLoader.getInstance().isModLoaded("polymer-core")) {
			RegistrySyncUtils.setServerEntry(BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE, BLOCKING_LEVEL);
			RegistrySyncUtils.setServerEntry(BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE, HAS_COMPONENT);
		}
	}
}
