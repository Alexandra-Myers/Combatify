package net.alexandra.atlas.atlas_combat.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Snowball;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Snowball.class)
public class SnowballMixin {

	@Redirect(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
	public boolean redirectDamage(Entity instance, DamageSource source, float amount) {
		return instance.hurt(DamageSource.thrown(((Snowball) (Object)this), ((Snowball) (Object)this).getOwner()), 0.5F);
	}

}
