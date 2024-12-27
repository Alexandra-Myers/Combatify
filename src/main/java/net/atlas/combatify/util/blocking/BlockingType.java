package net.atlas.combatify.util.blocking;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.component.CustomDataComponents;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.atlas.combatify.networking.NetworkingHandler;
import net.atlas.combatify.util.MethodHandler;
import net.atlas.combatify.util.blocking.damage_parsers.DamageParser;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.commons.lang3.mutable.MutableFloat;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.atlas.combatify.util.MethodHandler.arrowDisable;

public record BlockingType(ResourceLocation name, ResourceLocation factoryId, BlockingTypeHandler handler, BlockingTypeData data) {
	public static final Codec<BlockingType.Factory> FACTORY_CODEC = ResourceLocation.CODEC.validate(factory -> !Combatify.registeredTypeFactories.containsKey(factory) ? DataResult.error(() -> "Attempted to retrieve a Blocking Type Factory that does not exist: " + factory) : DataResult.success(factory)).xmap(factory -> Combatify.registeredTypeFactories.get(factory), factory -> Combatify.registeredTypeFactories.inverse().get(factory));
	public static final Codec<ResourceLocation> ID_CODEC = ResourceLocation.CODEC.validate(blocking_type -> blocking_type.equals(ResourceLocation.parse("empty")) || !Combatify.registeredTypes.containsKey(blocking_type) ? DataResult.error(() -> "Attempted to retrieve a Blocking Type that does not exist: " + blocking_type) : DataResult.success(blocking_type));
	public static final Codec<BlockingType> SIMPLE_CODEC = ID_CODEC.xmap(blocking_type -> Combatify.registeredTypes.get(blocking_type), BlockingType::name);
	public static final Codec<BlockingType> MODIFY = RecordCodecBuilder.create(instance ->
		instance.group(SIMPLE_CODEC.fieldOf("name").forGetter(blockingType -> blockingType),
				Codec.BOOL.optionalFieldOf("can_be_disabled").forGetter(blockingType -> Optional.of(blockingType.canBeDisabled())),
				Codec.BOOL.optionalFieldOf("can_crouch_block").forGetter(blockingType -> Optional.of(blockingType.canCrouchBlock())),
				Codec.BOOL.optionalFieldOf("can_block_hit").forGetter(blockingType -> Optional.of(blockingType.canBlockHit())),
				Codec.BOOL.optionalFieldOf("require_full_charge").forGetter(blockingType -> Optional.of(blockingType.requireFullCharge())),
				Codec.BOOL.optionalFieldOf("default_kb_mechanics").forGetter(blockingType -> Optional.of(blockingType.defaultKbMechanics())),
				Codec.BOOL.optionalFieldOf("has_shield_delay").forGetter(blockingType -> Optional.of(blockingType.hasDelay())))
			.apply(instance, (blockingType, canBeDisabled, canCrouchBlock, canBlockHit, requireFullCharge, defaultKbMechanics, hasDelay) -> blockingType.copy(canBeDisabled.orElse(null), canCrouchBlock.orElse(null), canBlockHit.orElse(null), requireFullCharge.orElse(null), defaultKbMechanics.orElse(null), hasDelay.orElse(null))));
	public static final Codec<BlockingType> CREATE = RecordCodecBuilder.create(instance ->
		instance.group(FACTORY_CODEC.fieldOf("factory").forGetter(BlockingType::factory),
				ResourceLocation.CODEC.fieldOf("name").validate(blocking_type -> blocking_type.equals(ResourceLocation.parse("empty")) ? DataResult.error(() -> "Unable to create a blank Blocking Type!") : DataResult.success(blocking_type)).forGetter(BlockingType::name),
				BlockingTypeData.CREATE.forGetter(BlockingType::data))
			.apply(instance, Factory::create));
	public static final Codec<BlockingType> CODEC = Codec.withAlternative(CREATE, MODIFY);
	public static final StreamCodec<RegistryFriendlyByteBuf, BlockingType> FULL_STREAM_CODEC = StreamCodec.composite(Factory.STREAM_CODEC, BlockingType::factory,
		ResourceLocation.STREAM_CODEC, BlockingType::name,
		BlockingTypeData.STREAM_CODEC, BlockingType::data,
		Factory::create);
	public BlockingTypeData data() {
		return data;
	}
	public boolean canCrouchBlock() {
		return data.canCrouchBlock;
	}
	public boolean canBlockHit() {
		return data.canBlockHit;
	}
	public boolean canBeDisabled() {
		return data.canBeDisabled;
	}
	public boolean requireFullCharge() {
		return data.requireFullCharge;
	}
	public boolean defaultKbMechanics() {
		return data.defaultKbMechanics;
	}

	public boolean hasDelay() {
		return data.hasDelay;
	}

