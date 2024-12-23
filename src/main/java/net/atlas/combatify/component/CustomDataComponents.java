package net.atlas.combatify.component;

import com.mojang.serialization.Codec;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.atlas.combatify.config.item.Blocker;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;

import java.util.function.UnaryOperator;

public class CustomDataComponents {
	public static DataComponentType<Float> BLOCKING_LEVEL = register(
		"combatify:blocking_level", builder -> builder.persistent(Codec.FLOAT)
	);
	public static DataComponentType<Float> PIERCING_LEVEL = register(
		"combatify:piercing_level", builder -> builder.persistent(ExtraCodecs.POSITIVE_FLOAT)
	);
	private static <T> DataComponentType<T> register(String string, UnaryOperator<DataComponentType.Builder<T>> unaryOperator) {
		return Registry.<DataComponentType<T>>register(
			BuiltInRegistries.DATA_COMPONENT_TYPE, string, ((DataComponentType.Builder)unaryOperator.apply(DataComponentType.builder())).build()
		);
	}
	public static void registerDataComponents() {
		if (FabricLoader.getInstance().isModLoaded("polymer-core")) {
			PolymerComponent.registerDataComponent(BLOCKING_LEVEL);
			PolymerComponent.registerDataComponent(PIERCING_LEVEL);
		}
	}
}
