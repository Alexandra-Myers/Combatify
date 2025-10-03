package net.atlas.combatify.component.custom;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
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
import net.atlas.combatify.util.blocking.ComponentModifier.DataSet;
import net.atlas.combatify.util.blocking.condition.*;
import net.atlas.combatify.util.blocking.damage_parsers.DamageParser;
import net.atlas.combatify.util.blocking.damage_parsers.PercentageBase;
import net.atlas.combatify.util.blocking.damage_parsers.Nullify;
import net.atlas.combatify.util.blocking.effect.PostBlockEffectWrapper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableFloat;

import static net.atlas.combatify.util.MethodHandler.arrowDisable;
import static net.atlas.combatify.util.MethodHandler.getBlockingType;

public record Blocker(List<DamageParser> damageParsers, Tooltip tooltip, ResourceLocation blockingTypeLocation, float useSeconds, PostBlockEffectWrapper postBlockEffect, BlockingCondition blockingCondition) {
	public Blocker(List<DamageParser> damageParsers, Tooltip tooltip, ResourceLocation blockingTypeLocation, float useSeconds, BlockingCondition blockingCondition) {
		this(damageParsers, tooltip, blockingTypeLocation, useSeconds, PostBlockEffectWrapper.DEFAULT, blockingCondition);
	}
	public static final Blocker EMPTY = new Blocker(Collections.emptyList(), new Tooltip(Collections.emptyList(), Collections.emptyList(), false), ResourceLocation.withDefaultNamespace("empty"), 0, PostBlockEffectWrapper.DEFAULT, new AnyOf(Collections.emptyList()));
	public static final Blocker VANILLA_SHIELD = new Blocker(Collections.singletonList(Nullify.NULLIFY_ALL), new Tooltip(Collections.emptyList(), Collections.emptyList(), true), ResourceLocation.withDefaultNamespace("shield"), 3600, PostBlockEffectWrapper.KNOCKBACK, Unconditional.INSTANCE);
	public static final Blocker NEW_SHIELD = new Blocker(List.of(PercentageBase.IGNORE_EXPLOSIONS_AND_PROJECTILES, Nullify.NULLIFY_EXPLOSIONS_AND_PROJECTILES), new Tooltip(Collections.singletonList(BlockingTypeInit.NEW_SHIELD_PROTECTION), Collections.singletonList(BlockingTypeInit.NEW_SHIELD_KNOCKBACK), true), ResourceLocation.withDefaultNamespace("new_shield"), 3600, PostBlockEffectWrapper.KNOCKBACK, Unconditional.INSTANCE);
	public static final Codec<Blocker> CODEC = RecordCodecBuilder.create(instance ->
	instance.group(DamageParser.CODEC.listOf().fieldOf("damage_parsers").forGetter(Blocker::damageParsers),
			Tooltip.CODEC.forGetter(Blocker::tooltip),
			BlockingType.ID_CODEC.fieldOf("type").forGetter(Blocker::blockingTypeLocation),
			ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("seconds", 3600F).forGetter(Blocker::useSeconds),
			PostBlockEffectWrapper.CODEC.orElse(PostBlockEffectWrapper.KNOCKBACK).forGetter(Blocker::postBlockEffect),
			BlockingConditions.MAP_CODEC.orElse(Unconditional.INSTANCE).forGetter(Blocker::blockingCondition))
		.apply(instance, Blocker::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, Blocker> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.fromCodecTrusted(DamageParser.CODEC)),
		Blocker::damageParsers,
		ByteBufCodecs.fromCodecTrusted(Tooltip.CODEC.codec()),
		Blocker::tooltip,
		ResourceLocation.STREAM_CODEC,
		Blocker::blockingTypeLocation,
		ByteBufCodecs.FLOAT,
		Blocker::useSeconds,
		ByteBufCodecs.fromCodecWithRegistriesTrusted(PostBlockEffectWrapper.CODEC.codec()),
		Blocker::postBlockEffect,
		BlockingCondition.STREAM_CODEC,
		Blocker::blockingCondition,
		Blocker::new
	);

	public Blocker withProtection(List<CombinedModifier> protection) {
		return new Blocker(damageParsers, new Tooltip(protection, tooltip.knockbackModifiers, tooltip.markBlocked), blockingTypeLocation, useSeconds, postBlockEffect, blockingCondition);
	}

