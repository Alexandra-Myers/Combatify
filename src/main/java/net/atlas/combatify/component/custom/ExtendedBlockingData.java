package net.atlas.combatify.component.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.component.CustomDataComponents;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.atlas.combatify.util.MethodHandler;
import net.atlas.combatify.util.blocking.BlockingType;
import net.atlas.combatify.util.blocking.BlockingTypeInit;
import net.atlas.combatify.util.blocking.ComponentModifier;
import net.atlas.combatify.util.blocking.ComponentModifier.CombinedModifier;
import net.atlas.combatify.util.blocking.condition.*;
import net.atlas.combatify.util.blocking.effect.PostBlockEffectWrapper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableFloat;

public record ExtendedBlockingData(Tooltip tooltip, ResourceLocation blockingTypeLocation, PostBlockEffectWrapper postBlockEffect, BlockingCondition blockingCondition, boolean hasBanner) {
	public ExtendedBlockingData(Tooltip tooltip, ResourceLocation blockingTypeLocation, PostBlockEffectWrapper postBlockEffect, BlockingCondition blockingCondition) {
		this(tooltip, blockingTypeLocation, postBlockEffect, blockingCondition, false);
	}
	public static final ExtendedBlockingData EMPTY = new ExtendedBlockingData(new Tooltip(Collections.emptyList(), Collections.emptyList()), ResourceLocation.withDefaultNamespace("empty"), PostBlockEffectWrapper.DEFAULT, new AnyOf(Collections.emptyList()));
	public static final ExtendedBlockingData VANILLA_SHIELD = new ExtendedBlockingData(new Tooltip(Collections.emptyList(), Collections.emptyList()), ResourceLocation.withDefaultNamespace("shield"), PostBlockEffectWrapper.KNOCKBACK, Unconditional.INSTANCE);
	public static final ExtendedBlockingData NEW_SHIELD = new ExtendedBlockingData(new Tooltip(Collections.singletonList(BlockingTypeInit.NEW_SHIELD_PROTECTION), Collections.singletonList(BlockingTypeInit.NEW_SHIELD_KNOCKBACK)), ResourceLocation.withDefaultNamespace("new_shield"), PostBlockEffectWrapper.KNOCKBACK, Unconditional.INSTANCE);
	public static final Codec<ExtendedBlockingData> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(Tooltip.CODEC.forGetter(ExtendedBlockingData::tooltip),
				BlockingType.ID_CODEC.fieldOf("type").forGetter(ExtendedBlockingData::blockingTypeLocation),
				PostBlockEffectWrapper.CODEC.orElse(PostBlockEffectWrapper.KNOCKBACK).forGetter(ExtendedBlockingData::postBlockEffect),
				BlockingConditions.MAP_CODEC.orElse(Unconditional.INSTANCE).forGetter(ExtendedBlockingData::blockingCondition),
				Codec.BOOL.optionalFieldOf("considers_banner", false).forGetter(ExtendedBlockingData::hasBanner))
			.apply(instance, ExtendedBlockingData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ExtendedBlockingData> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.fromCodecTrusted(Tooltip.CODEC.codec()),
		ExtendedBlockingData::tooltip,
		ResourceLocation.STREAM_CODEC,
		ExtendedBlockingData::blockingTypeLocation,
		ByteBufCodecs.fromCodecWithRegistriesTrusted(PostBlockEffectWrapper.CODEC.codec()),
		ExtendedBlockingData::postBlockEffect,
		BlockingCondition.STREAM_CODEC,
		ExtendedBlockingData::blockingCondition,
		ByteBufCodecs.BOOL,
		ExtendedBlockingData::hasBanner,
		ExtendedBlockingData::new
	);

	public ExtendedBlockingData withProtection(List<CombinedModifier> protection) {
		return new ExtendedBlockingData(new Tooltip(protection, tooltip.knockbackModifiers), blockingTypeLocation, postBlockEffect, blockingCondition);
	}

	public ExtendedBlockingData withKnockback(List<ComponentModifier> knockback) {
		return new ExtendedBlockingData(new Tooltip(tooltip.protectionModifiers, knockback), blockingTypeLocation, postBlockEffect, blockingCondition);
	}

