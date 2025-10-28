package net.atlas.combatify.mixin;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.*;
import net.atlas.combatify.util.CombatUtil;
import net.atlas.combatify.util.HitResultRotationEntry;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPingPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.atlas.combatify.Combatify.scheduleHitResult;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends PlayerMixin implements ServerPlayerExtensions {

	private boolean retainAttack;

	@Shadow
	public abstract Entity getCamera();

	@Shadow
	public abstract void swing(InteractionHand interactionHand);
	@Shadow
	public ServerGamePacketListenerImpl connection;
	public CopyOnWriteArrayList<HitResultRotationEntry> oldHitResults = new CopyOnWriteArrayList<>();
	public IntList pastPings = new IntArrayList(5);
	public boolean awaitingResponse = false;
	public int responseTimer = 0;
	public int tickTimer = 4;
	public int currentAveragePing = 0;

	@Unique
	public final ServerPlayer player = ServerPlayer.class.cast(this);

	@Unique
	public boolean shouldInit = true;

	public ServerPlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "tick", at = @At(value = "HEAD"))
	public void hitreg(CallbackInfo ci) {
		CombatUtil.setPosition((ServerPlayer)(Object)this);
		if (shouldInit && Combatify.unmoddedPlayers.contains(getUUID())) {
			scheduleHitResult.get(getUUID()).schedule(new TimerTask() {
				@Override
				public void run() {
					Entity camera = getCamera();
					if (camera != null) {
						adjustHitResults(MethodHandler.pickResult(player, camera));
					}
				}
			}, 0, 5);
			shouldInit = false;
		}
		tickTimer++;
		if(tickTimer >= 5 && Combatify.unmoddedPlayers.contains(getUUID()) && !awaitingResponse) {
			tickTimer = 0;
			connection.send(new ClientboundPingPacket(3492));
			awaitingResponse = true;
		}
		if (((PlayerExtensions) this.player).combatify$isAttackAvailable(-1.0F) && retainAttack && Combatify.unmoddedPlayers.contains(getUUID())) {
			retainAttack = false;
			swing(InteractionHand.MAIN_HAND);
		}
	}
	@Inject(method = "swing", at = @At(value = "HEAD"), cancellable = true)
	public void removeReset(InteractionHand hand, CallbackInfo ci) {
		super.swing(hand);
		if(Combatify.unmoddedPlayers.contains(getUUID())) {
			if (Combatify.isPlayerAttacking.get(getUUID())) {
				getPresentResult();
				float xRot = getXRot();
				float yRot = getYHeadRot();
				HitResult hitResult = oldHitResults.stream().filter(hitResultRotEntry -> hitResultRotEntry.shouldAccept(xRot, yRot))
					.min((firstResultRotEntry, secondResultRotEntry) -> firstResultRotEntry.compareTo(secondResultRotEntry, xRot, yRot)).map(HitResultRotationEntry::hitResult).orElse(null);
				if (hitResult != null) {
					Combatify.finalizingAttack.put(getUUID(), false);
					switch (hitResult.getType()) {
						case ENTITY:
							if (hitResult instanceof EntityHitResult) {
								Entity entity = ((EntityHitResult) hitResult).getEntity();
								handleInteract(entity, true);
							}
						case MISS:
							handleInteract(player, false);
					}
				}
			}
			Combatify.finalizingAttack.put(getUUID(), true);
			Combatify.isPlayerAttacking.put(getUUID(), true);
		}
		ci.cancel();
	}
	@Unique
	public void handleInteract(Entity entity, boolean hit) {
		if (retainAttack) {
			if(hit)
				this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, this.getSoundSource(), 1.0F, 1.0F);
			return;
		}
		if (!combatify$isAttackAvailable(0.0F)) {
			float var1 = this.player.getAttackStrengthScale(0.0F);
			if (var1 < 0.8F) {
				if(hit)
					this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, this.getSoundSource(), 1.0F, 1.0F);
				combatify$resetAttackStrengthTicker(!combatify$getMissedAttackRecovery());
				return;
			}

			if (var1 < 1.0F) {
				retainAttack = true;
				return;
			}
		}
		final ServerLevel serverLevel = this.player.serverLevel();
		this.player.resetLastActionTime();
		if (entity != null) {
			if (!serverLevel.getWorldBorder().isWithinBounds(entity.blockPosition())) {
				return;
			}
			double d = MethodHandler.getCurrentAttackReach(player, 1.0F) + 1;
			d *= d;

			AABB aABB = entity.getBoundingBox();
			Vec3 eyePos = player.getEyePosition(0.0F);
			double dist = eyePos.distanceToSqr(MethodHandler.getNearestPointTo(aABB, eyePos));
			if (entity instanceof ServerPlayer target) {
				if (CombatUtil.allowReach(player, target)) {
					dist = 0;
				} else {
					dist = Integer.MAX_VALUE;
				}
			}
			if (dist < d) {
				if(hit) {
					if (!(entity instanceof ItemEntity) && !(entity instanceof ExperienceOrb) && !(entity instanceof AbstractArrow) && entity != player) {
						ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
						if (itemStack.isItemEnabled(serverLevel.enabledFeatures())) {
							player.attack(entity);
						}
					} else {
						player.connection.disconnect(Component.translatable("multiplayer.disconnect.invalid_entity_attacked"));
						Combatify.LOGGER.warn("Player " + player.getName().getString() + " tried to attack an invalid entity");
					}
				} else {
					combatify$attackAir();
				}
			}
		}

	}
	@Override
	public void adjustHitResults(HitResult newValue) {
		if (awaitingResponse)
			responseTimer++;
		if (!awaitingResponse && responseTimer > 0) {
			int newPing = Mth.ceil(responseTimer * 0.5);
			pastPings.add(0, newPing);
			int removed = pastPings.removeInt(pastPings.size() - 1);
			responseTimer = 0;
			currentAveragePing = currentAveragePing - (removed / 5) + (newPing / 5);
		}
		HitResultRotationEntry newEntry = new HitResultRotationEntry(newValue, getXRot(), getYHeadRot());
		if (oldHitResults.size() > 1)
			oldHitResults.add(1, newEntry);
		else
			oldHitResults.add(newEntry);
		int currentPing = Mth.clamp(currentAveragePing + 1, 25, 200) / 5;
		oldHitResults.removeIf(hitResult -> oldHitResults.indexOf(hitResult) >= currentPing);
	}

	@Override
	public void setAwaitingResponse(boolean awaitingResponse) {
		this.awaitingResponse = awaitingResponse;
	}

	@Override
	public boolean isAwaitingResponse() {
		return awaitingResponse;
	}

	@Override
	public CopyOnWriteArrayList<HitResultRotationEntry> getOldHitResults() {
		return oldHitResults;
	}

	@Override
	public boolean isRetainingAttack() {
		return retainAttack;
	}

	@Override
	public void setRetainAttack(boolean retain) {
		retainAttack = retain;
	}

	@Override
	public void getPresentResult() {
		Entity camera = getCamera();
		if (camera != null) {
			oldHitResults.set(0, new HitResultRotationEntry(MethodHandler.pickResult(player, camera), getXRot(), getYHeadRot()));
		}
	}
}
