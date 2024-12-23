package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
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
		if (!(player.combatify$isAttackAvailable(1.0F)))
			ci.cancel();
		if (Combatify.unmoddedPlayers.contains(player.getUUID())) {
			if (player.combatify$isRetainingAttack()) {
				player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, player.getSoundSource(), 1.0F, 1.0F);
				ci.cancel();
				return;
			}
			if (!player.combatify$isAttackAvailable(0.0F)) {
				float var1 = player.getAttackStrengthScale(0.0F);
				if (var1 < 0.8F) {
					player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, player.getSoundSource(), 1.0F, 1.0F);
					player.combatify$resetAttackStrengthTicker(!player.combatify$getMissedAttackRecovery());
					ci.cancel();
				}

				if (var1 < 1.0F) {
					player.combatify$setRetainAttack(true);
					ci.cancel();
				}
			}
		}
	}

	@WrapOperation(method = "handleInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;canInteractWithEntity(Lnet/minecraft/world/phys/AABB;D)Z"))
	public boolean redirectCheck(ServerPlayer instance, AABB aabb, double v, Operation<Boolean> original, @Local(ordinal = 0) Entity entity) {
		if (entity instanceof ServerPlayer target)
            return CombatUtil.allowReach(player, target);
		return original.call(instance, aabb, v);
	}

}
