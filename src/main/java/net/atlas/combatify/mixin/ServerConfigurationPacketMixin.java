package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.atlas.combatify.extensions.ClientInformationHolder;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.config.PrepareSpawnTask;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class ServerConfigurationPacketMixin implements ClientInformationHolder {
	@Shadow
	@Nullable
	private PrepareSpawnTask prepareSpawnTask;
	@Unique
	public boolean hasShieldOnCrouch = true;

	@WrapOperation(method = "handleConfigurationFinished", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/config/PrepareSpawnTask;spawnPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/network/CommonListenerCookie;)Lnet/minecraft/server/level/ServerPlayer;"))
	public ServerPlayer injectSettingShieldOnCrouch(PrepareSpawnTask instance, Connection connection, CommonListenerCookie commonListenerCookie, Operation<ServerPlayer> original) {
		ServerPlayer res = original.call(prepareSpawnTask, connection, commonListenerCookie);
		res.combatify$setShieldOnCrouch(hasShieldOnCrouch);
		return res;
	}

	@Override
	public void combatify$setShieldOnCrouch(boolean hasShieldOnCrouch) {
		this.hasShieldOnCrouch = hasShieldOnCrouch;
	}
}
