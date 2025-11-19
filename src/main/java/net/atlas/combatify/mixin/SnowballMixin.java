package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Snowball.class)
public class SnowballMixin {

	@ModifyArg(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"), index = 1)
	public float modDamage(float f) {
		return f + Combatify.CONFIG.snowballDamage().floatValue();
	}

}
