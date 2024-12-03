package net.atlas.combatify.component;

import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.function.UnaryOperator;

public class CustomEnchantmentEffectComponents {
	public static DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> SHIELD_DISABLE = register(
		"combatify:shield_disable_time", builder -> builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf())
	);
	private static <T> DataComponentType<T> register(String string, UnaryOperator<DataComponentType.Builder<T>> unaryOperator) {
		return Registry.<DataComponentType<T>>register(
			BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, string, ((DataComponentType.Builder)unaryOperator.apply(DataComponentType.builder())).build()
		);
	}
	public static void registerEnchantmentEffectComponents() {
		if (FabricLoader.getInstance().isModLoaded("polymer-core"))
			PolymerComponent.registerEnchantmentEffectComponent(SHIELD_DISABLE);
	}
}
