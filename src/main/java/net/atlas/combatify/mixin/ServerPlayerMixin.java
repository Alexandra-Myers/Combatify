package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.*;
import net.atlas.combatify.util.CombatUtil;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.atlas.combatify.Combatify.scheduleHitResult;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends PlayerMixin implements ServerPlayerExtensions {

	@Unique
	private boolean retainAttack;

	@Shadow
	public abstract Entity getCamera();

	@Shadow
	public abstract void swing(InteractionHand interactionHand);
	@Shadow
	public ServerGamePacketListenerImpl connection;
	@Unique
	public CopyOnWriteArrayList<HitResult> oldHitResults = new CopyOnWriteArrayList<>();
	@Unique
	public Map<HitResult, Float[]> hitResultToRotationMap = new ConcurrentHashMap<>();
	@Unique
	public ArrayList<Integer> pastPings = new ArrayList<>();
	@Unique
	public boolean awaitingResponse = false;
	@Unique
	public int responseTimer = 0;
	@Unique
	public int tickTimer = 4;
	@Unique
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
			pastPings.add(0);
			pastPings.add(0);
			pastPings.add(0);
			pastPings.add(0);
			pastPings.add(0);
			scheduleHitResult.get(getUUID()).schedule(new TimerTask() {
				@Override
				public void run() {
					Entity camera = getCamera();
					if (camera != null) {
						adjustHitResults(MethodHandler.pickResult(player, camera));
					}
				}
			}, 0, 1);
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
				HitResult hitResult = null;
				getPresentResult();
				for (HitResult hitResultToChoose : oldHitResults) {
					if(hitResultToChoose == null)
						continue;
					Float[] rotations = null;
					if (hitResultToRotationMap.containsKey(hitResultToChoose))
						rotations = hitResultToRotationMap.get(hitResultToChoose);
					float xRot = getXRot() % 360;
					float yRot = getYHeadRot() % 360;
					if(rotations != null) {
						float xDiff = Math.abs(xRot - rotations[1]);
						float yDiff = Math.abs(yRot - rotations[0]);
						if(xDiff > 20 || yDiff > 20)
							continue;
					}
					if (hitResultToChoose.getType() == HitResult.Type.ENTITY) {
						hitResult = hitResultToChoose;
						break;
					}
					if (hitResultToChoose.getType() == HitResult.Type.MISS && hitResult == null) {
						hitResult = hitResultToChoose;
					}
				}
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
			if(!player.hasLineOfSight(entity)) {
				d = 6.25;
			}

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
						ServerGamePacketListenerImpl.LOGGER.warn("Player {} tried to attack an invalid entity", player.getName().getString());
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
			pastPings.add(0, Mth.ceil(responseTimer * 0.5));
			pastPings.removeIf(pastPing -> pastPings.indexOf(pastPing) > 5);
			responseTimer = 0;
			Collections.sort(pastPings);
			int averagePing = pastPings.get(2);
			currentAveragePing = Mth.clamp(averagePing, 25, 200);
		}
		if (oldHitResults.size() > 1)
			oldHitResults.add(1, newValue);
		else
			oldHitResults.add(newValue);
		oldHitResults.removeIf(hitResult -> {
			if(oldHitResults.indexOf(hitResult) > currentAveragePing + 1)
				hitResultToRotationMap.remove(hitResult);
			return oldHitResults.indexOf(hitResult) > currentAveragePing + 1;
		});
		Float[] rotations = new Float[2];
		rotations[0] = getYHeadRot() % 360;
		rotations[1] = getXRot() % 360;
		hitResultToRotationMap.put(newValue, rotations);
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
	public CopyOnWriteArrayList<HitResult> getOldHitResults() {
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
	public Map<HitResult, Float[]> getHitResultToRotationMap() {
		return hitResultToRotationMap;
	}

	@Override
	public void getPresentResult() {
		Entity camera = getCamera();
		if (camera != null) {
			oldHitResults.set(0, MethodHandler.pickResult(player, camera));
		}
	}
}
