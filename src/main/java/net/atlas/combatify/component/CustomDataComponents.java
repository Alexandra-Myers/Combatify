package net.atlas.combatify.component;

import com.mojang.serialization.Codec;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.component.custom.Blocker;
import net.atlas.combatify.component.custom.CanSweep;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class CustomDataComponents {
	private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, "combatify");
	public static final List<ResourceLocation> combatifyComponents = new ArrayList<>();
	public static DeferredHolder<DataComponentType<?>, DataComponentType<Blocker>> BLOCKER = register(
		"blocker", builder -> builder.persistent(Blocker.CODEC).networkSynchronized(Blocker.STREAM_CODEC)
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<CanSweep>> CAN_SWEEP = register(
		"can_sweep", builder -> builder.persistent(CanSweep.CODEC).networkSynchronized(CanSweep.STREAM_CODEC)
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> BLOCKING_LEVEL = register(
		"blocking_level", builder -> builder.persistent(ExtraCodecs.POSITIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT)
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<Float>> PIERCING_LEVEL = register(
		"piercing_level", builder -> builder.persistent(ExtraCodecs.POSITIVE_FLOAT).networkSynchronized(ByteBufCodecs.FLOAT)
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<Float>> CHARGED_REACH = register(
		"charged_reach", builder -> builder.persistent(Codec.floatRange(0, Float.MAX_VALUE)).networkSynchronized(ByteBufCodecs.FLOAT)
	);
	private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String string, UnaryOperator<DataComponentType.Builder<T>> unaryOperator) {
		combatifyComponents.add(Combatify.id(string));
		return DATA_COMPONENTS.registerComponentType(string, unaryOperator);
	}
	public static void registerDataComponents(IEventBus eventBus) {
		DATA_COMPONENTS.register(eventBus);
	}
	public static void postInit() {
		if (FabricLoader.getInstance().isModLoaded("polymer-core")) {
			PolymerComponent.registerDataComponent(BLOCKER.get());
			PolymerComponent.registerDataComponent(CAN_SWEEP.get());
			PolymerComponent.registerDataComponent(BLOCKING_LEVEL.get());
			PolymerComponent.registerDataComponent(PIERCING_LEVEL.get());
			PolymerComponent.registerDataComponent(CHARGED_REACH.get());
		}
	}
}
