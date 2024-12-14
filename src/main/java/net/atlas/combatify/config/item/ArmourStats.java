package net.atlas.combatify.config.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.combatify.config.ArmourVariable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;
import java.util.Optional;

import static net.atlas.combatify.config.ConfigurableItemData.clamp;
import static net.atlas.combatify.config.ConfigurableItemData.max;

public record ArmourStats(ArmourVariable durability, ArmourVariable defense, Optional<Double> optionalToughness, Optional<Double> optionalArmourKbRes) {
	public static final ArmourStats EMPTY = new ArmourStats(ArmourVariable.EMPTY, ArmourVariable.EMPTY, (Double) null, null);
	public static final Codec<ArmourStats> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(ArmourVariable.CODEC.optionalFieldOf("durability", ArmourVariable.EMPTY).forGetter(ArmourStats::durability),
				ArmourVariable.CODEC.optionalFieldOf("armor", ArmourVariable.EMPTY).forGetter(ArmourStats::defense),
				Codec.DOUBLE.optionalFieldOf("armor_toughness").forGetter(ArmourStats::optionalToughness),
				Codec.DOUBLE.optionalFieldOf("armor_knockback_resistance").forGetter(ArmourStats::optionalArmourKbRes))
			.apply(instance, ArmourStats::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, ArmourStats> STREAM_CODEC = StreamCodec.of((buf, armourStats) -> {
		ArmourVariable.ARMOUR_VARIABLE_STREAM_CODEC.encode(buf, armourStats.durability);
		ArmourVariable.ARMOUR_VARIABLE_STREAM_CODEC.encode(buf, armourStats.defense);
		buf.writeDouble(armourStats.optionalToughness.orElse(-10D));
		buf.writeDouble(armourStats.optionalArmourKbRes.orElse(-10D));
	}, buf -> {
		ArmourVariable durability = ArmourVariable.ARMOUR_VARIABLE_STREAM_CODEC.decode(buf);
		ArmourVariable defense = ArmourVariable.ARMOUR_VARIABLE_STREAM_CODEC.decode(buf);
		Double toughness = buf.readDouble();
		Double armourKbRes = buf.readDouble();
		if (toughness == -10)
			toughness = null;
		if (armourKbRes == -10)
			armourKbRes = null;
		return new ArmourStats(durability, defense, toughness, armourKbRes);
	});
	public ArmourStats(ArmourVariable durability, ArmourVariable defense, Double optionalToughness, Double optionalArmourKbRes) {
		this(durability, defense, Optional.ofNullable(optionalToughness), Optional.ofNullable(optionalArmourKbRes));
	}
	public ArmourStats(ArmourVariable durability, ArmourVariable defense, Optional<Double> optionalToughness, Optional<Double> optionalArmourKbRes) {
		this.durability = durability;
		this.defense = defense;
		this.optionalToughness = max(optionalToughness, 0);
		this.optionalArmourKbRes = clamp(optionalArmourKbRes, 0, 1);
	}

	public Double toughness() {
		return optionalToughness.orElse(null);
	}

	public Double armourKbRes() {
		return optionalArmourKbRes.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ArmourStats that)) return false;
        return Objects.equals(durability, that.durability) && Objects.equals(defense, that.defense) && Objects.equals(optionalToughness, that.optionalToughness) && Objects.equals(optionalArmourKbRes, that.optionalArmourKbRes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(durability, defense, optionalToughness, optionalArmourKbRes);
	}
}
