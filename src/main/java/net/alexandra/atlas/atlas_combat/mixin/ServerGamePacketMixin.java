package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.extensions.PlayerExtensions;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketMixin {
	@Shadow
	public ServerPlayer player;
	@Shadow
	@Final
	private MinecraftServer server;
	@Unique
	ServerGamePacketListenerImpl thisListener = ((ServerGamePacketListenerImpl)(Object)this);

	@Inject(method = "handleInteract", at = @At(value = "HEAD"))
	public void injectPlayer(ServerboundInteractPacket packet, CallbackInfo ci) {
		AtlasCombat.player = player;
	}

	@ModifyConstant(method = "handleInteract",
			constant = @Constant(doubleValue = 36.0))
	public double getActualAttackRange(double constant) {
		return ((PlayerExtensions)player).getSquaredAttackRange(player, 30);
	}
	@ModifyConstant(
			method = "handleUseItemOn",
			require = 1, allow = 1, constant = @Constant(doubleValue = 64.0))
	private double getActualReachDistance(final double reachDistance) {
		return ((PlayerExtensions)player).getSquaredReach(player, reachDistance);
	}
}
