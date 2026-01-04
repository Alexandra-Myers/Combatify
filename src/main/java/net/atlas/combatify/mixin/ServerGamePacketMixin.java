package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.util.CombatUtil;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
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
		if (!(player.combatify$isAttackAvailable(1.0F, player.getItemInHand(InteractionHand.MAIN_HAND))))
			ci.cancel();
		if (Combatify.unmoddedPlayers.contains(player.getUUID())) {
			if (player.combatify$isRetainingAttack()) {
				player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, player.getSoundSource(), 1.0F, 1.0F);
				ci.cancel();
				return;
			}
			if (!player.combatify$isAttackAvailable(0.0F, player.getItemInHand(InteractionHand.MAIN_HAND))) {
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

	@WrapOperation(method = "handleInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ServerboundInteractPacket;isWithinRange(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/phys/AABB;D)Z"))
	public boolean redirectCheck(ServerboundInteractPacket instance, ServerPlayer serverPlayer, AABB aABB, double d, Operation<Boolean> original, @Local(ordinal = 0) Entity entity) {
		boolean result;
		if (entity instanceof ServerPlayer target) result = CombatUtil.allowReach(player, target, instance::isWithinRange);
		else result = original.call(instance, serverPlayer, aABB, d);
		if (Combatify.unmoddedPlayers.contains(player.getUUID()) && !result) player.combatify$attackAir();
		return result;
	}

	@Inject(method = "tryPickItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/InventoryMenu;broadcastChanges()V", shift = At.Shift.AFTER))
	public void tryPickItem(ItemStack itemStack, CallbackInfo ci) {
		MethodHandler.forceUpdateItems(player, false);
	}

	@Inject(method = "handlePlayerAction", slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isSpectator()Z", ordinal = 1)), at = @At("RETURN"))
	public void handlePlayerAction(ServerboundPlayerActionPacket serverboundPlayerActionPacket, CallbackInfo ci) {
		MethodHandler.forceUpdateItems(player, false);
	}

	@Inject(method = "handleUseItemOn", at = @At("TAIL"))
	public void handleUseItemOn(ServerboundUseItemOnPacket serverboundUseItemOnPacket, CallbackInfo ci) {
		MethodHandler.forceUpdateItems(player, false);
	}

	@Inject(method = "handleSetCarriedItem", at = @At("RETURN"))
	public void handleSetCarriedItem(ServerboundSetCarriedItemPacket packet, CallbackInfo ci) {
		MethodHandler.forceUpdateItems(player, false);
	}

	@Inject(method = "handleInteract", at = @At("TAIL"))
	public void handleInteract(ServerboundInteractPacket serverboundInteractPacket, CallbackInfo ci) {
		MethodHandler.forceUpdateItems(player, false);
	}

	@Inject(method = "handleContainerClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;resumeRemoteUpdates()V", shift = At.Shift.AFTER))
	public void handleContainerClick(ServerboundContainerClickPacket serverboundContainerClickPacket, CallbackInfo ci) {
		MethodHandler.forceUpdateItems(player, false);
	}

	@Inject(method = "handleContainerButtonClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;clickMenuButton(Lnet/minecraft/world/entity/player/Player;I)Z", shift = At.Shift.AFTER))
	public void handleContainerButtonClick(ServerboundContainerButtonClickPacket serverboundContainerButtonClickPacket, CallbackInfo ci) {
		MethodHandler.forceUpdateItems(player, false);
	}

	@Inject(method = "handleSetCreativeModeSlot", at = @At("TAIL"))
	public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket serverboundSetCreativeModeSlotPacket, CallbackInfo ci) {
		MethodHandler.forceUpdateItems(player, false);
	}
}
