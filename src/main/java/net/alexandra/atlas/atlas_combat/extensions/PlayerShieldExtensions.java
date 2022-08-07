package net.alexandra.atlas.atlas_combat.extensions;

public interface PlayerShieldExtensions {

	default boolean customShieldInteractions(float damage) {return false;}

	default boolean hasEnabledShieldOnCrouch() {
		return false;
	}
}
