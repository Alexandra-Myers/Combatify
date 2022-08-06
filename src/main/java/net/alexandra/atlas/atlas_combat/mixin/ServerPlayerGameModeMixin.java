package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.item.NewAttributes;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
	@Redirect(method = "handleBlockBreakAction",
			at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;MAX_INTERACTION_DISTANCE:D",opcode = Opcodes.GETSTATIC))
	public double getActualReachDistance() {
		return Mth.square(((ServerGamePacketListenerImpl) (Object)this).player.getAttribute(NewAttributes.BASE_REACH).getValue());
	}
}
