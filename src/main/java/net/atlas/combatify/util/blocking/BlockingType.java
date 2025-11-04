package net.atlas.combatify.util.blocking;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.combatify.Combatify;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.*;

public record BlockingType(Identifier name, BlockingTypeData data) {
	public static final Codec<Identifier> ID_CODEC = Identifier.CODEC.validate(blocking_type -> blocking_type.equals(Identifier.parse("empty")) || !Combatify.registeredTypes.containsKey(blocking_type) ? DataResult.error(() -> "Attempted to retrieve a Blocking Type that does not exist: " + blocking_type) : DataResult.success(blocking_type));
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
		instance.group(Identifier.CODEC.fieldOf("name").validate(blocking_type -> blocking_type.equals(Identifier.parse("empty")) ? DataResult.error(() -> "Unable to create a blank Blocking Type!") : DataResult.success(blocking_type)).forGetter(BlockingType::name),
				BlockingTypeData.CREATE.forGetter(BlockingType::data))
			.apply(instance, BlockingType::new));
	public static final Codec<BlockingType> CODEC = Codec.withAlternative(CREATE, MODIFY);
	public static final StreamCodec<RegistryFriendlyByteBuf, BlockingType> FULL_STREAM_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC, BlockingType::name,
		BlockingTypeData.STREAM_CODEC, BlockingType::data,
		BlockingType::new);
	public static Builder builder() {
		return new Builder();
	}
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
		return new BlockingType(this.name, newData);
	}
	public boolean isEmpty() {
		return this.name.equals(Identifier.withDefaultNamespace("empty"));
	}

	public Identifier getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BlockingType that)) return false;
        return Objects.equals(name, that.name) && Objects.equals(data, that.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, data);
	}

	public static class Builder {
		private boolean canBeDisabled = true;
		private boolean canCrouchBlock = true;
		private boolean canBlockHit = false;
		private boolean requireFullCharge = true;
		private boolean defaultKbMechanics = true;
		private boolean hasDelay = true;
		public Builder setCrouchable(boolean crouchable) {
			canCrouchBlock = crouchable;
			return this;
		}
		public Builder setBlockHit(boolean blockHit) {
			canBlockHit = blockHit;
			return this;
		}
		public Builder setDisablement(boolean canDisable) {
			canBeDisabled = canDisable;
			return this;
		}
		public Builder setRequireFullCharge(boolean needsFullCharge) {
			requireFullCharge = needsFullCharge;
			return this;
		}
		public Builder setKbMechanics(boolean defaultKbMechanics) {
			this.defaultKbMechanics = defaultKbMechanics;
			return this;
		}
		public Builder setDelay(boolean hasDelay) {
			this.hasDelay = hasDelay;
			return this;
		}
		public BlockingType build(String name) {
			return build(Identifier.parse(name));
		}
		public BlockingType build(Identifier name) {
			return new BlockingType(name, new BlockingTypeData(canBeDisabled, canCrouchBlock, canBlockHit, requireFullCharge, defaultKbMechanics, hasDelay));
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
}
