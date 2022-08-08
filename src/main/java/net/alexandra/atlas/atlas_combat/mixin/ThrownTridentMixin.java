package net.alexandra.atlas.atlas_combat.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownTrident.class)
public class ThrownTridentMixin {

	@Shadow
	@Final
	private static EntityDataAccessor<Byte> ID_LOYALTY;

	@Inject(method = "tick", at = @At(value = "HEAD"))
	public void injectVoidReturnLogic(CallbackInfo ci) {
		ThrownTrident trident = ((ThrownTrident) (Object)this);
		int j = trident.entityData.get(ID_LOYALTY);
		if(trident.getY() <= -65) {
			if (!trident.isAcceptibleReturnOwner()) {
				trident.discard();
			}else {
				trident.setNoPhysics(true);
				Vec3 vec3 = trident.getEyePosition().subtract(trident.position());
				trident.setPosRaw(trident.getX(), trident.getY() + vec3.y * 0.015 * j, trident.getZ());
				if (trident.level.isClientSide) {
					trident.yOld = trident.getY();
				}

				double d = 0.05 * j;
				trident.setDeltaMovement(trident.getDeltaMovement().scale(0.95).add(vec3.normalize().scale(d)));
				if (trident.clientSideReturnTridentTickCount == 0) {
					trident.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
				}

				++trident.clientSideReturnTridentTickCount;
			}
		}
	}

}
