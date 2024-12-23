package net.atlas.combatify.extensions;

public interface BlockHitResultExtensions {
	default void combatify$setIsLedgeEdge() {
		throw new IllegalStateException("Extension has not been applied");
	}
	default boolean combatify$isLedgeEdge() {
		throw new IllegalStateException("Extension has not been applied");
	}
}
