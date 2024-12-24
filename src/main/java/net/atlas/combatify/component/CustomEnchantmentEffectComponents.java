package net.atlas.combatify.component;

import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.atlas.combatify.util.blocking.effect.PostBlockEffect;
import net.atlas.combatify.util.blocking.effect.PostBlockEffects;
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
	public static DataComponentType<List<ConditionalEffect<PostBlockEffect>>> POST_BLOCK_EFFECTS = register(
		"combatify:post_block_effects", builder -> builder.persistent(ConditionalEffect.codec(PostBlockEffects.MAP_CODEC.codec(), LootContextParamSets.ENCHANTED_DAMAGE).listOf())
	);
	public static DataComponentType<List<EnchantmentValueEffect>> SHIELD_EFFECTIVENESS = register(
		"combatify:shield_effectiveness", builder -> builder.persistent(EnchantmentValueEffect.CODEC.listOf())
	);
	public static DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> SHIELD_DISABLE = register(
		"combatify:shield_disable_time", builder -> builder.persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf())
	);
	private static <T> DataComponentType<T> register(String string, UnaryOperator<DataComponentType.Builder<T>> unaryOperator) {
		return Registry.<DataComponentType<T>>register(
			BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, string, ((DataComponentType.Builder)unaryOperator.apply(DataComponentType.builder())).build()
		);
	}
	public static void registerEnchantmentEffectComponents() {
		if (FabricLoader.getInstance().isModLoaded("polymer-core")) {
			PolymerComponent.registerEnchantmentEffectComponent(POST_BLOCK_EFFECTS);
			PolymerComponent.registerEnchantmentEffectComponent(SHIELD_EFFECTIVENESS);
			PolymerComponent.registerEnchantmentEffectComponent(SHIELD_DISABLE);
		}
	}
}
