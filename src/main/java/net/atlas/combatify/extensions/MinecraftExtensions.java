package net.atlas.combatify.extensions;

import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

public interface MinecraftExtensions {
	default void combatify$setAimAssistHitResult(@Nullable HitResult aimAssistHitResult) {
		throw new IllegalStateException("Extension has not been applied");
	}
}
