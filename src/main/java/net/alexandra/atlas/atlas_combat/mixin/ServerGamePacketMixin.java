package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.extensions.IHandler;
import net.alexandra.atlas.atlas_combat.extensions.ItemExtensions;
import net.alexandra.atlas.atlas_combat.extensions.PlayerExtensions;
import net.alexandra.atlas.atlas_combat.item.NewAttributes;
import net.alexandra.atlas.atlas_combat.item.WeaponType;
import net.alexandra.atlas.atlas_combat.networking.NewServerboundInteractPacket;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
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
	@Shadow
	@Final
	private MinecraftServer server;
	@Unique
	ServerGamePacketListenerImpl thisListener = ((ServerGamePacketListenerImpl)(Object)this);

	@Redirect(method = "handleInteract",
			at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;MAX_INTERACTION_DISTANCE:D",opcode = Opcodes.GETSTATIC))
	public double getActualAttackRange() {
		return ((PlayerExtensions)player).getSquaredAttackRange(player, 30);
	}

	@Redirect(
			method = "handleUseItemOn",
			at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;MAX_INTERACTION_DISTANCE:D",opcode = Opcodes.GETSTATIC))
	private double getActualReachDistance() {
		return ((PlayerExtensions)player).getSquaredReach(player, MAX_INTERACTION_DISTANCE);
	}
	@ModifyConstant(
			method = "handleUseItemOn",
			require = 1, allow = 1, constant = @Constant(doubleValue = 64.0))
	private double getActualReachDistance(final double reachDistance) {
		return ((PlayerExtensions)player).getSquaredReach(player, reachDistance);
	}
}
