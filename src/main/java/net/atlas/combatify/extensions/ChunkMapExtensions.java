package net.atlas.combatify.extensions;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.entity.Entity;

public interface ChunkMapExtensions {
	default ChunkMap.TrackedEntity combatify$getTrackedEntity(Entity entity) {
		throw new IllegalStateException("Extension has not been applied");
	}
}