	public BlockingType blockingType() {
		if (blockingTypeLocation.equals(ResourceLocation.withDefaultNamespace("empty"))) return BlockingTypeInit.EMPTY;
		return Combatify.registeredTypes.get(blockingTypeLocation);
	}

	public void doEffect(ServerLevel serverLevel, EquipmentSlot equipmentSlot, ItemStack blockingItem, LivingEntity target, LivingEntity attacker, DamageSource damageSource) {
		if (postBlockEffect.matches(Enchantment.damageContext(serverLevel, 1, target, damageSource))) {
			LivingEntity applicable = switch (postBlockEffect.affected()) {
				case ATTACKER, DAMAGING_ENTITY -> attacker;
                case VICTIM -> target;
			};
			postBlockEffect.effect().doEffect(serverLevel, new EnchantedItemInUse(blockingItem, equipmentSlot, target), attacker, damageSource, 1, applicable, applicable.position());
		}
		CustomEnchantmentHelper.applyPostBlockedEffects(serverLevel, target, attacker, damageSource);
		MethodHandler.tryDisable(serverLevel, attacker, target, damageSource, blockingItem);
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean canUse(ItemStack itemStack, Level level, Player user, InteractionHand hand) {
		boolean stillRequiresCharge = Combatify.CONFIG.shieldOnlyWhenCharged() && user.getAttackStrengthScale(1.0F) < Combatify.CONFIG.shieldChargePercentage() / 100F && blockingType().requireFullCharge();
		if (stillRequiresCharge) return false;
		return blockingCondition.canUse(itemStack, level, user, hand);
	}

	public boolean canShowInTooltip(ItemStack itemStack, Player player) {
		return blockingCondition.canShowInToolTip(itemStack, player);
	}

	public record Tooltip(List<CombinedModifier> protectionModifiers, List<ComponentModifier> knockbackModifiers) {
		public static MapCodec<Tooltip> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(CombinedModifier.CODEC.listOf().fieldOf("protection_modifiers").forGetter(Tooltip::protectionModifiers),
				ComponentModifier.CODEC.listOf().optionalFieldOf("knockback_modifiers", Collections.emptyList()).forGetter(Tooltip::knockbackModifiers))
			.apply(instance, Tooltip::new));
		public void appendTooltipInfo(Consumer<Component> writer, Player player, ItemStack stack) {
			List<Component> protection = Collections.emptyList();
			List<Component> knockback = Collections.emptyList();
			int blockingLevel = stack.getOrDefault(CustomDataComponents.BLOCKING_LEVEL, 1);
			List<CombinedModifier> intermediaryProtection = protectionModifiers.stream().filter(combinedModifier -> combinedModifier.matches(stack)).toList();
			if (!intermediaryProtection.isEmpty()) protection = intermediaryProtection.getFirst().tryCombine(new ArrayList<>(intermediaryProtection), blockingLevel, player.getRandom());
			List<ComponentModifier> intermediaryKnockback = knockbackModifiers.stream().filter(componentModifier -> componentModifier.matches(stack)).toList();
			if (!intermediaryKnockback.isEmpty()) knockback = intermediaryKnockback.getFirst().tryCombine(new ArrayList<>(intermediaryKnockback), blockingLevel, player.getRandom());
			if (protection.isEmpty() && knockback.isEmpty()) return;
			writer.accept(CommonComponents.EMPTY);
			writer.accept(Component.translatableWithFallback("item.modifiers.use", "When used:").withStyle(ChatFormatting.GRAY));
			protection.forEach(component -> writer.accept(CommonComponents.space().append(component).withStyle(ChatFormatting.DARK_GREEN)));
			knockback.forEach(component -> writer.accept(CommonComponents.space().append(component).withStyle(ChatFormatting.DARK_GREEN)));
		}
		public float getShieldKnockbackResistanceValue(ItemStack itemStack, RandomSource randomSource) {
			int blockingLevel = itemStack.getOrDefault(CustomDataComponents.BLOCKING_LEVEL, 1);
			MutableFloat knockbackResistance = new MutableFloat(0);
			knockbackModifiers.stream().filter(componentModifier -> componentModifier.matches(itemStack)).forEach(componentModifier -> knockbackResistance.setValue(componentModifier.modifyValue(knockbackResistance.getValue(), blockingLevel, randomSource)));
			return knockbackResistance.getValue();
		}
	}
}
