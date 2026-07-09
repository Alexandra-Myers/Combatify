package net.atlas.combatify.mixin;

//? <26.2 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.config.KnockbackMode;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
*///?}
import net.minecraft.world.entity.ai.behavior.RamTarget;
import org.spongepowered.asm.mixin.Mixin;
//? <26.2 {
/*import org.spongepowered.asm.mixin.injection.At;
*///?}

@Mixin(RamTarget.class)
public class RamTargetMixin {
	//? <26.2 {
	/*@WrapOperation(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/goat/Goat;J)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
	public void knockback(LivingEntity instance, double strength, double x, double z, Operation<Void> original, @Local(ordinal = 1) DamageSource source) {
		KnockbackMode.extractContext(instance, source);
		original.call(instance, strength, x, z);
	}
	*///?}
}
