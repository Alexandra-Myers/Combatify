package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.*;
import net.atlas.combatify.util.CombatUtil;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends PlayerMixin implements ServerPlayerExtensions {
	@Unique
	private boolean retainAttack;

	@Shadow
	public abstract void swing(InteractionHand interactionHand);

	@Shadow
	public ServerGamePacketListenerImpl connection;

	@Shadow
	public abstract Entity getCamera();

	@Unique
	public final ServerPlayer player = ServerPlayer.class.cast(this);

	public ServerPlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "tick", at = @At(value = "HEAD"))
	public void hitreg(CallbackInfo ci) {
		CombatUtil.setPosition((ServerPlayer)(Object)this);
		if (((PlayerExtensions) this.player).isAttackAvailable(-1.0F) && retainAttack && Combatify.unmoddedPlayers.contains(getUUID())) {
			retainAttack = false;
			Entity entity = getCamera();
			if (entity == null)
				entity = this.player;
			Vec3 eyePos = entity.getEyePosition(1.0f);
			Vec3 viewVector = entity.getViewVector(1.0f);
			double reach = entityInteractionRange();
			double sqrReach = reach * reach;
			Vec3 adjPos = eyePos.add(viewVector.x * reach, viewVector.y * reach, viewVector.z * reach);
			AABB rayBB = entity.getBoundingBox().expandTowards(viewVector.scale(reach)).inflate(1.0, 1.0, 1.0);
			HitResult hitResult = entity.pick(reach, 1.0f, false);
			double i = hitResult.getLocation().distanceToSqr(eyePos);
			EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(entity, eyePos, adjPos, rayBB, (entityx) -> !entityx.isSpectator() && entityx.isPickable(), sqrReach);
			if (entityHitResult != null && entityHitResult.getLocation().distanceToSqr(eyePos) < i)
				hitResult = entityHitResult;
			else
				MethodHandler.redirectResult(player, hitResult);
			if (hitResult.getType() == HitResult.Type.ENTITY)
				connection.handleInteract(ServerboundInteractPacket.createAttackPacket(((EntityHitResult)hitResult).getEntity(), false));
		}
	}
	@Inject(method = "swing", at = @At(value = "HEAD"), cancellable = true)
	public void removeReset(InteractionHand hand, CallbackInfo ci) {
		super.swing(hand);
		if (Combatify.unmoddedPlayers.contains(getUUID())) {
			if (Combatify.isPlayerAttacking.get(getUUID())) {
				handleInteract(false);
			}
			Combatify.isPlayerAttacking.put(getUUID(), true);
		}
		ci.cancel();
	}
	@Unique
	public void handleInteract(boolean hit) {
		if (retainAttack) {
			if(hit)
				this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, this.getSoundSource(), 1.0F, 1.0F);
			return;
		}
		if (!isAttackAvailable(0.0F)) {
			float var1 = this.player.getAttackStrengthScale(0.0F);
			if (var1 < 0.8F) {
				if(hit)
					this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, this.getSoundSource(), 1.0F, 1.0F);
				resetAttackStrengthTicker(!getMissedAttackRecovery());
				return;
			}

			if (var1 < 1.0F) {
				retainAttack = true;
				return;
			}
		}
		attackAir();
	}
	@Inject(method = "updatePlayerAttributes", at = @At("HEAD"), cancellable = true)
	public void removeCreativeReach(CallbackInfo ci) {
		ci.cancel();
	}
}
