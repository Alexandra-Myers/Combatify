package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.extensions.IUpdateAttributesPacket;
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
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerEntity.class)
public abstract class ServerEntityMixin {
	@Shadow
	@Final
	private Entity entity;

	@Shadow
	protected abstract void broadcastAndSend(Packet<?> packet);

	@Redirect(method = "addPairing", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
	public void modifyAttributes(ServerGamePacketListenerImpl instance, Packet<?> packet, @Local(ordinal = 0) ServerPlayer serverPlayer) {
		if(packet instanceof ClientboundBundlePacket clientboundBundlePacket)
			clientboundBundlePacket.subPackets().forEach(clientGamePacketListenerPacket -> {
				if(clientGamePacketListenerPacket instanceof ClientboundUpdateAttributesPacket clientboundUpdateAttributesPacket) {
					((IUpdateAttributesPacket) clientboundUpdateAttributesPacket).changeAttributes(serverPlayer);
				}
			});
		instance.send(packet);
	}
	@Redirect(method = "sendDirtyEntityData", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerEntity;broadcastAndSend(Lnet/minecraft/network/protocol/Packet;)V"))
	public void modifyAttributes1(ServerEntity instance, Packet<?> packet) {
		if(entity instanceof ServerPlayer serverPlayer && packet instanceof ClientboundUpdateAttributesPacket clientboundUpdateAttributesPacket)
			((IUpdateAttributesPacket) clientboundUpdateAttributesPacket).changeAttributes(serverPlayer);
		broadcastAndSend(packet);
	}
}
