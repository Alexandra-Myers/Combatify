package net.atlas.combatify.extensions;

public interface ArmedEntityRenderStateExtensions {
	default boolean combatify$mobIsGuarding() {
		throw new IllegalStateException("Extension has not been applied");
	}
	default void combatify$setMobIsGuarding(boolean mobIsGuarding) {
		throw new IllegalStateException("Extension has not been applied");
	}
}
