package net.atlas.combatify.util;

import net.atlas.combatify.extensions.PlayerExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.atlas.combatify.util.MethodHandler.rayTraceEntity;

@OnlyIn(Dist.CLIENT)
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
			EntityHitResult rayTraceResult = rayTraceEntity(player, 1.0F, MethodHandler.getCurrentAttackReach(player, 0.0F));
			Entity entity = rayTraceResult != null ? rayTraceResult.getEntity() : null;
			if (entity != null && bl) {
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
