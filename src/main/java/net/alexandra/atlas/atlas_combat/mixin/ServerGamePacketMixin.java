package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.extensions.ItemExtensions;
import net.alexandra.atlas.atlas_combat.extensions.PlayerExtensions;
import net.alexandra.atlas.atlas_combat.item.NewAttributes;
import net.alexandra.atlas.atlas_combat.item.WeaponType;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
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
	public static double MAX_INTERACTION_DISTANCE;

	@Redirect(method = "handleInteract",
			at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;MAX_INTERACTION_DISTANCE:D",opcode = Opcodes.GETSTATIC))
	public double getActualAttackRange() {
		return ((PlayerExtensions)player).getSquaredAttackRange(player, MAX_INTERACTION_DISTANCE);
	}

	@Redirect(
			method = "handleUseItemOn",
			at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;MAX_INTERACTION_DISTANCE:D",opcode = Opcodes.GETSTATIC))
	private double getActualReachDistance() {
		return ((PlayerExtensions)player).getSquaredReach(player, MAX_INTERACTION_DISTANCE);
	}
	@Inject(method = "handleInteract",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ServerboundInteractPacket;getTarget(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;"), cancellable = true)
	public void inject(ServerboundInteractPacket packet, CallbackInfo ci) {
		final Entity entity1 = packet.getTarget(player.getLevel());
		if(entity1 == null) {
			((PlayerExtensions)player).attackAir();
			ci.cancel();
		}
	}
	@ModifyConstant(
			method = "handleUseItemOn",
			require = 1, allow = 1, constant = @Constant(doubleValue = 64.0))
	private double getActualReachDistance(final double reachDistance) {
		return ((PlayerExtensions)player).getSquaredReach(player, reachDistance);
	}
}
