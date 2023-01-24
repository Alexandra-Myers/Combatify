package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin {
	@Shadow
	@Nullable
	public abstract Player getPlayerOwner();

	@Inject(method = "onHitEntity", at = @At(value = "TAIL"))
	protected void onHitEntity(EntityHitResult entityHitResult, CallbackInfo ci) {
		if(AtlasCombat.CONFIG.fishingHookKB() && entityHitResult.getEntity() instanceof LivingEntity livingEntity)
			livingEntity.hurt(DamageSource.thrown(((FishingHook) (Object) this), getPlayerOwner()), 0);
	}
}
