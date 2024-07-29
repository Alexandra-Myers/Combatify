package net.atlas.combatify.util;

import net.atlas.combatify.Combatify;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static net.atlas.combatify.util.MethodHandler.*;

@Environment(EnvType.CLIENT)
public class ClientMethodHandler {
	public static HitResult redirectResult(@Nullable HitResult instance) {
		if (instance == null)
			return null;
		Minecraft minecraft = Minecraft.getInstance();
		if (Combatify.CONFIG.swingThroughGrass() && instance.getType() == HitResult.Type.BLOCK) {
			BlockHitResult blockHitResult = (BlockHitResult)instance;
			BlockPos blockPos = blockHitResult.getBlockPos();
			Level level = Objects.requireNonNull(minecraft.level);
			Player minecraftPlayer = Objects.requireNonNull(minecraft.player);
			Entity player = Objects.requireNonNull(minecraft.getCameraEntity());
			boolean bl = !level.getBlockState(blockPos).canOcclude() && !level.getBlockState(blockPos).getBlock().hasCollision;
			EntityHitResult rayTraceResult = rayTraceEntity(player, 1.0F, getCurrentAttackReach(minecraftPlayer, 0.0F));
			Entity entity = rayTraceResult != null ? rayTraceResult.getEntity() : null;
			if (entity != null && bl) {
				double dist = player.getEyePosition().distanceToSqr(MethodHandler.getNearestPointTo(entity.getBoundingBox(), player.getEyePosition()));
				double reach = MethodHandler.getCurrentAttackReach(minecraftPlayer, 1.0F);
				reach *= reach;
				if (!minecraftPlayer.hasLineOfSight(entity))
					reach = 6.25;
				if (dist > reach)
					return null;
				double enemyDistance = player.distanceTo(entity);
				List<BlockPos> blockPosList = pickFromPos(player, enemyDistance);
				if (!blockPosList.isEmpty())
					return null;
				return rayTraceResult;
			}
		}
		return null;
	}
}
