package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.atlascore.command.ConfigCommand;
import net.atlas.atlascore.config.AtlasConfig;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.networking.NetworkingHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ConfigCommand.class)
public class ConfigCommandMixin {
	@WrapOperation(method = "resetConfig", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
	private static void removeForUnmodded(ServerGamePacketListenerImpl instance, Packet<?> packet, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) AtlasConfig atlasConfig) {
		if (!ServerPlayNetworking.canSend(instance.getPlayer(), NetworkingHandler.RemainingUseSyncPacket.TYPE) && atlasConfig.name.getNamespace().equals(Combatify.MOD_ID)) return;
		original.call(instance, packet);
	}
	@WrapOperation(method = "resetConfigValue", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
	private static void removeForUnmodded0(ServerGamePacketListenerImpl instance, Packet<?> packet, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) AtlasConfig atlasConfig) {
		if (!ServerPlayNetworking.canSend(instance.getPlayer(), NetworkingHandler.RemainingUseSyncPacket.TYPE) && atlasConfig.name.getNamespace().equals(Combatify.MOD_ID)) return;
		original.call(instance, packet);
	}
	@WrapOperation(method = "updateConfigValue", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
	private static void removeForUnmodded1(ServerGamePacketListenerImpl instance, Packet<?> packet, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) AtlasConfig atlasConfig) {
		if (!ServerPlayNetworking.canSend(instance.getPlayer(), NetworkingHandler.RemainingUseSyncPacket.TYPE) && atlasConfig.name.getNamespace().equals(Combatify.MOD_ID)) return;
		original.call(instance, packet);
	}
}
