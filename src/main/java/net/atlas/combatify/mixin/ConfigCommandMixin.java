package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.atlascore.AtlasCore;
import net.atlas.atlascore.command.ConfigCommand;
import net.atlas.atlascore.config.AtlasConfig;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.networking.NetworkingHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ConfigCommand.class)
public class ConfigCommandMixin {
	@Redirect(method = "resetConfig", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
	private static void removeForUnmodded(PlayerList instance, Packet<?> packet, @Local(ordinal = 0, argsOnly = true) AtlasConfig atlasConfig) {
		for (ServerPlayer player : instance.getPlayers()) {
			if (!ServerPlayNetworking.canSend(player, AtlasCore.AtlasConfigPacket.TYPE)) return;
			if (!ServerPlayNetworking.canSend(player, NetworkingHandler.RemainingUseSyncPacket.TYPE) && atlasConfig.name.getNamespace().equals(Combatify.MOD_ID)) return;
			player.connection.send(packet);
		}
	}
}
