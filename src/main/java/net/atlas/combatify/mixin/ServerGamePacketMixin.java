package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.PlayerExtensions;
import net.atlas.combatify.extensions.ServerPlayerExtensions;
import net.atlas.combatify.util.CombatUtil;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketMixin {
	@Shadow
	public ServerPlayer player;

	@Inject(method = "handleInteract", at = @At(value = "HEAD"), cancellable = true)
	public void injectPlayer(ServerboundInteractPacket packet, CallbackInfo ci) {
		if (!(((PlayerExtensions) player).isAttackAvailable(1.0F)))
			ci.cancel();
		if (Combatify.unmoddedPlayers.contains(player.getUUID())) {
			if (((ServerPlayerExtensions)player).isRetainingAttack()) {
				player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, player.getSoundSource(), 1.0F, 1.0F);
				ci.cancel();
			}
			if (!((PlayerExtensions) player).isAttackAvailable(0.0F)) {
				float var1 = player.getAttackStrengthScale(0.0F);
				if (var1 < 0.8F) {
					player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, player.getSoundSource(), 1.0F, 1.0F);
					((PlayerExtensions) player).resetAttackStrengthTicker(!((PlayerExtensions) player).getMissedAttackRecovery());
					ci.cancel();
				}

				if (var1 < 1.0F) {
					((ServerPlayerExtensions) player).setRetainAttack(true);
					ci.cancel();
				}
			}
		}
	}

	@Redirect(method = "handleInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;canInteractWithEntity(Lnet/minecraft/world/phys/AABB;D)Z"))
	public boolean redirectCheck(ServerPlayer instance, AABB aabb, double v, @Local(ordinal = 0) Entity entity) {
		if (entity instanceof ServerPlayer target) {
            return CombatUtil.allowReach(player, target);
		}
//		double d = MethodHandler.getCurrentAttackReach(player, 1.0F) + 1;
//		d *= d;
//		if(!player.hasLineOfSight(entity)) {
//			d = 6.25;
//		}
		// If target is not a player do vanilla code
//		Vec3 vec3 = player.getEyePosition();
//		return vec3.distanceToSqr(MethodHandler.getNearestPointTo(aabb, vec3)) <= d;
		return instance.canInteractWithEntity(aabb, 1);
	}

}
