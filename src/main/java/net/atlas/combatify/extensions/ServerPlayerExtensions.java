package net.atlas.combatify.extensions;

import net.atlas.combatify.util.HitResultRotationEntry;
import net.minecraft.world.phys.HitResult;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public interface ServerPlayerExtensions {

	void adjustHitResults(HitResult newValue);

    void setAwaitingResponse(boolean awaitingResponse);

	boolean isAwaitingResponse();

    CopyOnWriteArrayList<HitResultRotationEntry> getOldHitResults();

	boolean isRetainingAttack();

	void setRetainAttack(boolean retain);

	void getPresentResult();
}
