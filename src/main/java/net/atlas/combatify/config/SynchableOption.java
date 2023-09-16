package net.atlas.combatify.config;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.ForgeConfigSpec;

public abstract class SynchableOption<T> {
	private final ForgeConfigSpec.ConfigValue<T> value;
	private T overrideValue = null;
	protected final boolean restartRequired;
	public SynchableOption(ForgeConfigSpec.ConfigValue<T> configValue, boolean requiresRestart) {
		value = configValue;
		restartRequired = requiresRestart;
	}
	public final SynchableOption<T> trySync(FriendlyByteBuf buf) {
		overrideValue = readFromBuf(buf);
		return this;
	}
	public final T findMismatch(FriendlyByteBuf buf) {
		overrideValue = readFromBuf(buf);
		if (restartRequired && overrideValue != value.get())
			return overrideValue;
		return null;
	}
	abstract T readFromBuf(FriendlyByteBuf buf);
	public final T get() {
		if(overrideValue != null)
			return overrideValue;
		return value.get();
	}
	abstract void writeToBuf(FriendlyByteBuf buf);
	public final void restore() {
		overrideValue = null;
	}
}
