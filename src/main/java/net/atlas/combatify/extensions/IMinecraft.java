package net.atlas.combatify.extensions;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public interface IMinecraft {
	HitResult redirectResult(HitResult instance);

	void startUseItem(InteractionHand hand);
	EntityHitResult rayTraceEntity(Player player, float partialTicks, double blockReachDistance);
}
