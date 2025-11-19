package net.atlas.combatify.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
	@Shadow
	@Final
	protected ServerPlayer player;

	@Inject(method = "handleBlockBreakAction", at = @At(value = "HEAD"))
	public void addReset(BlockPos blockPos, ServerboundPlayerActionPacket.Action action, Direction direction, int i, int j, CallbackInfo ci) {
		if (!this.player.isWithinBlockInteractionRange(blockPos, 1.0)) return;
		if (blockPos.getY() > i) return;
		player.combatify$resetAttackStrengthTicker(false);
	}
}
