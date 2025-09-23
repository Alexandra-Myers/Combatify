package net.atlas.combatify.mixin;

import com.google.common.collect.Lists;
import net.atlas.combatify.Combatify;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.*;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements ServerEntityGetter, WorldGenLevel {
	protected ServerLevelMixin(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, RegistryAccess registryAccess, Holder<DimensionType> holder, boolean bl, boolean bl2, long l, int i) {
		super(writableLevelData, resourceKey, registryAccess, holder, bl, bl2, l, i);
	}

	@Shadow
	public abstract List<ServerPlayer> players();

	@Shadow
	@Final
	private ServerChunkCache chunkSource;

	@Inject(method = "method_31420", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", ordinal = 1))
	public void runSyncDelayed(TickRateManager tickRateManager, ProfilerFiller profilerFiller, Entity entity, CallbackInfo ci) {
		if (Combatify.CONFIG.delayedEntityUpdates()) {
			List<ServerPlayer> list = Lists.newArrayList();
			List<ServerPlayer> list2 = this.players();

			ChunkMap.TrackedEntity trackedEntity = chunkSource.chunkMap.combatify$getTrackedEntity(entity);
			if (trackedEntity == null) return;
			SectionPos sectionPos = trackedEntity.lastSectionPos;
			SectionPos sectionPos2 = SectionPos.of(entity);
			boolean bl = !Objects.equals(sectionPos, sectionPos2);
			if (bl) {
				trackedEntity.updatePlayers(list2);
				if (entity instanceof ServerPlayer) list.add((ServerPlayer) entity);

				trackedEntity.lastSectionPos = sectionPos2;
			}

			if (bl || this.chunkSource.chunkMap.getDistanceManager().inEntityTickingRange(sectionPos2.chunk().toLong()))
				trackedEntity.serverEntity.sendChanges();

			if (!list.isEmpty()) trackedEntity.updatePlayers(list);
		}
	}
}
