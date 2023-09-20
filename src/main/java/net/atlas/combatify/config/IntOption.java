package net.atlas.combatify.config;

import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Field;

public class IntOption extends SynchableOption<Integer> {
	public IntOption(Field configValue, boolean requiresRestart) {
		super(configValue, requiresRestart);
	}

	@Override
	Integer readFromBuf(FriendlyByteBuf buf) {
		return buf.readInt();
	}

	@Override
	void writeToBuf(FriendlyByteBuf buf) {
		buf.writeInt(get());
	}
}
