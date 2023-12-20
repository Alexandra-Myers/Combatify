package net.atlas.combatify.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.atlas.combatify.util.MethodHandler.*;

@Environment(EnvType.CLIENT)
public class ClientMethodHandler {
	public static HitResult redirectResult(@Nullable HitResult instance) {
		if (instance == null)
			return null;
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
				double reach = player.distanceTo(entity);
				double d = 0;
				HitResult check;
				while (d <= Math.ceil(reach)) {
					check = pickFromPos(player, blockPos, reach, d);
					if(check.getType() == HitResult.Type.BLOCK) {
						bl = !level.getBlockState(((BlockHitResult)check).getBlockPos()).canOcclude() && !level.getBlockState(((BlockHitResult)check).getBlockPos()).getBlock().hasCollision;
						if (!bl)
							return instance;
					}
					d += 0.1;
				}
				minecraft.crosshairPickEntity = entity;
				minecraft.hitResult = rayTraceResult;
				return minecraft.hitResult;
			} else {
				return instance;
			}

		}
		return instance;
	}
}
