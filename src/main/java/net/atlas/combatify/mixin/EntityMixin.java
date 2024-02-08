package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.atlas.combatify.Combatify;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityMixin {

	@Shadow
	public abstract float getBbWidth();

	@Shadow
	public abstract float getBbHeight();

	@ModifyReturnValue(method = "getPickRadius", at = @At(value = "RETURN"))
	public float inflateBoxes(float original) {
		float f = Math.max(getBbWidth(), getBbHeight());
		if (f < Combatify.CONFIG.minHitboxSize()) {
		  return (float) ((Combatify.CONFIG.minHitboxSize() - f) * 0.5F);
		}
		return original;
	}
}
