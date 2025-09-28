package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.atlas.combatify.Combatify;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerChunkCache.class)
public class ServerChunkCacheMixin {
	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap;tick()V"))
	public void removeCall(ChunkMap instance, Operation<Void> original) {
		if (!Combatify.CONFIG.delayedEntityUpdates()) original.call(instance);
	}
}
