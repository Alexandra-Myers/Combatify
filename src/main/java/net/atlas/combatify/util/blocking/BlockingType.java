package net.atlas.combatify.util.blocking;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.atlas.combatify.Combatify;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static net.atlas.combatify.Combatify.EMPTY;
import static net.atlas.combatify.Combatify.id;

public abstract class BlockingType {
	public static final Codec<BlockingType.Factory<?>> FACTORY_CODEC = ResourceLocation.CODEC.validate(factory -> !Combatify.registeredTypeFactories.containsKey(factory) ? DataResult.error(() -> "Attempted to retrieve a Blocking Type Factory that does not exist: " + factory) : DataResult.success(factory)).xmap(factory -> Combatify.registeredTypeFactories.get(factory), factory -> Combatify.registeredTypeFactories.inverse().get(factory));
	public static final Codec<BlockingType> SIMPLE_CODEC = ResourceLocation.CODEC.validate(blocking_type -> blocking_type.equals(id("empty")) || blocking_type.equals(id("blank")) || !Combatify.registeredTypes.containsKey(blocking_type) ? DataResult.error(() -> "Attempted to retrieve a Blocking Type that does not exist: " + blocking_type) : DataResult.success(blocking_type)).xmap(blocking_type -> Combatify.registeredTypes.get(blocking_type), BlockingType::getName);
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
				ResourceLocation.CODEC.fieldOf("name").validate(blocking_type -> blocking_type.equals(id("empty")) || blocking_type.equals(id("blank")) ? DataResult.error(() -> "Unable to create a blank Blocking Type!") : DataResult.success(blocking_type)).forGetter(BlockingType::getName),
				BlockingTypeData.CREATE.forGetter(BlockingType::data))
			.apply(instance, Factory::create));
	public static final Codec<BlockingType> CODEC = Codec.withAlternative(CREATE, MODIFY);
	public static final StreamCodec<RegistryFriendlyByteBuf, BlockingType> IDENTITY_STREAM_CODEC = StreamCodec.of(RegistryFriendlyByteBuf::writeResourceLocation, RegistryFriendlyByteBuf::readResourceLocation).map(blocking_type -> Combatify.registeredTypes.get(blocking_type), BlockingType::getName);
	public static final StreamCodec<RegistryFriendlyByteBuf, BlockingType> FULL_STREAM_CODEC = StreamCodec.composite(Factory.STREAM_CODEC, BlockingType::factory,
		ResourceLocation.STREAM_CODEC, BlockingType::getName,
		BlockingTypeData.STREAM_CODEC, BlockingType::data,
		Factory::create);
	private final ResourceLocation name;
	private BlockingTypeData data;
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

	public BlockingType(ResourceLocation name, BlockingTypeData data) {
		this.name = name;
		this.data = data;
	}
	public BlockingType copy(Boolean canBeDisabled, Boolean canCrouchBlock, Boolean canBlockHit, Boolean requireFullCharge, Boolean defaultKbMechanics, Boolean hasDelay) {
		BlockingTypeData newData = this.data.copy(canBeDisabled, canCrouchBlock, canBlockHit, requireFullCharge, defaultKbMechanics, hasDelay);
		if (Combatify.defaultTypes.containsKey(this.name)) return factory().create(this.name, newData);
		this.data = newData;
		return this;
	}
	public abstract Factory<? extends BlockingType> factory();
	public static <B extends BlockingType> Builder<B> builder(Factory<B> initialiser) {
		return new Builder<>(initialiser);
	}
	public boolean isEmpty() {
		return this == EMPTY;
	}

	public ResourceLocation getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BlockingType that)) return false;
        return Objects.equals(getName(), that.getName()) && Objects.equals(data, that.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName(), data);
	}

	public abstract void block(ServerLevel serverLevel, LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl);
	public abstract float getShieldBlockDamageValue(ItemStack stack, RandomSource random);
	public abstract double getShieldKnockbackResistanceValue(ItemStack stack);
	public @NotNull InteractionResult use(ItemStack itemStack, Level world, Player player, InteractionHand interactionHand) {
		player.startUsingItem(interactionHand);
		return InteractionResult.CONSUME;
	}

	public void appendTooltipInfo(Consumer<Component> consumer, Player player, ItemStack stack) {
		consumer.accept(CommonComponents.EMPTY);
		consumer.accept(Component.translatable("item.modifiers.use").withStyle(ChatFormatting.GRAY));
		float f = getShieldBlockDamageValue(stack, player.getRandom());
		double g = getShieldKnockbackResistanceValue(stack);
		consumer.accept(CommonComponents.space().append(
			Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_VALUE.id(),
				ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(f),
				getStrengthTranslationKey())).withStyle(ChatFormatting.DARK_GREEN));
		if (g > 0.0)
			consumer.accept(CommonComponents.space().append(
				Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_VALUE.id(),
					ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(g * 10.0),
					Component.translatable("attribute.name.knockback_resistance"))).withStyle(ChatFormatting.DARK_GREEN));
	}
	public Component getStrengthTranslationKey() {
		return Component.translatable("attribute.name.shield_strength");
	}
	public static class Builder<B extends BlockingType> {
		public final Factory<B> initialiser;
		public Builder(Factory<B> initialiser) {
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
			return initialiser.create(name, new BlockingTypeData(canCrouchBlock, canBlockHit, canBeDisabled, requireFullCharge, defaultKbMechanics, hasDelay));
		}
	}
	@FunctionalInterface
	public interface Factory<B extends BlockingType> {
		StreamCodec<ByteBuf, Factory<? extends BlockingType>> STREAM_CODEC = ResourceLocation.STREAM_CODEC.map(Combatify.registeredTypeFactories::get, Combatify.registeredTypeFactories.inverse()::get);
		B create(ResourceLocation name, BlockingTypeData blockingTypeData);
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
}
