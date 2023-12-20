package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

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
	private AABB getHigherRange1(AABB original, @Local(ordinal = 0) Entity entity, @Local(ordinal = 1) Vec3 vec32) {
		if (this.minecraft.player != null) {
			return entity.getBoundingBox().expandTowards(vec32.scale(MethodHandler.getCurrentAttackReach(minecraft.player,0.0F))).inflate(1.0);
		}
		return original;
	}

	@ModifyArg(method = "pick(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;"), index = 2)
	private Vec3 getHigherRange2(Vec3 original, @Local(ordinal = 0) Vec3 vec3, @Local(ordinal = 1) Vec3 vec32) {
		if (this.minecraft.player != null) {
			double reach = MethodHandler.getCurrentAttackReach(minecraft.player,0.0F);
			return vec3.add(vec32.x * reach, vec32.y * reach, vec32.z * reach);
		}
		return original;
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
