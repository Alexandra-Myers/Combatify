package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.extensions.ClientInformationHolder;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public class ServerConfigurationPacketMixin implements ClientInformationHolder {
	@Unique
	public boolean hasShieldOnCrouch = true;

	@Inject(method = "handleConfigurationFinished", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;placeNewPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/network/CommonListenerCookie;)V"))
	public void injectSettingShieldOnCrouch(ServerboundFinishConfigurationPacket serverboundFinishConfigurationPacket, CallbackInfo ci, @Local(ordinal = 0) ServerPlayer newPlayer) {
		newPlayer.combatify$setShieldOnCrouch(hasShieldOnCrouch);
	}

	@Override
	public void combatify$setShieldOnCrouch(boolean hasShieldOnCrouch) {
		this.hasShieldOnCrouch = hasShieldOnCrouch;
	}
}
