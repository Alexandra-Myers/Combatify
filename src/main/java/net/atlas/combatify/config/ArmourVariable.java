package net.atlas.combatify.config;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

import java.util.Objects;

public record ArmourVariable(Integer any, Integer helmet, Integer chestplate, Integer leggings, Integer boots, Integer body) {
	public static final ArmourVariable EMPTY = new ArmourVariable(null, null, null, null, null, null);
	public static final StreamCodec<RegistryFriendlyByteBuf, ArmourVariable> ARMOUR_VARIABLE_STREAM_CODEC = StreamCodec.of((buf, armourVariable) -> {
		buf.writeVarInt(armourVariable.any == null ? -10 : armourVariable.any);
		buf.writeVarInt(armourVariable.helmet == null ? -10 : armourVariable.helmet);
		buf.writeVarInt(armourVariable.chestplate == null ? -10 : armourVariable.chestplate);
		buf.writeVarInt(armourVariable.leggings == null ? -10 : armourVariable.leggings);
		buf.writeVarInt(armourVariable.boots == null ? -10 : armourVariable.boots);
		buf.writeVarInt(armourVariable.body == null ? -10 : armourVariable.body);
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
	public static ArmourVariable create(Integer any) {
		return new ArmourVariable(ConfigurableItemData.max(any, 1), null, null, null, null, null);
	}
	public static ArmourVariable create(Integer any, Integer helmet, Integer chestplate, Integer leggings, Integer boots, Integer body) {
		return new ArmourVariable(ConfigurableItemData.max(any, 1), ConfigurableItemData.max(helmet, 1), ConfigurableItemData.max(chestplate, 1), ConfigurableItemData.max(leggings, 1), ConfigurableItemData.max(boots, 1), ConfigurableItemData.max(body, 1));
	}
	public Integer getValue(Item item) {
		if (item instanceof ArmorItem armorItem) {
			Integer forType = switch (armorItem.getType()) {
				case HELMET -> helmet;
				case CHESTPLATE -> chestplate;
				case LEGGINGS -> leggings;
				case BOOTS -> boots;
				case BODY -> body;
			};
			return forType == null ? any : forType;
		}
		return any;
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
