package net.atlas.combatify.criterion;

import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.atlas.defaulted.init.registry.AbstractedHolder;
import net.atlas.defaulted.init.registry.Bootstrapper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import org.jspecify.annotations.NonNull;

public class DataComponentPredicateInit extends Bootstrapper<DataComponentPredicate.Type<?>> {
	public static final DataComponentPredicateInit INSTANCE = new DataComponentPredicateInit();
	public static AbstractedHolder<DataComponentPredicate.Type<?>, DataComponentPredicate.Type<@NonNull ItemBlockingLevelPredicate>> BLOCKING_LEVEL;

	public DataComponentPredicateInit() {
		super(Registries.DATA_COMPONENT_PREDICATE_TYPE, "combatify", BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE);
	}

	public static void registerDataComponentPredicates() {
		INSTANCE.init();
		if (FabricLoader.getInstance().isModLoaded("polymer-core")) {
			RegistrySyncUtils.setServerEntry(BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE, BLOCKING_LEVEL.get());
		}
	}

	@Override
	protected void bootstrap() {
		BLOCKING_LEVEL = register("blocking_level", () -> new DataComponentPredicate.ConcreteType<>(ItemBlockingLevelPredicate.CODEC));
	}
}
