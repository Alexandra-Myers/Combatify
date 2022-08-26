package net.alexandra.atlas.atlas_combat.extensions;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

import javax.annotation.Nullable;

public interface IMinecraft {
	void startUseItem(InteractionHand hand);
	EntityHitResult rayTraceEntity(Player player, float partialTicks, double blockReachDistance);

    @Nullable
    EntityHitResult findEntity(Player player, float partialTicks, double blockReachDistance);

    @Nullable
    EntityHitResult findNormalEntity(Player player, float partialTicks, double blockReachDistance);

	@Nullable
	EntityHitResult findEntity(Player player, float partialTicks, double blockReachDistance, int strengthMultiplier);

	@Nullable
	EntityHitResult findNormalEntity(Player player, float partialTicks, double blockReachDistance, int strengthMultiplier);

	void getStartAttack();
}
