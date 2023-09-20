package net.atlas.combatify.config;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Field;

public abstract class SynchableOption<T> {
	private final Field value;
	private T overrideValue = null;
	protected final boolean restartRequired;
	public SynchableOption(Field configValue, boolean requiresRestart) {
		value = configValue;
		restartRequired = requiresRestart;
	}
	public final SynchableOption<T> trySync(FriendlyByteBuf buf) {
		overrideValue = readFromBuf(buf);
		return this;
	}
	public final T findMismatch(FriendlyByteBuf buf) {
		overrideValue = readFromBuf(buf);
		try {
			if (restartRequired && overrideValue != value.get(null))
				return overrideValue;
		} catch (IllegalAccessException e) {
			throw new ReportedException(new CrashReport("Access to this option is not allowed! This should not happen!", e));
		}
		return null;
	}
	abstract T readFromBuf(FriendlyByteBuf buf);
	public final T get() {
		if(overrideValue != null)
			return overrideValue;
		try {
			return (T) value.get(null);
		} catch (IllegalAccessException e) {
			throw new ReportedException(new CrashReport("Access to this option is not allowed! This should not happen!", e));
		}
	}
	abstract void writeToBuf(FriendlyByteBuf buf);
	public final void restore() {
		overrideValue = null;
	}
}
