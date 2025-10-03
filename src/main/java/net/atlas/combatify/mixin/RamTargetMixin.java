package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.RamTarget;
import net.minecraft.world.entity.animal.goat.Goat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RamTarget.class)
public class RamTargetMixin {
	@WrapOperation(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/goat/Goat;J)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
	public void knockback(LivingEntity instance, double d, double e, double f, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) ServerLevel serverLevel, @Local(ordinal = 0, argsOnly = true) Goat goat) {
		Combatify.CONFIG.knockbackMode().runKnockback(instance, serverLevel.damageSources().mobAttack(goat), d, e, f, original::call);
	}
}
