package net.atlas.combatify.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.atlas.combatify.util.MethodHandler.*;

@OnlyIn(Dist.CLIENT)
public class ClientMethodHandler {
	public static HitResult redirectResult(@Nullable HitResult instance) {
		if (instance == null)
			return null;
		Minecraft minecraft = Minecraft.getInstance();
		if (instance.getType() == HitResult.Type.BLOCK) {
			Player minecraftPlayer = Objects.requireNonNull(minecraft.player);
			Entity player = Objects.requireNonNull(minecraft.getCameraEntity());
			double reach = MethodHandler.getCurrentAttackReachWithoutChargedReach(minecraftPlayer) + (!minecraftPlayer.isCrouching() ? getChargedReach(minecraftPlayer.getItemInHand(InteractionHand.MAIN_HAND)) + 0.25 : 0.25);
			EntityHitResult rayTraceResult = rayTraceEntity(player, 1.0F, reach);
			Entity entity = rayTraceResult != null ? rayTraceResult.getEntity() : null;
			if (entity != null) {
				double dist = player.getEyePosition().distanceToSqr(MethodHandler.getNearestPointTo(entity.getBoundingBox(), player.getEyePosition()));
				reach *= reach;
				if (dist > reach)
					return null;
				double enemyDistance = player.distanceTo(entity);
				HitResult newResult = pickCollisions(player, enemyDistance);
				if (newResult.getType() != HitResult.Type.MISS)
					return null;
				return rayTraceResult;
			}
		}
		return null;
	}
}
