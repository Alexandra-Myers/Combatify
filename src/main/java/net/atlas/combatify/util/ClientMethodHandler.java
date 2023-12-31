package net.atlas.combatify.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.atlas.combatify.util.MethodHandler.*;

@OnlyIn(Dist.CLIENT)
public class ClientMethodHandler {
	public static void redirectResult(@Nullable HitResult instance) {
		if (instance == null)
			return;
		Minecraft minecraft = Minecraft.getInstance();
		if(instance.getType() == HitResult.Type.BLOCK) {
			BlockHitResult blockHitResult = (BlockHitResult)instance;
			BlockPos blockPos = blockHitResult.getBlockPos();
			Level level = Objects.requireNonNull(minecraft.level);
			Player player = Objects.requireNonNull(minecraft.player);
			boolean bl = !level.getBlockState(blockPos).canOcclude() && !level.getBlockState(blockPos).getBlock().hasCollision;
			EntityHitResult rayTraceResult = rayTraceEntity(player, 1.0F, getCurrentAttackReach(player, 0.0F));
			Entity entity = rayTraceResult != null ? rayTraceResult.getEntity() : null;
			if (entity != null && bl) {
				double dist = player.getEyePosition().distanceToSqr(getNearestPointTo(entity.getBoundingBox(), player.getEyePosition()));
				double reach = getCurrentAttackReach(player, 1.0F);
				reach *= reach;
				if (!player.hasLineOfSight(entity))
					reach = 6.25;
				if (dist > reach)
					return;
				double enemyDistance = player.distanceTo(entity);
				double d = 0;
				HitResult check;
				while (d <= enemyDistance) {
					check = pickFromPos(player, enemyDistance, d);
					if (check.getType() == HitResult.Type.BLOCK) {
						bl = !level.getBlockState(((BlockHitResult)check).getBlockPos()).canOcclude() && !level.getBlockState(((BlockHitResult)check).getBlockPos()).getBlock().hasCollision;
						if (!bl)
							return;
					}
					d += 0.0002;
				}
				minecraft.hitResult = rayTraceResult;
				if (entity instanceof LivingEntity || entity instanceof ItemFrame)
					minecraft.crosshairPickEntity = entity;
			}
		}
	}
}
