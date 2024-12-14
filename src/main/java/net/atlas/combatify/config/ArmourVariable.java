package net.atlas.combatify.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

import java.util.Objects;
import java.util.Optional;

public record ArmourVariable(Optional<Integer> any, Optional<Integer> helmet, Optional<Integer> chestplate, Optional<Integer> leggings, Optional<Integer> boots, Optional<Integer> body) {
	public static final Codec<ArmourVariable> SIMPLE_CODEC = Codec.INT.xmap(ArmourVariable::create, armourVariable -> armourVariable.any().orElse(null));
	public static final Codec<ArmourVariable> FULL_CODEC = RecordCodecBuilder.create(instance ->
		instance.group(Codec.INT.optionalFieldOf("any").forGetter(ArmourVariable::any),
			Codec.INT.optionalFieldOf("helmet").forGetter(ArmourVariable::helmet),
			Codec.INT.optionalFieldOf("chestplate").forGetter(ArmourVariable::chestplate),
			Codec.INT.optionalFieldOf("leggings").forGetter(ArmourVariable::leggings),
			Codec.INT.optionalFieldOf("boots").forGetter(ArmourVariable::boots),
			Codec.INT.optionalFieldOf("body").forGetter(ArmourVariable::body)).apply(instance, ArmourVariable::new));
	public static final Codec<ArmourVariable> CODEC = Codec.withAlternative(FULL_CODEC, SIMPLE_CODEC);
	public static final ArmourVariable EMPTY = new ArmourVariable((Integer) null, null, null, null, null, null);
	public static final StreamCodec<RegistryFriendlyByteBuf, ArmourVariable> ARMOUR_VARIABLE_STREAM_CODEC = StreamCodec.of((buf, armourVariable) -> {
		buf.writeVarInt(armourVariable.any.orElse(-10));
		buf.writeVarInt(armourVariable.helmet.orElse(-10));
		buf.writeVarInt(armourVariable.chestplate.orElse(-10));
		buf.writeVarInt(armourVariable.leggings.orElse(-10));
		buf.writeVarInt(armourVariable.boots.orElse(-10));
		buf.writeVarInt(armourVariable.body.orElse(-10));
	}, buf -> {
		Integer any = buf.readVarInt();
		Integer helmet = buf.readVarInt();
		Integer chestplate = buf.readVarInt();
		Integer leggings = buf.readVarInt();
		Integer boots = buf.readVarInt();
		Integer body = buf.readVarInt();
		if (any == -10)
			any = null;
		if (helmet == -10)
			helmet = null;
		if (chestplate == -10)
			chestplate = null;
		if (leggings == -10)
			leggings = null;
		if (boots == -10)
			boots = null;
		if (body == -10)
			body = null;
		return new ArmourVariable(any, helmet, chestplate, leggings, boots, body);
	});
	public ArmourVariable(Integer any, Integer helmet, Integer chestplate, Integer leggings, Integer boots, Integer body) {
		this(Optional.ofNullable(any), Optional.ofNullable(helmet), Optional.ofNullable(chestplate), Optional.ofNullable(leggings), Optional.ofNullable(boots), Optional.ofNullable(body));
	}
	public ArmourVariable(Optional<Integer> any, Optional<Integer> helmet, Optional<Integer> chestplate, Optional<Integer> leggings, Optional<Integer> boots, Optional<Integer> body) {
		this.any = ConfigurableItemData.max(any, 1);
		this.helmet = ConfigurableItemData.max(helmet, 1);
		this.chestplate = ConfigurableItemData.max(chestplate, 1);
		this.leggings = ConfigurableItemData.max(leggings, 1);
		this.boots = ConfigurableItemData.max(boots, 1);
		this.body = ConfigurableItemData.max(body, 1);
	}
	public static ArmourVariable create(Integer any) {
		return new ArmourVariable(any, null, null, null, null, null);
	}
	public Integer getValue(Item item) {
		if (item instanceof ArmorItem armorItem) {
			return switch (armorItem.getType()) {
				case HELMET -> helmet.orElseGet(() -> any.orElse(null));
				case CHESTPLATE -> chestplate.orElseGet(() -> any.orElse(null));
				case LEGGINGS -> leggings.orElseGet(() -> any.orElse(null));
				case BOOTS -> boots.orElseGet(() -> any.orElse(null));
				case BODY -> body.orElseGet(() -> any.orElse(null));
			};
        }
		return any.orElse(null);
	}

	public boolean isEmpty() {
		return this.equals(EMPTY);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ArmourVariable that)) return false;
        return Objects.equals(any, that.any) && Objects.equals(helmet, that.helmet) && Objects.equals(chestplate, that.chestplate) && Objects.equals(leggings, that.leggings) && Objects.equals(boots, that.boots) && Objects.equals(body, that.body);
	}

	@Override
	public int hashCode() {
		return Objects.hash(any, helmet, chestplate, leggings, boots, body);
	}
}
