package net.atlas.combatify.config.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.util.BlockingType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;
import java.util.Optional;

import static net.atlas.combatify.config.ConfigurableItemData.clamp;

public record Blocker(Optional<BlockingType> optionalBlockingType, Optional<Double> optionalBlockStrength, Optional<Double> optionalBlockKbRes, Optional<Float> optionalBlockingLevel) {
	public static final Blocker EMPTY = new Blocker((BlockingType) null, null, null, null);
	public static final Codec<Blocker> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(BlockingType.SIMPLE_CODEC.optionalFieldOf("blocking_type").forGetter(Blocker::optionalBlockingType),
				Codec.DOUBLE.optionalFieldOf("damage_protection").forGetter(Blocker::optionalBlockStrength),
				Codec.DOUBLE.optionalFieldOf("block_knockback_resistance").forGetter(Blocker::optionalBlockKbRes),
				Codec.FLOAT.optionalFieldOf("blocking_level").forGetter(Blocker::optionalBlockingLevel))
			.apply(instance, Blocker::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, Blocker> STREAM_CODEC = StreamCodec.of((buf, blocker) -> {
		buf.writeUtf(blocker.optionalBlockingType.isEmpty() ? "blank" : blocker.optionalBlockingType.get().getName());
		buf.writeDouble(blocker.optionalBlockStrength.orElse(-10D));
		buf.writeDouble(blocker.optionalBlockKbRes.orElse(-10D));
		buf.writeBoolean(blocker.optionalBlockingLevel.isPresent());
		blocker.optionalBlockingLevel.ifPresent(buf::writeFloat);
	}, buf -> {
		String blockingType = buf.readUtf();
		BlockingType bType = Combatify.registeredTypes.get(blockingType);
		Double blockStrength = buf.readDouble();
		Double blockKbRes = buf.readDouble();
		Float blockingLevel = null;
		if (buf.readBoolean())
			blockingLevel = buf.readFloat();
		if (blockStrength == -10)
			blockStrength = null;
		if (blockKbRes == -10)
			blockKbRes = null;
		return new Blocker(bType, blockStrength, blockKbRes, blockingLevel);
	});
	public Blocker(BlockingType optionalBlockingType, Double optionalBlockStrength, Double optionalBlockKbRes, Float optionalBlockingLevel) {
		this(Optional.ofNullable(optionalBlockingType), Optional.ofNullable(optionalBlockStrength), Optional.ofNullable(optionalBlockKbRes), Optional.ofNullable(optionalBlockingLevel));
	}
	public Blocker(Optional<BlockingType> optionalBlockingType, Optional<Double> optionalBlockStrength, Optional<Double> optionalBlockKbRes, Optional<Float> optionalBlockingLevel) {
		this.optionalBlockingType = optionalBlockingType;
		this.optionalBlockStrength = clamp(optionalBlockStrength, 0, 1000);
		this.optionalBlockKbRes = clamp(optionalBlockKbRes, 0, 1);
		this.optionalBlockingLevel = optionalBlockingLevel;
	}

	public BlockingType blockingType() {
		return optionalBlockingType.orElse(null);
	}

	public Double blockStrength() {
		return optionalBlockStrength.orElse(null);
	}

	public Double blockKbRes() {
		return optionalBlockKbRes.orElse(null);
	}

	public Float blockingLevel() {
		return optionalBlockingLevel.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Blocker blocker)) return false;
        return Objects.equals(optionalBlockingType, blocker.optionalBlockingType) && Objects.equals(optionalBlockStrength, blocker.optionalBlockStrength) && Objects.equals(optionalBlockKbRes, blocker.optionalBlockKbRes) && Objects.equals(optionalBlockingLevel, blocker.optionalBlockingLevel);
	}

	@Override
	public int hashCode() {
		return Objects.hash(optionalBlockingType, optionalBlockStrength, optionalBlockKbRes, optionalBlockingLevel);
	}
}
