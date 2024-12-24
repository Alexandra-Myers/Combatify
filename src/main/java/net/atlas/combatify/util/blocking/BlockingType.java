package net.atlas.combatify.util.blocking;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.combatify.Combatify;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
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
				Codec.BOOL.optionalFieldOf("can_crouch_block").forGetter(blockingType -> Optional.of(blockingType.canCrouchBlock)),
				Codec.BOOL.optionalFieldOf("can_block_hit").forGetter(blockingType -> Optional.of(blockingType.canBlockHit)),
				Codec.BOOL.optionalFieldOf("can_be_disabled").forGetter(blockingType -> Optional.of(blockingType.canBeDisabled)),
				Codec.BOOL.optionalFieldOf("require_full_charge").forGetter(blockingType -> Optional.of(blockingType.requireFullCharge)),
				Codec.BOOL.optionalFieldOf("default_kb_mechanics").forGetter(blockingType -> Optional.of(blockingType.defaultKbMechanics)),
				Codec.BOOL.optionalFieldOf("has_shield_delay").forGetter(blockingType -> Optional.of(blockingType.hasDelay)))
			.apply(instance, BlockingType::copy));
	public static final Codec<BlockingType> CREATE = RecordCodecBuilder.create(instance ->
		instance.group(FACTORY_CODEC.fieldOf("factory").forGetter(BlockingType::factory),
				ResourceLocation.CODEC.fieldOf("name").validate(blocking_type -> blocking_type.equals(id("empty")) || blocking_type.equals(id("blank")) ? DataResult.error(() -> "Unable to create a blank Blocking Type!") : DataResult.success(blocking_type)).forGetter(BlockingType::getName),
				Codec.BOOL.optionalFieldOf("can_crouch_block", true).forGetter(BlockingType::canCrouchBlock),
				Codec.BOOL.optionalFieldOf("can_block_hit", false).forGetter(BlockingType::canBlockHit),
				Codec.BOOL.optionalFieldOf("can_be_disabled", true).forGetter(BlockingType::canBeDisabled),
				Codec.BOOL.optionalFieldOf("require_full_charge", true).forGetter(BlockingType::requireFullCharge),
				Codec.BOOL.optionalFieldOf("default_kb_mechanics", true).forGetter(BlockingType::defaultKbMechanics),
				Codec.BOOL.optionalFieldOf("has_shield_delay", true).forGetter(BlockingType::hasDelay))
			.apply(instance, Factory::create));
	public static final Codec<BlockingType> CODEC = Codec.withAlternative(CREATE, MODIFY);
	public static final StreamCodec<RegistryFriendlyByteBuf, BlockingType> STREAM_CODEC = StreamCodec.of(RegistryFriendlyByteBuf::writeResourceLocation, RegistryFriendlyByteBuf::readResourceLocation).map(blocking_type -> Combatify.registeredTypes.get(blocking_type), BlockingType::getName);
	private final ResourceLocation name;
	private final boolean canBeDisabled;
	private final boolean canCrouchBlock;
	private final boolean canBlockHit;
	private final boolean requireFullCharge;
	private final boolean defaultKbMechanics;
	private final boolean hasDelay;
	public boolean canCrouchBlock() {
		return canCrouchBlock;
	}
	public boolean canBlockHit() {
		return canBlockHit;
	}
	public boolean isToolBlocker() {
		return false;
	}
	public boolean canBeDisabled() {
		return canBeDisabled;
	}
	public boolean requireFullCharge() {
		return requireFullCharge;
	}
	public boolean defaultKbMechanics() {
		return defaultKbMechanics;
	}

	public boolean hasDelay() {
		return hasDelay;
	}

	public BlockingType(ResourceLocation name, boolean crouchable, boolean blockHit, boolean canDisable, boolean needsFullCharge, boolean defaultKbMechanics, boolean hasDelay) {
		this.name = name;
		this.canCrouchBlock = crouchable;
		this.canBlockHit = blockHit;
		this.canBeDisabled = canDisable;
		this.requireFullCharge = needsFullCharge;
		this.defaultKbMechanics = defaultKbMechanics;
		this.hasDelay = hasDelay;
	}
	public BlockingType copy(Optional<Boolean> crouchable, Optional<Boolean> blockHit, Optional<Boolean> canDisable, Optional<Boolean> needsFullCharge, Optional<Boolean> defaultKbMechanics, Optional<Boolean> hasDelay) {
		return factory().create(name, crouchable.orElse(canCrouchBlock), blockHit.orElse(canBlockHit), canDisable.orElse(canBeDisabled), needsFullCharge.orElse(requireFullCharge), defaultKbMechanics.orElse(this.defaultKbMechanics), hasDelay.orElse(this.hasDelay));
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
		return canBeDisabled == that.canBeDisabled && canCrouchBlock == that.canCrouchBlock && isToolBlocker() == that.isToolBlocker() && canBlockHit == that.canBlockHit && requireFullCharge == that.requireFullCharge && defaultKbMechanics == that.defaultKbMechanics && Objects.equals(getName(), that.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName(), canBeDisabled, canCrouchBlock, isToolBlocker(), canBlockHit, requireFullCharge, defaultKbMechanics);
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
			return initialiser.create(name, canCrouchBlock, canBlockHit, canBeDisabled, requireFullCharge, defaultKbMechanics, hasDelay);
		}
	}
	@FunctionalInterface
	public interface Factory<B extends BlockingType> {
		B create(ResourceLocation name, boolean crouchable, boolean blockHit, boolean canDisable, boolean needsFullCharge, boolean defaultKbMechanics, boolean hasDelay);
	}
}
