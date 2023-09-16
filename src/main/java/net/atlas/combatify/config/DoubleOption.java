package net.atlas.combatify.config;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.ForgeConfigSpec;

public class DoubleOption extends SynchableOption<Double> {
	public DoubleOption(ForgeConfigSpec.ConfigValue<Double> configValue, boolean requiresRestart) {
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
