package net.alexandra.atlas.atlas_combat.extensions;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

public interface IMinecraft {
	void startUseItem(InteractionHand hand);
	EntityHitResult rayTraceEntity(Player player, float partialTicks, double blockReachDistance);
}
