package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.config.ConfigHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Snowball;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collections;

@Mixin(Snowball.class)
public class SnowballMixin {

	@Unique
	public final float snowballDamage = ConfigHelper.snowballDamage;


	@Redirect(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
	public boolean redirectDamage(Entity instance, DamageSource source, float amount) {
		return instance.hurt(DamageSource.thrown(((Snowball) (Object)this), ((Snowball) (Object)this).getOwner()), snowballDamage);
	}

}
