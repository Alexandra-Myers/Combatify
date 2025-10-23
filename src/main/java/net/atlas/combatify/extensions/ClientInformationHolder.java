package net.atlas.combatify.extensions;

public interface ClientInformationHolder {
	default void combatify$setShieldOnCrouch(boolean hasShieldOnCrouch) {
		throw new IllegalStateException("Extension has not been applied");
	}
}
