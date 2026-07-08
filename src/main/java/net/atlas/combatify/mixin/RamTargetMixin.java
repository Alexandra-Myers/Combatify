package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.RamTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RamTarget.class)
public class RamTargetMixin {
	//? <26.2 {
	/*@WrapOperation(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/goat/Goat;J)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
	public void knockback(LivingEntity instance, double d, double e, double f, Operation<Void> original, @Local(ordinal = 1) DamageSource damageSource) {
		Combatify.CONFIG.knockbackMode().runKnockback(instance, damageSource, d, e, f, original::call);
	}
	*///?} >=26.2 {
	@WrapOperation(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/goat/Goat;J)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDDLnet/minecraft/world/damagesource/DamageSource;F)V"))
	public void knockback(LivingEntity instance, double d, double e, double f, DamageSource damageSource, float v, Operation<Void> original) {
		Combatify.CONFIG.knockbackMode().runKnockback(instance, damageSource, d, e, f, original::call, v);
	}
	//?}
}
