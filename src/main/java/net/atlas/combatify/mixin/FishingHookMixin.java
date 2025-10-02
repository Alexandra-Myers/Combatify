package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends Entity {
	public FishingHookMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Shadow
	@Nullable
	public abstract Player getPlayerOwner();

	@WrapOperation(method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;"))
	public Vec3 modifyMovement(Vec3 instance, double d, double e, double f, Operation<Vec3> original, @Local(ordinal = 0, argsOnly = true) Player player) {
		if (Combatify.CONFIG.fishingHookKB()) {
			float playerXRotRadians = player.getXRot() * (float) (Math.PI / 180);
			float playerYRotRadians = player.getYRot() * (float) (Math.PI / 180);
			double xRotFactor = Mth.cos(playerXRotRadians);
			Vec3 newDelta = new Vec3(-Mth.sin(playerYRotRadians) * xRotFactor * 0.4,
				-Mth.sin(playerXRotRadians) * 0.4,
				Mth.cos(playerYRotRadians) * xRotFactor * 0.4);
			return newDelta
				.normalize()
				.add(random.nextGaussian() * 0.007499999832361937,
					random.nextGaussian() * 0.007499999832361937,
					random.nextGaussian() * 0.007499999832361937)
				.scale(1.5);
		}

		return original.call(instance, d, e, f);
	}

	@Inject(method = "onHitEntity", at = @At(value = "TAIL"))
	protected void onHitEntity(EntityHitResult entityHitResult, CallbackInfo ci) {
		if (Combatify.CONFIG.fishingHookKB() && entityHitResult.getEntity() instanceof LivingEntity livingEntity)
			livingEntity.hurt(damageSources().thrown(FishingHook.class.cast(this), getPlayerOwner()), 0);
	}
}
