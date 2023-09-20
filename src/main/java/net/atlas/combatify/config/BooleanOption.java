package net.atlas.combatify.config;

import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Field;

public class BooleanOption extends SynchableOption<Boolean> {
	public BooleanOption(Field configValue, boolean requiresRestart) {
		super(configValue, requiresRestart);
	}

	@Override
	Boolean readFromBuf(FriendlyByteBuf buf) {
		return buf.readBoolean();
	}

	@Override
	void writeToBuf(FriendlyByteBuf buf) {
		buf.writeBoolean(get());
	}
}
