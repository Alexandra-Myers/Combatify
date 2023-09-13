package net.atlas.combatify.extensions;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.Map;

public interface ServerPlayerExtensions {
	HitResult pickResult(Entity camera);

	void adjustHitResults(HitResult newValue);

    void setAwaitingResponse(boolean awaitingResponse);

	boolean isAwaitingResponse();

    ArrayList<HitResult> getOldHitResults();

	boolean isRetainingAttack();

	void setRetainAttack(boolean retain);

    Map<HitResult, Float[]> getHitResultToRotationMap();

	void getPresentResult();
}
