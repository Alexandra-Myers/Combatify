package net.atlas.combatify.mixin;

import net.atlas.combatify.util.MethodHandler;
import net.minecraft.world.entity.EntityDimensions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityDimensions.class)
public class EntityDimensionsMixin {
	@Mutable
	@Shadow
	@Final
	public float width;

	@Mutable
	@Shadow
	@Final
	public float height;

	@Inject(method = "<init>", at = @At(value = "RETURN"))
	public void inflateBoxes(float f, float g, boolean bl, CallbackInfo ci) {
		width = MethodHandler.inflate(f);
		height = MethodHandler.inflate(g);
	}
}