	public Blocker withKnockback(List<ComponentModifier> knockback) {
		return new Blocker(damageParsers, new Tooltip(tooltip.protectionModifiers, knockback, tooltip.markBlocked), blockingTypeLocation, useSeconds, postBlockEffect, blockingCondition);
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
		MethodHandler.disableShield(attacker, target, damageSource, blockingItem);
	}

	public int useTicks() {
		return (int) (useSeconds * 20.0F);
	}

	public void block(ServerLevel serverLevel, LivingEntity instance, DamageSource source, ItemStack itemStack, LocalFloatRef amount, LocalFloatRef protectedDamage, LocalBooleanRef blocked) {
		if (blockingCondition.canBlock(serverLevel, instance, itemStack, source, amount.get())) {
			if (getBlockingType(itemStack).hasDelay() && Combatify.CONFIG.shieldDelay() > 0 && itemStack.getUseDuration(instance) - instance.getUseItemRemainingTicks() < Combatify.CONFIG.shieldDelay()) {
				if (Combatify.CONFIG.disableDuringShieldDelay())
					if (source.getDirectEntity() instanceof LivingEntity attacker)
						MethodHandler.disableShield(attacker, instance, source, itemStack);
				return;
			}
			completeBlock(serverLevel, instance, itemStack, source, amount, protectedDamage, blocked);
		}
	}
	public void completeBlock(ServerLevel serverLevel, LivingEntity instance, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef protectedDamage, LocalBooleanRef wasBlocked) {
		int blockingLevel = blockingItem.getOrDefault(CustomDataComponents.BLOCKING_LEVEL, 1);
		DataSet protection = new DataSet(0, 0);
		List<CombinedModifier> intermediaryProtection = tooltip.protectionModifiers().stream().filter(combinedModifier -> combinedModifier.matches(blockingItem)).toList();
		if (!intermediaryProtection.isEmpty()) protection = intermediaryProtection.getFirst().tryCombineVal(intermediaryProtection, blockingLevel, instance.getRandom());
		final DataSet endProtection = CustomEnchantmentHelper.modifyShieldEffectiveness(blockingItem, instance.getRandom(), protection);
		damageParsers.forEach(damageParserConditionalEffect -> {
			protectedDamage.set(damageParserConditionalEffect.parse(amount.get(), endProtection, source.typeHolder()));
			amount.set(Math.max(amount.get() - protectedDamage.get(), 0));
		});
		MethodHandler.hurtCurrentlyUsedShield(instance, protectedDamage.get());
		if (source.getDirectEntity() instanceof LivingEntity livingEntity)
			MethodHandler.blockedByShield(serverLevel, instance, livingEntity, source);
		switch (source.getDirectEntity()) {
			case Arrow arrow when Combatify.CONFIG.arrowDisableMode().satisfiesConditions(arrow) ->
				arrowDisable(instance, source, arrow, blockingItem);
			case SpectralArrow arrow when Combatify.CONFIG.arrowDisableMode().satisfiesConditions(arrow) ->
				arrowDisable(instance, source, arrow, blockingItem);
			case null, default -> {
				// Do nothing
			}
		}
		wasBlocked.set(tooltip.markBlocked);
	}

	public InteractionResult use(ItemStack itemStack, Level level, Player user, InteractionHand hand, InteractionResult original) {
		if (blockingTypeLocation.equals(ResourceLocation.withDefaultNamespace("empty"))) return null;
		if (original != InteractionResult.PASS) return null;
		if (!canUse(itemStack, level, user, hand)) return null;
		user.startUsingItem(hand);
		return InteractionResult.CONSUME;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean canUse(ItemStack itemStack, Level level, Player user, InteractionHand hand) {
		return blockingCondition.canUse(itemStack, level, user, hand);
	}

	public boolean canShowInTooltip(ItemStack itemStack, Player player) {
		return blockingCondition.canShowInToolTip(itemStack, player);
	}

	public boolean canOverrideUseDurationAndAnimation(ItemStack itemStack) {
		return blockingCondition.overridesUseDurationAndAnimation(itemStack);
	}
	public record Tooltip(List<CombinedModifier> protectionModifiers, List<ComponentModifier> knockbackModifiers, boolean markBlocked) {
		public static MapCodec<Tooltip> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(CombinedModifier.CODEC.listOf().fieldOf("protection_modifiers").forGetter(Tooltip::protectionModifiers),
				ComponentModifier.CODEC.listOf().optionalFieldOf("knockback_modifiers", Collections.emptyList()).forGetter(Tooltip::knockbackModifiers),
				Codec.BOOL.fieldOf("mark_blocked").forGetter(Tooltip::markBlocked))
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
