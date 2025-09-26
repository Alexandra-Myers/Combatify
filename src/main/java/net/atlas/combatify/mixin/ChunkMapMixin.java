package net.atlas.combatify.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.annotation.mixin.Incompatible;
import net.atlas.combatify.extensions.ChunkMapExtensions;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin implements ChunkMapExtensions {
	@Shadow
	@Final
	private Int2ObjectMap<ChunkMap.TrackedEntity> entityMap;

	@Inject(method = "tick()V", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;"), cancellable = true)
	public void cancelIfDelay(CallbackInfo ci) {
		if (Combatify.CONFIG.delayedEntityUpdates()) ci.cancel();
	}

	@Override
	public ChunkMap.TrackedEntity combatify$getTrackedEntity(Entity entity) {
		if (!entityMap.containsKey(entity.getId())) return null;
		return entityMap.get(entity.getId());
	}
}
