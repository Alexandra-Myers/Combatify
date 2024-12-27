package net.atlas.combatify.config.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.combatify.component.custom.Blocker;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

import java.util.Objects;
import java.util.Optional;

import static net.atlas.combatify.config.ConfigurableItemData.clamp;

public record BlockingInformation(Optional<Blocker> optionalBlocking, Optional<Double> optionalBlockStrength, Optional<Double> optionalBlockKbRes, Optional<Integer> optionalBlockingLevel) {
	public static final BlockingInformation EMPTY = new BlockingInformation((Blocker) null, null, null, null);
	public static final Codec<BlockingInformation> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(Blocker.SIMPLE_CODEC.optionalFieldOf("blocking_type").forGetter(BlockingInformation::optionalBlocking),
				Codec.DOUBLE.optionalFieldOf("damage_protection").forGetter(BlockingInformation::optionalBlockStrength),
				Codec.DOUBLE.optionalFieldOf("block_knockback_resistance").forGetter(BlockingInformation::optionalBlockKbRes),
				ExtraCodecs.POSITIVE_INT.optionalFieldOf("blocking_level").forGetter(BlockingInformation::optionalBlockingLevel))
			.apply(instance, BlockingInformation::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, BlockingInformation> STREAM_CODEC = StreamCodec.of((buf, blocker) -> {
		buf.writeBoolean(blocker.optionalBlocking.isPresent());
		blocker.optionalBlocking.ifPresent(blocking -> Blocker.STREAM_CODEC.encode(buf, blocking));
		buf.writeDouble(blocker.optionalBlockStrength.orElse(-10D));
		buf.writeDouble(blocker.optionalBlockKbRes.orElse(-10D));
		buf.writeBoolean(blocker.optionalBlockingLevel.isPresent());
		blocker.optionalBlockingLevel.ifPresent(buf::writeVarInt);
	}, buf -> {
		Boolean blockingPresent = buf.readBoolean();
		Blocker blocking = blockingPresent ? Blocker.STREAM_CODEC.decode(buf) : null;
		Double blockStrength = buf.readDouble();
		Double blockKbRes = buf.readDouble();
		Integer blockingLevel = null;
		if (buf.readBoolean())
			blockingLevel = buf.readVarInt();
		if (blockStrength == -10)
			blockStrength = null;
		if (blockKbRes == -10)
			blockKbRes = null;
		return new BlockingInformation(blocking, blockStrength, blockKbRes, blockingLevel);
	});
	public BlockingInformation(Blocker optionalBlocking, Double optionalBlockStrength, Double optionalBlockKbRes, Integer optionalBlockingLevel) {
		this(Optional.ofNullable(optionalBlocking), Optional.ofNullable(optionalBlockStrength), Optional.ofNullable(optionalBlockKbRes), Optional.ofNullable(optionalBlockingLevel));
	}
	public BlockingInformation(Optional<Blocker> optionalBlocking, Optional<Double> optionalBlockStrength, Optional<Double> optionalBlockKbRes, Optional<Integer> optionalBlockingLevel) {
		this.optionalBlocking = optionalBlocking;
		this.optionalBlockStrength = clamp(optionalBlockStrength, 0, 1000);
		this.optionalBlockKbRes = clamp(optionalBlockKbRes, 0, 1);
		this.optionalBlockingLevel = optionalBlockingLevel;
	}

	public Blocker blocking() {
		return optionalBlocking.orElse(null);
	}

	public Double blockStrength() {
		return optionalBlockStrength.orElse(null);
	}

	public Double blockKbRes() {
		return optionalBlockKbRes.orElse(null);
	}

	public Integer blockingLevel() {
		return optionalBlockingLevel.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BlockingInformation blocker)) return false;
        return Objects.equals(optionalBlocking, blocker.optionalBlocking) && Objects.equals(optionalBlockStrength, blocker.optionalBlockStrength) && Objects.equals(optionalBlockKbRes, blocker.optionalBlockKbRes) && Objects.equals(optionalBlockingLevel, blocker.optionalBlockingLevel);
	}

	@Override
	public int hashCode() {
		return Objects.hash(optionalBlocking, optionalBlockStrength, optionalBlockKbRes, optionalBlockingLevel);
	}
}
