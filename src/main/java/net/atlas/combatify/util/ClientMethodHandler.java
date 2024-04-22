package net.atlas.combatify.util;

import net.atlas.combatify.Combatify;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static net.atlas.combatify.util.MethodHandler.*;

@Environment(EnvType.CLIENT)
public class ClientMethodHandler {
	public static void redirectResult(@Nullable HitResult instance) {
		if (instance == null)
			return;
		Minecraft minecraft = Minecraft.getInstance();
		if (Combatify.CONFIG.swingThroughGrass() && instance.getType() == HitResult.Type.BLOCK) {
			BlockHitResult blockHitResult = (BlockHitResult)instance;
			BlockPos blockPos = blockHitResult.getBlockPos();
			Level level = Objects.requireNonNull(minecraft.level);
			Player player = Objects.requireNonNull(minecraft.player);
			boolean bl = !level.getBlockState(blockPos).canOcclude() && !level.getBlockState(blockPos).getBlock().hasCollision;
			EntityHitResult rayTraceResult = rayTraceEntity(player, 1.0F, getCurrentAttackReach(player, 0.0F));
			Entity entity = rayTraceResult != null ? rayTraceResult.getEntity() : null;
			if (entity != null && bl) {
				double dist = player.getEyePosition().distanceToSqr(MethodHandler.getNearestPointTo(entity.getBoundingBox(), player.getEyePosition()));
				double reach = MethodHandler.getCurrentAttackReach(player, 1.0F);
				reach *= reach;
				if (!player.hasLineOfSight(entity))
					reach = 6.25;
				if (dist > reach)
					return;
				double enemyDistance = player.distanceTo(entity);
				List<BlockPos> blockPosList = pickFromPos(player, enemyDistance);
				if (!blockPosList.isEmpty())
					return;
				minecraft.hitResult = rayTraceResult;
				if (entity instanceof LivingEntity || entity instanceof ItemFrame) {
					minecraft.crosshairPickEntity = entity;
				}
			}
		}
	}
}
