package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public abstract class ProjectileMixin extends Entity {
	public ProjectileMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "shootFromRotation", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getKnownMovement()Lnet/minecraft/world/phys/Vec3;"), cancellable = true)
	public void redirectShoot(Entity user, float pitch, float yaw, float roll, float modifierZ, float modifierXYZ, CallbackInfo ci) {
		if (Combatify.CONFIG.ctsMomentumPassedToProjectiles()) {
			Vec3 currentMomentum = getDeltaMovement();
			Vec3 entityMomentum = user.onGround() ? user.getDeltaMovement() : user.getDeltaMovement().multiply(1.0, 0.0, 1.0);
			Vec3 projected = MethodHandler.project(entityMomentum, currentMomentum);
			setDeltaMovement(currentMomentum.add(projected));
			ci.cancel();
		}
	}
}
