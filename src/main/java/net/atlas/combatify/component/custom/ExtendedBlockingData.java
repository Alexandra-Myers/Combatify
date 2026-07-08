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
import net.atlas.combatify.util.blocking.*;
import net.atlas.combatify.util.blocking.ComponentModifier.CombinedModifier;
import net.atlas.combatify.util.blocking.condition.*;
import net.atlas.combatify.util.blocking.effect.PostBlockEffectWrapper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableFloat;

import static net.atlas.combatify.util.MethodHandler.arrowDisable;
import static net.atlas.combatify.util.MethodHandler.getBlockingType;

public record ExtendedBlockingData(BaseBlocksAttacks baseBlocksAttacks,
								   Tooltip tooltip,
								   ResourceLocation blockingTypeLocation,
								   PostBlockEffectWrapper postBlockEffect,
								   BlockingCondition blockingCondition,
								   List<DamageReduction> bannerReductions) {
	public ExtendedBlockingData(BaseBlocksAttacks baseBlocksAttacks, Tooltip tooltip, ResourceLocation blockingTypeLocation, PostBlockEffectWrapper postBlockEffect, BlockingCondition blockingCondition) {
		this(baseBlocksAttacks, tooltip, blockingTypeLocation, postBlockEffect, blockingCondition, Collections.emptyList());
	}
	public static final ExtendedBlockingData EMPTY = new ExtendedBlockingData(new BaseBlocksAttacks(Collections.emptyList(), 0, 1, ItemDamageFunction.DEFAULT), new Tooltip(Collections.emptyList(), Collections.emptyList()), ResourceLocation.withDefaultNamespace("empty"), PostBlockEffectWrapper.DEFAULT, new AnyOf(Collections.emptyList()));
	public static final ExtendedBlockingData VANILLA_SHIELD = new ExtendedBlockingData(new BaseBlocksAttacks(Collections.singletonList(DamageReduction.DEFAULT), 3600, 1, new ItemDamageFunction(3, 0, 1)), new Tooltip(Collections.emptyList(), Collections.emptyList()), ResourceLocation.withDefaultNamespace("shield"), PostBlockEffectWrapper.KNOCKBACK, Unconditional.INSTANCE);
	public static final ExtendedBlockingData NEW_SHIELD = new ExtendedBlockingData(new BaseBlocksAttacks(Collections.emptyList(), 3600, 1, ItemDamageFunction.DEFAULT), new Tooltip(Collections.singletonList(BlockingTypeInit.NEW_SHIELD_PROTECTION), Collections.singletonList(BlockingTypeInit.NEW_SHIELD_KNOCKBACK)), ResourceLocation.withDefaultNamespace("new_shield"), PostBlockEffectWrapper.KNOCKBACK, Unconditional.INSTANCE);
	public static final Codec<ExtendedBlockingData> CODEC = RecordCodecBuilder.create(instance ->
	instance.group(BaseBlocksAttacks.CODEC.forGetter(ExtendedBlockingData::baseBlocksAttacks),
			Tooltip.CODEC.forGetter(ExtendedBlockingData::tooltip),
			BlockingType.ID_CODEC.fieldOf("type").forGetter(ExtendedBlockingData::blockingTypeLocation),
			PostBlockEffectWrapper.CODEC.orElse(PostBlockEffectWrapper.KNOCKBACK).forGetter(ExtendedBlockingData::postBlockEffect),
			BlockingConditions.MAP_CODEC.orElse(Unconditional.INSTANCE).forGetter(ExtendedBlockingData::blockingCondition),
			DamageReduction.CODEC.listOf().optionalFieldOf("banner_damage_reductions", Collections.emptyList()).forGetter(ExtendedBlockingData::bannerReductions))
		.apply(instance, ExtendedBlockingData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ExtendedBlockingData> STREAM_CODEC = StreamCodec.composite(
		BaseBlocksAttacks.STREAM_CODEC,
		ExtendedBlockingData::baseBlocksAttacks,
		ByteBufCodecs.fromCodecTrusted(Tooltip.CODEC.codec()),
		ExtendedBlockingData::tooltip,
		ResourceLocation.STREAM_CODEC,
		ExtendedBlockingData::blockingTypeLocation,
		ByteBufCodecs.fromCodecWithRegistriesTrusted(PostBlockEffectWrapper.CODEC.codec()),
		ExtendedBlockingData::postBlockEffect,
		BlockingCondition.STREAM_CODEC,
		ExtendedBlockingData::blockingCondition,
		DamageReduction.STREAM_CODEC.apply(ByteBufCodecs.list()),
		ExtendedBlockingData::bannerReductions,
		ExtendedBlockingData::new
	);

	public ExtendedBlockingData withProtection(List<CombinedModifier> protection) {
		return new ExtendedBlockingData(baseBlocksAttacks, new Tooltip(protection, tooltip.knockbackModifiers), blockingTypeLocation, postBlockEffect, blockingCondition, bannerReductions);
	}

	public ExtendedBlockingData withKnockback(List<ComponentModifier> knockback) {
		return new ExtendedBlockingData(baseBlocksAttacks, new Tooltip(tooltip.protectionModifiers, knockback), blockingTypeLocation, postBlockEffect, blockingCondition, bannerReductions);
	}

	public ExtendedBlockingData withDisableCooldownScale(float disableCooldownScale) {
		return new ExtendedBlockingData(new BaseBlocksAttacks(baseBlocksAttacks.damageReductions(), baseBlocksAttacks.useSeconds(), disableCooldownScale, baseBlocksAttacks.itemDamage()), tooltip, blockingTypeLocation, postBlockEffect, blockingCondition, bannerReductions);
	}

	public ExtendedBlockingData withItemDamageFunction(ItemDamageFunction itemDamage) {
		return new ExtendedBlockingData(new BaseBlocksAttacks(baseBlocksAttacks.damageReductions(), baseBlocksAttacks.useSeconds(), baseBlocksAttacks.disableCooldownScale(), itemDamage), tooltip, blockingTypeLocation, postBlockEffect, blockingCondition, bannerReductions);
	}

	public ExtendedBlockingData withDamageReductions(List<DamageReduction> reductions) {
		return new ExtendedBlockingData(new BaseBlocksAttacks(reductions, baseBlocksAttacks.useSeconds(), baseBlocksAttacks.disableCooldownScale(), baseBlocksAttacks.itemDamage()), tooltip, blockingTypeLocation, postBlockEffect, blockingCondition, bannerReductions);
	}

	public ExtendedBlockingData withBlocksAttacks(Float blockDelaySeconds, Float disableCooldownScale, List<DamageReduction> damageReductions, ItemDamageFunction itemDamage) {
		return new ExtendedBlockingData(new BaseBlocksAttacks(damageReductions, blockDelaySeconds, disableCooldownScale, itemDamage), tooltip, blockingTypeLocation, postBlockEffect, blockingCondition, bannerReductions);
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
		MethodHandler.disableShield(attacker, target, damageSource, blockingItem, baseBlocksAttacks.disableCooldownScale());
	}

	public int useTicks() {
		return (int) (baseBlocksAttacks.useSeconds() * 20.0F);
	}

	public void block(ServerLevel serverLevel, LivingEntity instance, DamageSource source, ItemStack itemStack, LocalFloatRef amount, LocalFloatRef protectedDamage, LocalBooleanRef blocked) {
		if (blockingCondition.canBlock(serverLevel, instance, itemStack, source, amount.get())) {
			if (getBlockingType(itemStack).hasDelay() && Combatify.CONFIG.shieldDelay() > 0 && itemStack.getUseDuration(instance) - instance.getUseItemRemainingTicks() < Combatify.CONFIG.shieldDelay()) {
				if (Combatify.CONFIG.disableDuringShieldDelay())
					if (source.getDirectEntity() instanceof LivingEntity attacker)
						MethodHandler.disableShield(attacker, instance, source, itemStack, baseBlocksAttacks.disableCooldownScale());
				return;
			}
			completeBlock(serverLevel, instance, itemStack, source, amount, protectedDamage, blocked);
		}
	}

	public void completeBlock(ServerLevel serverLevel, LivingEntity instance, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef protectedDamage, LocalBooleanRef wasBlocked) {
		double angle;
		Vec3 sourcePosition = source.getSourcePosition();
		if (sourcePosition != null) {
			Vec3 viewVector = instance.calculateViewVector(0.0F, instance.getYHeadRot());
			Vec3 dirToAttacked = sourcePosition.vectorTo(instance.position());
			dirToAttacked = new Vec3(dirToAttacked.x, 0.0F, dirToAttacked.z).normalize();
			angle = Math.acos(dirToAttacked.dot(viewVector));
		} else angle = 0;
		float oldAmount = amount.get();
		baseBlocksAttacks().damageReductions().forEach(damageReduction ->
			amount.set(Math.max(amount.get() - damageReduction.resolve(source, amount.get(), angle), 0)));
		BannerPatternLayers bannerPatternLayers = blockingItem.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
		DyeColor dyeColor = blockingItem.get(DataComponents.BASE_COLOR);
		if (!bannerReductions.isEmpty() && (!bannerPatternLayers.layers().isEmpty() || dyeColor != null))
			bannerReductions.forEach(damageReduction ->
				amount.set(Math.max(amount.get() - damageReduction.resolve(source, amount.get(), angle), 0)));
		MethodHandler.hurtCurrentlyUsedShield(instance, oldAmount - amount.get(), baseBlocksAttacks().itemDamage());
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
		protectedDamage.set(oldAmount - amount.get());
		wasBlocked.set(amount.get() <= 0);
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
