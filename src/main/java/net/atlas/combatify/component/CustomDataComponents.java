package net.atlas.combatify.component;

import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.component.custom.ExtendedBlockingData;
import net.atlas.combatify.component.custom.CanSweep;
import net.atlas.combatify.util.CommonUtils;
import net.atlas.defaulted.init.registry.AbstractedHolder;
import net.atlas.defaulted.init.registry.Bootstrapper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class CustomDataComponents extends Bootstrapper<DataComponentType<?>> {
	public static final CustomDataComponents INSTANCE = new CustomDataComponents();
	public static final List<Identifier> combatifyComponents = new ArrayList<>();
	public static AbstractedHolder<DataComponentType<?>, DataComponentType<@NonNull ExtendedBlockingData>> EXTENDED_BLOCKING_DATA;
	public static AbstractedHolder<DataComponentType<?>, DataComponentType<@NonNull CanSweep>> CAN_SWEEP;
	public static AbstractedHolder<DataComponentType<?>, DataComponentType<@NonNull Integer>> BLOCKING_LEVEL;
	public static AbstractedHolder<DataComponentType<?>, DataComponentType<@NonNull Float>> PIERCING_LEVEL;
	public static AbstractedHolder<DataComponentType<?>, DataComponentType<@NonNull Float>> CHARGED_REACH;

	public CustomDataComponents() {
		super(Registries.DATA_COMPONENT_TYPE, "combatify", BuiltInRegistries.DATA_COMPONENT_TYPE);
	}

	public <A> AbstractedHolder<DataComponentType<?>, DataComponentType<A>> register(String path, UnaryOperator<DataComponentType.Builder<@NonNull A>> unaryOperator) {
		combatifyComponents.add(Combatify.id(path));
		return register(path, () -> unaryOperator.apply(DataComponentType.builder()).build());
	}

	public static void registerDataComponents() {
		INSTANCE.init();
		if (FabricLoader.getInstance().isModLoaded("polymer-core")) {
			PolymerComponent.registerDataComponent(EXTENDED_BLOCKING_DATA.get());
			PolymerComponent.registerDataComponent(CAN_SWEEP.get());
			PolymerComponent.registerDataComponent(BLOCKING_LEVEL.get());
			PolymerComponent.registerDataComponent(PIERCING_LEVEL.get());
			PolymerComponent.registerDataComponent(CHARGED_REACH.get());
		}
	}

	@Override
	protected void bootstrap() {
		EXTENDED_BLOCKING_DATA = register("extended_blocking_data", builder -> builder.persistent(ExtendedBlockingData.CODEC).networkSynchronized(ExtendedBlockingData.STREAM_CODEC));
		CAN_SWEEP = register("can_sweep", builder -> builder.persistent(CanSweep.CODEC).networkSynchronized(CanSweep.STREAM_CODEC));
		BLOCKING_LEVEL = register("blocking_level", builder -> builder.persistent(ExtraCodecs.POSITIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT));
		PIERCING_LEVEL = register("piercing_level", builder -> builder.persistent(ExtraCodecs.POSITIVE_FLOAT).networkSynchronized(ByteBufCodecs.FLOAT));
		CHARGED_REACH = register("charged_reach", builder -> builder.persistent(CommonUtils.NON_NEGATIVE_FLOAT).networkSynchronized(ByteBufCodecs.FLOAT));
	}
}
