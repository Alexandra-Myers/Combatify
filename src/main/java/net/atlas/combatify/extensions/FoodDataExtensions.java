package net.atlas.combatify.extensions;

public interface FoodDataExtensions {
	default float combatify$getExhaustionLevel() {
		throw new IllegalStateException("Extension has not been applied");
	}
	default float combatify$setExhaustion(float exhaustionLevel) {
		throw new IllegalStateException("Extension has not been applied");
	}
}
