package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.extensions.IUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerEntity.class)
public class ServerEntityMixin {
	@Shadow
	@Final
	private Entity entity;

	@ModifyExpressionValue(method = "addPairing", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundBundlePacket;<init>(Ljava/lang/Iterable;)V"))
	public ClientboundBundlePacket modifyAttributes(ClientboundBundlePacket original, @Local(ordinal = 0) ServerPlayer serverPlayer) {
		original.subPackets().forEach(clientGamePacketListenerPacket -> {
			if(clientGamePacketListenerPacket instanceof ClientboundUpdateAttributesPacket clientboundUpdateAttributesPacket) {
				((IUpdateAttributesPacket) clientboundUpdateAttributesPacket).changeAttributes(serverPlayer);
			}
		});
		return original;
	}
	@ModifyExpressionValue(method = "sendDirtyEntityData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundUpdateAttributesPacket;<init>(ILjava/util/Collection;)V"))
	public ClientboundUpdateAttributesPacket modifyAttributes1(ClientboundUpdateAttributesPacket original) {
		if(entity instanceof ServerPlayer serverPlayer)
			((IUpdateAttributesPacket) original).changeAttributes(serverPlayer);
		return original;
	}
}
