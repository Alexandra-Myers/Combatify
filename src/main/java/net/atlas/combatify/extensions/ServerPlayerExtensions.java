package net.atlas.combatify.extensions;

import net.minecraft.world.phys.HitResult;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public interface ServerPlayerExtensions {
	void combatify$adjustHitResults(HitResult newValue);

    void combatify$setAwaitingResponse(boolean awaitingResponse);

	boolean combatify$isAwaitingResponse();

    CopyOnWriteArrayList<HitResult> combatify$getOldHitResults();

	boolean combatify$isRetainingAttack();

	void combatify$setRetainAttack(boolean retain);

    Map<HitResult, Float[]> combatify$getHitResultToRotationMap();

	void combatify$getPresentResult();
}
