package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.projectile.Snowball;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Snowball.class)
public class SnowballMixin {

	@Unique
	public final float snowballDamage = AtlasCombat.CONFIG.snowballDamage();


	@Redirect(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
	public boolean redirectDamage(Entity instance, DamageSource source, float amount) {
		var snowball = Snowball.class.cast(this);
		return instance.hurt(DamageSource.thrown(snowball, snowball.getOwner()), snowballDamage + (instance instanceof Blaze ? 3 : 0));
	}

}
