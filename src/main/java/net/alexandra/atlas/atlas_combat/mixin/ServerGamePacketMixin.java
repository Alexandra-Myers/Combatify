package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.item.NewAttributes;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
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
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketMixin {
	@Shadow
	public ServerPlayer player;
	public double var = player != null ? player.getAttribute(NewAttributes.ATTACK_REACH).getValue() : 2.5;
	public double var2 = player != null ? player.getAttribute(NewAttributes.BASE_REACH).getValue() : 6.0;
	@Redirect(
			method = "handleInteract",
			at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;MAX_INTERACTION_DISTANCE:D", opcode = Opcodes.GETSTATIC))
	private double getActualAttackRange() {
		return var;
	}

	@Redirect(
			method = "handleUseItemOn",
			at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;MAX_INTERACTION_DISTANCE:D", opcode = Opcodes.GETSTATIC))
	private double getActualReachDistance() {
		return var2;
	}
}
