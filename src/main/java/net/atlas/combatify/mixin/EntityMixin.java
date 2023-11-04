package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

	@Shadow
	public abstract float getBbWidth();

	@Shadow
	public abstract float getBbHeight();

	@Inject(method = "getPickRadius", at = @At(value = "HEAD"))
	public void inflateBoxes(CallbackInfoReturnable<Float> cir) {
		float f = Math.max(getBbWidth(), getBbHeight());
		if (f < Combatify.CONFIG.minHitboxSize()) {
		  cir.setReturnValue((Combatify.CONFIG.minHitboxSize() - f) * 0.5F);
		}
	}
}