	public BlockingType copy(Boolean canBeDisabled, Boolean canCrouchBlock, Boolean canBlockHit, Boolean requireFullCharge, Boolean defaultKbMechanics, Boolean hasDelay) {
		BlockingTypeData newData = this.data.copy(canBeDisabled, canCrouchBlock, canBlockHit, requireFullCharge, defaultKbMechanics, hasDelay);
		return factory().create(this.name, newData);
	}
	public Factory factory() {
		return Combatify.registeredTypeFactories.get(factoryId);
	}
	public static <B extends BlockingType> Builder<B> builder(Factory initialiser) {
		return new Builder<>(initialiser);
	}
	public boolean isEmpty() {
		return this.name.equals(ResourceLocation.withDefaultNamespace("empty"));
	}

	public ResourceLocation getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BlockingType that)) return false;
        return Objects.equals(name, that.name) && Objects.equals(handler, that.handler) && Objects.equals(data, that.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, handler, data);
	}

	public void block(ServerLevel serverLevel, LivingEntity instance, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef protectedDamage, LocalBooleanRef blocked) {
		handler.block(serverLevel, instance, blockingItem, source, amount, protectedDamage, blocked);
	}

	public static class Builder<B extends BlockingType> {
		public final Factory initialiser;
		public Builder(Factory initialiser) {
			this.initialiser = initialiser;
		}
		private boolean canBeDisabled = true;
		private boolean canCrouchBlock = true;
		private boolean canBlockHit = false;
		private boolean requireFullCharge = true;
		private boolean defaultKbMechanics = true;
		private boolean hasDelay = true;
		public Builder<B> setCrouchable(boolean crouchable) {
			canCrouchBlock = crouchable;
			return this;
		}
		public Builder<B> setBlockHit(boolean blockHit) {
			canBlockHit = blockHit;
			return this;
		}
		public Builder<B> setDisablement(boolean canDisable) {
			canBeDisabled = canDisable;
			return this;
		}
		public Builder<B> setRequireFullCharge(boolean needsFullCharge) {
			requireFullCharge = needsFullCharge;
			return this;
		}
		public Builder<B> setKbMechanics(boolean defaultKbMechanics) {
			this.defaultKbMechanics = defaultKbMechanics;
			return this;
		}
		public Builder<B> setDelay(boolean hasDelay) {
			this.hasDelay = hasDelay;
			return this;
		}
		public BlockingType build(String name) {
			return build(ResourceLocation.parse(name));
		}
		public BlockingType build(ResourceLocation name) {
			return initialiser.create(name, new BlockingTypeData(canBeDisabled, canCrouchBlock, canBlockHit, requireFullCharge, defaultKbMechanics, hasDelay));
		}
	}
	@FunctionalInterface
	public interface Factory {
		StreamCodec<ByteBuf, Factory> STREAM_CODEC = ResourceLocation.STREAM_CODEC.map(Combatify.registeredTypeFactories::get, Combatify.registeredTypeFactories.inverse()::get);
		BlockingType create(ResourceLocation name, BlockingTypeData blockingTypeData);
		static Factory forHandler(BlockingTypeHandler handler, ResourceLocation id) {
			return (name, data) -> new BlockingType(name, id, handler, data);
		}
	}
	public record BlockingTypeData(boolean canBeDisabled, boolean canCrouchBlock, boolean canBlockHit,
								   boolean requireFullCharge, boolean defaultKbMechanics, boolean hasDelay) {
		public static final StreamCodec<FriendlyByteBuf, BlockingTypeData> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, BlockingTypeData::canBeDisabled,
			ByteBufCodecs.BOOL, BlockingTypeData::canCrouchBlock,
			ByteBufCodecs.BOOL, BlockingTypeData::canBlockHit,
			ByteBufCodecs.BOOL, BlockingTypeData::requireFullCharge,
			ByteBufCodecs.BOOL, BlockingTypeData::defaultKbMechanics,
			ByteBufCodecs.BOOL, BlockingTypeData::hasDelay,
			BlockingTypeData::new);
		public static final MapCodec<BlockingTypeData> CREATE = RecordCodecBuilder.mapCodec(instance ->
			instance.group(Codec.BOOL.optionalFieldOf("can_be_disabled", true).forGetter(BlockingTypeData::canBeDisabled),
				Codec.BOOL.optionalFieldOf("can_crouch_block", true).forGetter(BlockingTypeData::canCrouchBlock),
				Codec.BOOL.optionalFieldOf("can_block_hit", false).forGetter(BlockingTypeData::canBlockHit),
				Codec.BOOL.optionalFieldOf("require_full_charge", true).forGetter(BlockingTypeData::requireFullCharge),
				Codec.BOOL.optionalFieldOf("default_kb_mechanics", true).forGetter(BlockingTypeData::defaultKbMechanics),
				Codec.BOOL.optionalFieldOf("has_shield_delay", true).forGetter(BlockingTypeData::hasDelay)).apply(instance, BlockingTypeData::new));
		public BlockingTypeData copy(Boolean canBeDisabled, Boolean canCrouchBlock, Boolean canBlockHit, Boolean requireFullCharge, Boolean defaultKbMechanics, Boolean hasDelay) {
			return new BlockingTypeData(canBeDisabled == null ? this.canBeDisabled : canBeDisabled, canCrouchBlock == null ? this.canCrouchBlock : canCrouchBlock, canBlockHit == null ? this.canBlockHit : canBlockHit, requireFullCharge == null ? this.requireFullCharge : requireFullCharge, defaultKbMechanics == null ? this.defaultKbMechanics : defaultKbMechanics, hasDelay == null ? this.hasDelay : hasDelay);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof BlockingTypeData that)) return false;
            return canBeDisabled == that.canBeDisabled && canCrouchBlock == that.canCrouchBlock && canBlockHit == that.canBlockHit && requireFullCharge == that.requireFullCharge && defaultKbMechanics == that.defaultKbMechanics && hasDelay == that.hasDelay;
		}

		@Override
		public int hashCode() {
			return Objects.hash(canBeDisabled, canCrouchBlock, canBlockHit, requireFullCharge, defaultKbMechanics, hasDelay);
		}
	}
	public record BlockingTypeHandler(List<ConditionalEffect<DamageParser>> damageParsers, Optional<LootItemCondition> triggerPostBlockEffects, List<ConditionalEffect<ComponentModifier>> protectionModifiers, List<ComponentModifier> knockbackModifiers, boolean markBlocked) {
		public static final MapCodec<BlockingTypeHandler> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(ConditionalEffect.codec(DamageParser.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf().fieldOf("damage_parsers").forGetter(BlockingTypeHandler::damageParsers),
					ConditionalEffect.conditionCodec(LootContextParamSets.ENCHANTED_DAMAGE).optionalFieldOf("trigger_post_block_effects").forGetter(BlockingTypeHandler::triggerPostBlockEffects),
					ConditionalEffect.codec(ComponentModifier.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf().fieldOf("protection_modifiers").forGetter(BlockingTypeHandler::protectionModifiers),
					ComponentModifier.CODEC.listOf().optionalFieldOf("knockback_modifiers", Collections.emptyList()).forGetter(BlockingTypeHandler::knockbackModifiers),
					Codec.BOOL.optionalFieldOf("mark_blocked", true).forGetter(BlockingTypeHandler::markBlocked))
				.apply(instance, BlockingTypeHandler::new));
		public void block(ServerLevel serverLevel, LivingEntity instance, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef protectedDamage, LocalBooleanRef wasBlocked) {
			int blockingLevel = blockingItem.getOrDefault(CustomDataComponents.BLOCKING_LEVEL, 1);
			LootContext context = Enchantment.damageContext(serverLevel, blockingLevel, instance, source);
			MutableFloat protection = new MutableFloat(0);
			matchingConditionalEffects(protectionModifiers, context).forEach(componentModifier -> protection.setValue(componentModifier.modifyValue(protection.getValue(), blockingLevel, instance.getRandom())));
			ConfigurableItemData configurableItemData = MethodHandler.forItem(blockingItem.getItem());
			if (configurableItemData != null) {
				if (configurableItemData.blocker().blockStrength() != null) protection.setValue(configurableItemData.blocker().blockStrength().floatValue());
			}
			protection.setValue(CustomEnchantmentHelper.modifyShieldEffectiveness(blockingItem, instance.getRandom(), protection.getValue()));
			List<DamageParser> damageParserList = matchingConditionalEffects(damageParsers, context);
			if (!damageParserList.isEmpty()) {
				protectedDamage.set(amount.get());
				damageParserList.forEach(damageParserConditionalEffect -> protectedDamage.set(damageParserConditionalEffect.parse(protectedDamage.get(), protection.getValue())));
			} else protectedDamage.set(0);
			amount.set(Math.max(amount.get() - protectedDamage.get(), 0));
			MethodHandler.hurtCurrentlyUsedShield(instance, protectedDamage.get());
			AtomicBoolean canTriggerPostBlockEffects = new AtomicBoolean(true);
			triggerPostBlockEffects.ifPresent(lootItemCondition -> canTriggerPostBlockEffects.set(lootItemCondition.test(context)));
			if (source.getDirectEntity() instanceof LivingEntity livingEntity && canTriggerPostBlockEffects.get())
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
			wasBlocked.set(markBlocked);
		}
		public static <T> List<T> matchingConditionalEffects(List<ConditionalEffect<T>> effects, LootContext lootContext) {
			return effects.stream().filter(componentModifierConditionalEffect -> componentModifierConditionalEffect.matches(lootContext)).map(ConditionalEffect::effect).toList();
		}
		public void updateServerTooltipInfo(Player player, ItemStack stack, int slot) {
			List<Component> protection = Collections.emptyList();
			List<Component> knockback = Collections.emptyList();
			int blockingLevel = stack.getOrDefault(CustomDataComponents.BLOCKING_LEVEL, 1);
			if (player instanceof ServerPlayer serverPlayer) {
				LootParams lootParams = new LootParams.Builder(serverPlayer.serverLevel())
					.withParameter(LootContextParams.TOOL, stack)
					.withParameter(LootContextParams.ENCHANTMENT_LEVEL, blockingLevel)
					.create(LootContextParamSets.ENCHANTED_ITEM);
				LootContext lootContext = new LootContext.Builder(lootParams).create(Optional.empty());
				List<ComponentModifier> intermediaryProtection = protectionModifiers.stream().map(ConditionalEffect::effect).filter(componentModifier -> componentModifier.matches(lootContext)).toList();
				if (!intermediaryProtection.isEmpty()) protection = intermediaryProtection.getFirst().tryCombine(new ArrayList<>(intermediaryProtection), blockingLevel, player.getRandom());
				List<ComponentModifier> intermediaryKnockback = knockbackModifiers.stream().filter(componentModifier -> componentModifier.matches(lootContext)).toList();
				if (!intermediaryKnockback.isEmpty()) knockback = intermediaryKnockback.getFirst().tryCombine(new ArrayList<>(intermediaryKnockback), blockingLevel, player.getRandom());
				ConfigurableItemData configurableItemData = MethodHandler.forItem(stack.getItem());
				if (configurableItemData != null) {
					if (configurableItemData.blocker().blockStrength() != null)
						protection = List.of(ComponentModifier.buildComponent(protectionModifiers.stream().map(ConditionalEffect::effect).toList().getFirst().tooltipComponent(), configurableItemData.blocker().blockStrength().floatValue()));
					if (configurableItemData.blocker().blockKbRes() != null)
						knockback = List.of(CommonComponents.space().append(
							Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_VALUE.id(),
								ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(configurableItemData.blocker().blockKbRes() * 10.0),
								Component.translatable("attribute.name.knockback_resistance"))).withStyle(ChatFormatting.DARK_GREEN));
				}
				if (ServerPlayNetworking.canSend(serverPlayer, NetworkingHandler.ClientboundTooltipUpdatePacket.TYPE)) {
					ServerPlayNetworking.send(serverPlayer, new NetworkingHandler.ClientboundTooltipUpdatePacket(protection, NetworkingHandler.ClientboundTooltipUpdatePacket.DataType.PROTECTION, slot));
					ServerPlayNetworking.send(serverPlayer, new NetworkingHandler.ClientboundTooltipUpdatePacket(knockback, NetworkingHandler.ClientboundTooltipUpdatePacket.DataType.KNOCKBACK, slot));
                }
			}
		}
		public float getShieldKnockbackResistanceValue(ServerLevel serverLevel, ItemStack itemStack, RandomSource randomSource) {
			ConfigurableItemData configurableItemData = MethodHandler.forItem(itemStack.getItem());
			if (configurableItemData != null) {
				if (configurableItemData.blocker().blockKbRes() != null)
					return configurableItemData.blocker().blockKbRes().floatValue();
			}
			int blockingLevel = itemStack.getOrDefault(CustomDataComponents.BLOCKING_LEVEL, 1);
			LootParams lootParams = new LootParams.Builder(serverLevel)
				.withParameter(LootContextParams.TOOL, itemStack)
				.withParameter(LootContextParams.ENCHANTMENT_LEVEL, blockingLevel)
				.create(LootContextParamSets.ENCHANTED_ITEM);
			LootContext lootContext = new LootContext.Builder(lootParams).create(Optional.empty());
			MutableFloat knockbackResistance = new MutableFloat(0);
			knockbackModifiers.stream().filter(componentModifier -> componentModifier.matches(lootContext)).forEach(componentModifier -> knockbackResistance.setValue(componentModifier.modifyValue(knockbackResistance.getValue(), blockingLevel, randomSource)));
			return knockbackResistance.getValue();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof BlockingTypeHandler that)) return false;
            return markBlocked == that.markBlocked && Objects.equals(damageParsers, that.damageParsers) && Objects.equals(triggerPostBlockEffects, that.triggerPostBlockEffects) && Objects.equals(protectionModifiers, that.protectionModifiers) && Objects.equals(knockbackModifiers, that.knockbackModifiers);
		}

		@Override
		public int hashCode() {
			return Objects.hash(damageParsers, triggerPostBlockEffects, protectionModifiers, knockbackModifiers, markBlocked);
		}
	}
}
