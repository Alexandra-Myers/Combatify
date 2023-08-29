package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.AABBExtensions;
import net.atlas.combatify.extensions.PlayerExtensions;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketMixin {
	@Shadow
	public ServerPlayer player;

	@Inject(method = "handleInteract", at = @At(value = "HEAD"))
	public void injectPlayer(ServerboundInteractPacket packet, CallbackInfo ci) {
		Combatify.player = player;
	}

	@Redirect(method = "handleInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D"))
	public double redirectCheck(AABB instance, Vec3 old) {
		Vec3 vec3 = player.getEyePosition(0.0F);
		return vec3.distanceToSqr(((AABBExtensions)instance).getNearestPointTo(vec3));
	}
	@ModifyExpressionValue(method = "handleInteract",
			at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;MAX_INTERACTION_DISTANCE:D",opcode = Opcodes.GETSTATIC))
	public double getActualAttackRange(double original, @Local(ordinal = 0) Entity entity) {
		double d = ((PlayerExtensions)player).getAttackRange(1.0F) + 1;
		d *= d;
		if(!player.hasLineOfSight(entity)) {
			d = 6.25;
		}
		return d;
	}
}