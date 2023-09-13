package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.AABBExtensions;
import net.atlas.combatify.extensions.PlayerExtensions;
import net.atlas.combatify.extensions.ServerPlayerExtensions;
import net.atlas.combatify.util.CombatUtil;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPongPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketMixin {
	@Shadow
	public ServerPlayer player;
	@Unique Entity targetEntity;

	@Inject(method = "handleInteract", at = @At(value = "HEAD"), cancellable = true)
	public void injectPlayer(ServerboundInteractPacket packet, CallbackInfo ci) {
		if (!(((PlayerExtensions) player).isAttackAvailable(1.0F)))
			ci.cancel();
		Combatify.player = player;
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

	@Redirect(method = "handleInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D"))
	public double redirectCheck(AABB instance, Vec3 old) {
		if (targetEntity instanceof ServerPlayer target) {
			if (CombatUtil.allowReach(player, target)) {
				return 0;
			} else {
				return Integer.MAX_VALUE;
			}
		}
		// If target is not a player do vanilla code
		Vec3 vec3 = player.getEyePosition(0.0F);
		return vec3.distanceToSqr(((AABBExtensions)instance).getNearestPointTo(vec3));
	}
	@SuppressWarnings("unused")
	@ModifyExpressionValue(method = "handleInteract",
			at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;MAX_INTERACTION_DISTANCE:D",opcode = Opcodes.GETSTATIC))
	public double getActualAttackRange(double original, @Local(ordinal = 0) Entity entity) {
		double d = ((PlayerExtensions)player).getCurrentAttackReach(1.0F) + 1;
		d *= d;
		if(!player.hasLineOfSight(entity)) {
			d = 6.25;
		}

		return d;
	}
	/**
	 *  Credits to <a href="https://github.com/Blumbo/CTS-AntiCheat/tree/master">Blumbo's CTS Anti-Cheat</a>, integrated into Combatify from there <br>
	 *  <h4>Licensed under MIT</h4> <br>
	 *  Stores the target for use later.
	 */
	@Inject(method = "handleInteract", at = @At("HEAD"))
	private void handleInteract(ServerboundInteractPacket packet, CallbackInfo ci) {
		targetEntity = packet.getTarget(player.serverLevel());
	}
	@Inject(method = "handlePong", at = @At(value = "HEAD"))
	public void getPing(ServerboundPongPacket serverboundPongPacket, CallbackInfo ci) {
		if (serverboundPongPacket.getId() == 3492 && Combatify.unmoddedPlayers.contains(player.getUUID()) && ((ServerPlayerExtensions)player).isAwaitingResponse()) {
			((ServerPlayerExtensions) player).setAwaitingResponse(false);
		}
	}

}
