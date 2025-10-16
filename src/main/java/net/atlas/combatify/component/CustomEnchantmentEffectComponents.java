package net.atlas.combatify.component;

import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.atlas.combatify.component.custom.ProtectionBaseFactor;
import net.atlas.combatify.util.blocking.effect.PostBlockEffect;
import net.atlas.combatify.util.blocking.effect.PostBlockEffects;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.TargetedConditionalEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.UnaryOperator;

public class CustomEnchantmentEffectComponents {
	private static final DeferredRegister.DataComponents ENCHANTMENT_EFFECT_COMPONENTS = DeferredRegister.createDataComponents(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, "combatify");
	public static DeferredHolder<DataComponentType<?>, DataComponentType<List<TargetedConditionalEffect<PostBlockEffect>>>> POST_BLOCK_EFFECTS = register(
		"post_block_effects", builder -> builder.persistent(TargetedConditionalEffect.codec(PostBlockEffects.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf())
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<List<ProtectionBaseFactor>>> SHIELD_EFFECTIVENESS = register(
		"shield_effectiveness", builder -> builder.persistent(ProtectionBaseFactor.CODEC.listOf())
	);
	public static DeferredHolder<DataComponentType<?>, DataComponentType<List<TargetedConditionalEffect<EnchantmentValueEffect>>>> SHIELD_DISABLE = register(
		"shield_disable_time", builder -> builder.persistent(TargetedConditionalEffect.equipmentDropsCodec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf())
	);
	private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String string, UnaryOperator<DataComponentType.Builder<T>> unaryOperator) {
		return ENCHANTMENT_EFFECT_COMPONENTS.registerComponentType(string, unaryOperator);
	}
	public static void registerEnchantmentEffectComponents(IEventBus bus) {
		ENCHANTMENT_EFFECT_COMPONENTS.register(bus);
	}
	public static void postInit() {
		if (FabricLoader.getInstance().isModLoaded("polymer-core")) {
			PolymerComponent.registerEnchantmentEffectComponent(POST_BLOCK_EFFECTS.get());
			PolymerComponent.registerEnchantmentEffectComponent(SHIELD_EFFECTIVENESS.get());
			PolymerComponent.registerEnchantmentEffectComponent(SHIELD_DISABLE.get());
		}
	}
}
