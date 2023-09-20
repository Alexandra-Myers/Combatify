package net.atlas.combatify.config;

import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Field;

public class DoubleOption extends SynchableOption<Double> {
	public DoubleOption(Field configValue, boolean requiresRestart) {
		super(configValue, requiresRestart);
	}

	@Override
	Double readFromBuf(FriendlyByteBuf buf) {
		return (double) buf.readFloat();
	}

	@Override
	void writeToBuf(FriendlyByteBuf buf) {
		buf.writeFloat(get().floatValue());
	}
}
