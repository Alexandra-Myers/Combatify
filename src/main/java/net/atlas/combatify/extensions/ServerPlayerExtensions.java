package net.atlas.combatify.extensions;

public interface ServerPlayerExtensions {

	default boolean combatify$isRetainingAttack() {
		throw new IllegalStateException("Extension has not been applied");
	}

	default void combatify$setRetainAttack(boolean retain) {
		throw new IllegalStateException("Extension has not been applied");
	}
}
