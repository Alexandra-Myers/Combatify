package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.extensions.PlayerExtensions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
	@Final
	@Shadow
	protected ServerPlayer player;
	@ModifyConstant(
			method = "handleBlockBreakAction",
			require = 1, allow = 1, constant = @Constant(doubleValue = 36.0))
	public double getActualReachDistance(double constant) {
		return ((PlayerExtensions)player).getSquaredReach(player, constant);
	}
}
