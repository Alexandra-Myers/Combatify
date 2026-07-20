package net.atlas.combatify.component;

import com.mojang.serialization.Codec;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.atlas.combatify.component.custom.ProtectionBaseFactor;
import net.atlas.combatify.util.blocking.effect.PostBlockEffect;
import net.atlas.combatify.util.blocking.effect.PostBlockEffects;
import net.atlas.defaulted.enchantment.generators.AddEffectGenerator;
import net.atlas.defaulted.init.registry.AbstractedHolder;
import net.atlas.defaulted.init.registry.Bootstrapper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.item.enchantment.TargetedConditionalEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.UnaryOperator;

public class CustomEnchantmentEffectComponents extends Bootstrapper<DataComponentType<?>> {
	public static final CustomEnchantmentEffectComponents INSTANCE = new CustomEnchantmentEffectComponents();
	public static AbstractedHolder<DataComponentType<?>, DataComponentType<@NonNull List<TargetedConditionalEffect<@NonNull PostBlockEffect>>>> POST_BLOCK_EFFECTS;
	public static AbstractedHolder<DataComponentType<?>, DataComponentType<@NonNull List<ProtectionBaseFactor>>> SHIELD_EFFECTIVENESS;
	public static AbstractedHolder<DataComponentType<?>, DataComponentType<@NonNull List<TargetedConditionalEffect<@NonNull EnchantmentValueEffect>>>> SHIELD_DISABLE;

	public CustomEnchantmentEffectComponents() {
		super(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, "combatify", BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE);
	}

	public <A> AbstractedHolder<DataComponentType<?>, DataComponentType<A>> register(String path, UnaryOperator<DataComponentType.Builder<@NonNull A>> unaryOperator) {
		return register(path, () -> unaryOperator.apply(DataComponentType.builder()).build());
	}

	private static <T extends Validatable> Codec<List<T>> validatedListCodec(final Codec<T> elementCodec, final ContextKeySet paramSet) {
		return elementCodec.listOf().validate(Validatable.listValidatorForContext(paramSet));
	}
	public static void registerEnchantmentEffectComponents() {
		INSTANCE.init();
		if (FabricLoader.getInstance().isModLoaded("polymer-core")) {
			PolymerComponent.registerEnchantmentEffectComponent(POST_BLOCK_EFFECTS.get());
			PolymerComponent.registerEnchantmentEffectComponent(SHIELD_EFFECTIVENESS.get());
			PolymerComponent.registerEnchantmentEffectComponent(SHIELD_DISABLE.get());
		}
		AddEffectGenerator.VALID_TYPES.add(CustomEnchantmentEffectComponents.SHIELD_DISABLE.get());
		AddEffectGenerator.VALID_TYPES.add(CustomEnchantmentEffectComponents.SHIELD_EFFECTIVENESS.get());
		AddEffectGenerator.VALID_TYPES.add(CustomEnchantmentEffectComponents.POST_BLOCK_EFFECTS.get());
	}

	@Override
	protected void bootstrap() {
		POST_BLOCK_EFFECTS = register("post_block_effects", builder -> builder.persistent(validatedListCodec(TargetedConditionalEffect.codec(PostBlockEffects.CODEC), LootContextParamSets.ENCHANTED_DAMAGE)));
		SHIELD_EFFECTIVENESS = register("shield_effectiveness", builder -> builder.persistent(ProtectionBaseFactor.CODEC.listOf()));
		SHIELD_DISABLE = register("shield_disable_time", builder -> builder.persistent(validatedListCodec(TargetedConditionalEffect.equipmentDropsCodec(EnchantmentValueEffect.CODEC), LootContextParamSets.ENCHANTED_DAMAGE)));
	}
}
