package net.atlas.combatify.config;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.ForgeConfigSpec;

public class BooleanOption extends SynchableOption<Boolean> {
	public BooleanOption(ForgeConfigSpec.ConfigValue<Boolean> configValue, boolean requiresRestart) {
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
