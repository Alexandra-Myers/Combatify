package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

	@Shadow
	private AABB bb;

	@Inject(method = "setBoundingBox", at = @At(value = "RETURN"))
	public void inflateBoxes(AABB aABB, CallbackInfo ci) {
		if (aABB.getSize() < Combatify.CONFIG.minHitboxSize())
			bb = MethodHandler.inflateBoundingBox(aABB);
	}
}
