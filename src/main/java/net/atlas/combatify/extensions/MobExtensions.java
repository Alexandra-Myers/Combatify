package net.atlas.combatify.extensions;

public interface MobExtensions {
	default boolean combatify$isGuarding() {
		throw new IllegalStateException("Extension has not been applied");
	}
	default boolean combatify$overrideSprintLogic() {
		throw new IllegalStateException("Extension has not been applied");
	}
	default void combatify$setOverrideSprintLogic(boolean override) {
		throw new IllegalStateException("Extension has not been applied");
	}
}
