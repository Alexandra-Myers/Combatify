package net.atlas.combatify.component;

import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.atlas.combatify.component.custom.ExtendedBlockingData;
import net.atlas.combatify.component.custom.CanSweep;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class CustomDataComponents {
	public static final List<ResourceLocation> combatifyComponents = new ArrayList<>();
	public static DataComponentType<ExtendedBlockingData> EXTENDED_BLOCKING_DATA = register(
		"combatify:extended_blocking_data", builder -> builder.persistent(ExtendedBlockingData.CODEC).networkSynchronized(ExtendedBlockingData.STREAM_CODEC)
	);
	public static DataComponentType<CanSweep> CAN_SWEEP = register(
		"combatify:can_sweep", builder -> builder.persistent(CanSweep.CODEC).networkSynchronized(CanSweep.STREAM_CODEC)
	);
	public static DataComponentType<Integer> BLOCKING_LEVEL = register(
		"combatify:blocking_level", builder -> builder.persistent(ExtraCodecs.POSITIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT)
	);
	public static DataComponentType<Float> PIERCING_LEVEL = register(
		"combatify:piercing_level", builder -> builder.persistent(ExtraCodecs.POSITIVE_FLOAT).networkSynchronized(ByteBufCodecs.FLOAT)
	);
	private static <T> DataComponentType<T> register(String string, UnaryOperator<DataComponentType.Builder<T>> unaryOperator) {
		combatifyComponents.add(ResourceLocation.parse(string));
		return Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE, string, unaryOperator.apply(DataComponentType.builder()).build()
		);
	}
	public static void registerDataComponents() {
		if (FabricLoader.getInstance().isModLoaded("polymer-core")) {
			PolymerComponent.registerDataComponent(EXTENDED_BLOCKING_DATA);
			PolymerComponent.registerDataComponent(CAN_SWEEP);
			PolymerComponent.registerDataComponent(BLOCKING_LEVEL);
			PolymerComponent.registerDataComponent(PIERCING_LEVEL);
		}
	}
}
