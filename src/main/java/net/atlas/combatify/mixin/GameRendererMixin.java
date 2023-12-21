package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@SuppressWarnings("unused")
@Mixin(GameRenderer.class)
abstract class GameRendererMixin implements ResourceManagerReloadListener/*, AutoCloseable*/ {
    @Shadow
	@Final
	Minecraft minecraft;

	@ModifyArg(method = "pick(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;"), index = 5)
	private double getHigherRange(double original) {
		if (this.minecraft.player != null) {
			return MethodHandler.getSquaredCurrentAttackReach(minecraft.player,0.0F);
		}
		return original;
	}

	@ModifyArg(method = "pick(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;"), index = 3)
	private AABB getHigherRange1(Entity entity, Vec3 vec3, Vec3 vec33, AABB original, Predicate<Entity> predicate, double d) {
		if (this.minecraft.player != null) {
			return entity.getBoundingBox().expandTowards(entity.getViewVector(1.0F).scale(MethodHandler.getCurrentAttackReach(minecraft.player,0.0F))).inflate(1.0);
		}
		return original;
	}

	@ModifyArg(method = "pick(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;"), index = 2)
	private Vec3 getHigherRange2(Entity entity, Vec3 vec3, Vec3 original, AABB aabb, Predicate<Entity> predicate, double d) {
		if (this.minecraft.player != null) {
			double reach = MethodHandler.getCurrentAttackReach(minecraft.player,0.0F);
			Vec3 vec32 = entity.getViewVector(1.0F);
			return vec3.add(vec32.x * reach, vec32.y * reach, vec32.z * reach);
		}
		return original;
	}
	@Inject(method = "pick(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D", ordinal = 1))
	private void modifyReachCheck(float f, CallbackInfo ci, @Local(ordinal = 0) EntityHitResult hitResult, @Local(ordinal = 0) Entity entity) {
		double dist = entity.getEyePosition(f).distanceToSqr(MethodHandler.getNearestPointTo(hitResult.getEntity().getBoundingBox(), entity.getEyePosition(f)));
		if (minecraft.player != null && dist < MethodHandler.getCurrentAttackReach(minecraft.player,0.0F)) {
			this.minecraft.hitResult = hitResult;
			if (hitResult.getEntity() instanceof LivingEntity || hitResult.getEntity() instanceof ItemFrame) {
				this.minecraft.crosshairPickEntity = hitResult.getEntity();
			}
		}
	}

    @ModifyExpressionValue(method = "pick(F)V", at = @At(value = "CONSTANT", args = "doubleValue=3.0"))
    private double getActualAttackRange(double original) {
        if (this.minecraft.player != null) {
            return MethodHandler.getCurrentAttackReach(minecraft.player,0.0F);
        }
        return original - 0.5;
    }

	@ModifyExpressionValue(method = "pick(F)V", at = @At(value = "CONSTANT", args = "doubleValue=9.0"))
    private double getActualAttackRange1(double original) {
		double d = Math.sqrt(original);
		d -= 0.5;
		d *= d;
        if (this.minecraft.player != null) {
            return MethodHandler.getSquaredCurrentAttackReach(minecraft.player,0.0F);
        }
        return d;
    }
}
