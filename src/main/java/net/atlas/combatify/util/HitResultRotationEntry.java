package net.atlas.combatify.util;

import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public record HitResultRotationEntry(HitResult hitResult, float xRot, float yRot) {
	public boolean shouldAccept(float currentXRot, float currentYRot) {
		float xDiff = Math.abs(currentXRot - xRot);
		float yDiff = Math.abs(currentYRot - yRot);
		return !(xDiff > 20) && !(yDiff > 20);
	}
	public int compareTo(@NotNull HitResultRotationEntry second, float currentXRot, float currentYRot) {
		int firstPreference = 0;
		int secondPreference = 0;
		float xDiff = Math.abs(currentXRot - xRot);
		float yDiff = Math.abs(currentYRot - yRot);
		firstPreference -= Math.round(xDiff + yDiff);
		if (hitResult.getType() == HitResult.Type.ENTITY) firstPreference += 50;
		xDiff = Math.abs(currentXRot - second.xRot);
		yDiff = Math.abs(currentYRot - second.yRot);
		secondPreference -= Math.round(xDiff + yDiff);
		if (second.hitResult.getType() == HitResult.Type.ENTITY) secondPreference += 50;
		return firstPreference - secondPreference;
	}
}
