package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.extensions.LivingEntityExtensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mob.class)
public class MobMixin {
	@Redirect(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
	public void redirectKnockback(LivingEntity instance, double strength, double x, double z) {
		((LivingEntityExtensions)instance).newKnockback((float)strength, x, z);
	}
}
