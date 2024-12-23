package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerEntity.class)
public abstract class ServerEntityMixin {
	@Shadow
	@Final
	private Entity entity;

	@WrapOperation(method = "addPairing", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
	public void modifyAttributes(ServerGamePacketListenerImpl instance, Packet<?> packet, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) ServerPlayer serverPlayer) {
		if (packet instanceof ClientboundBundlePacket clientboundBundlePacket)
			clientboundBundlePacket.subPackets().forEach(clientGamePacketListenerPacket -> {
				if(Combatify.unmoddedPlayers.contains(serverPlayer.getUUID()) && clientGamePacketListenerPacket instanceof ClientboundUpdateAttributesPacket clientboundUpdateAttributesPacket) {
					clientboundUpdateAttributesPacket.combatify$changeAttributes(serverPlayer);
				}
			});
		original.call(instance, packet);
	}
	@WrapOperation(method = "sendDirtyEntityData", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerEntity;broadcastAndSend(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 1))
	public void modifyAttributes1(ServerEntity instance, Packet<?> packet, Operation<Void> original) {
		if(entity instanceof ServerPlayer serverPlayer && Combatify.unmoddedPlayers.contains(serverPlayer.getUUID()) && packet instanceof ClientboundUpdateAttributesPacket clientboundUpdateAttributesPacket)
			clientboundUpdateAttributesPacket.combatify$changeAttributes(serverPlayer);
		original.call(instance, packet);
	}
}
