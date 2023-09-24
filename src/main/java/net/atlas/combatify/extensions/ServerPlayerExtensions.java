package net.atlas.combatify.extensions;

import net.minecraft.world.phys.HitResult;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public interface ServerPlayerExtensions {

	void adjustHitResults(HitResult newValue);

    void setAwaitingResponse(boolean awaitingResponse);

	boolean isAwaitingResponse();

    CopyOnWriteArrayList<HitResult> getOldHitResults();

	boolean isRetainingAttack();

	void setRetainAttack(boolean retain);

    Map<HitResult, Float[]> getHitResultToRotationMap();

	void getPresentResult();
}
