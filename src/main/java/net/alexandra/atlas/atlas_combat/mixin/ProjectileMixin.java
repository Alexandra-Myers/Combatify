package net.alexandra.atlas.atlas_combat.mixin;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public abstract class ProjectileMixin {
	@Shadow
	public abstract void shoot(double v, double v1, double v2, float v3, float v4);

	@Inject(method = "shootFromRotation", at = @At(value = "HEAD"), cancellable = true)
	public void redirectShoot(Entity user, float pitch, float yaw, float roll, float modifierZ, float modifierXYZ, CallbackInfo ci) {
		float f = -Mth.sin(yaw * (float) (Math.PI / 180.0)) * Mth.cos(pitch * (float) (Math.PI / 180.0));
		float g = -Mth.sin((pitch + roll) * (float) (Math.PI / 180.0));
		float h = Mth.cos(yaw * (float) (Math.PI / 180.0)) * Mth.cos(pitch * (float) (Math.PI / 180.0));
		shoot(f, g, h, modifierZ, modifierXYZ);
		ci.cancel();
	}
}
