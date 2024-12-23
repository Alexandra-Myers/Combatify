package net.atlas.combatify.extensions;

public interface MobExtensions {
	default boolean combatify$isGuarding() {
		throw new IllegalStateException("Extension has not been applied");
	}
}
