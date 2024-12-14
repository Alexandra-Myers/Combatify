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

public record Blocker(Optional<BlockingType> optionalBlockingType, Optional<Double> optionalBlockStrength, Optional<Double> optionalBlockKbRes) {
	public static final Blocker EMPTY = new Blocker((BlockingType) null, null, null);
	public static final Codec<Blocker> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(BlockingType.SIMPLE_CODEC.optionalFieldOf("blocking_type").forGetter(Blocker::optionalBlockingType),
				Codec.DOUBLE.optionalFieldOf("damage_protection").forGetter(Blocker::optionalBlockStrength),
				Codec.DOUBLE.optionalFieldOf("block_knockback_resistance").forGetter(Blocker::optionalBlockKbRes))
			.apply(instance, Blocker::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, Blocker> STREAM_CODEC = StreamCodec.of((buf, blocker) -> {
		buf.writeUtf(blocker.optionalBlockingType.isEmpty() ? "blank" : blocker.optionalBlockingType.get().getName());
		buf.writeDouble(blocker.optionalBlockStrength.orElse(-10D));
		buf.writeDouble(blocker.optionalBlockKbRes.orElse(-10D));
	}, buf -> {
		String blockingType = buf.readUtf();
		BlockingType bType = Combatify.registeredTypes.get(blockingType);
		Double blockStrength = buf.readDouble();
		Double blockKbRes = buf.readDouble();
		if (blockStrength == -10)
			blockStrength = null;
		if (blockKbRes == -10)
			blockKbRes = null;
		return new Blocker(bType, blockStrength, blockKbRes);
	});
	public Blocker(BlockingType optionalBlockingType, Double optionalBlockStrength, Double optionalBlockKbRes) {
		this(Optional.ofNullable(optionalBlockingType), Optional.ofNullable(optionalBlockStrength), Optional.ofNullable(optionalBlockKbRes));
	}
	public Blocker(Optional<BlockingType> optionalBlockingType, Optional<Double> optionalBlockStrength, Optional<Double> optionalBlockKbRes) {
		this.optionalBlockingType = optionalBlockingType;
		this.optionalBlockStrength = clamp(optionalBlockStrength, 0, 1000);
		this.optionalBlockKbRes = clamp(optionalBlockKbRes, 0, 1);
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Blocker blocker)) return false;
        return Objects.equals(optionalBlockingType, blocker.optionalBlockingType) && Objects.equals(optionalBlockStrength, blocker.optionalBlockStrength) && Objects.equals(optionalBlockKbRes, blocker.optionalBlockKbRes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(optionalBlockingType, optionalBlockStrength, optionalBlockKbRes);
	}
}
