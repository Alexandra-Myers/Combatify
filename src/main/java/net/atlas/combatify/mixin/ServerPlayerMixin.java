package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.*;
import net.atlas.combatify.util.CombatUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPingPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

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
	public ArrayList<HitResult> oldHitResults = new ArrayList<>();
	public Map<HitResult, Float[]> hitResultToRotationMap = new HashMap<>();
	public ArrayList<Integer> pastPings = new ArrayList<>();
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
						adjustHitResults(pickResult(camera));
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
		if (((PlayerExtensions) this.player).isAttackAvailable(-1.0F) && retainAttack && Combatify.unmoddedPlayers.contains(getUUID())) {
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
				Entity camera = getCamera();
				if (camera != null) {
					oldHitResults.set(0, pickResult(camera));
				}
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
					if (hitResultToChoose.getType() == HitResult.Type.BLOCK) {
						hitResult = hitResultToChoose;
					}
					if (hitResultToChoose.getType() == HitResult.Type.MISS && hitResult == null) {
						hitResult = hitResultToChoose;
					}
				}
				if (hitResult != null) {
					Combatify.finalizingAttack.put(getUUID(), false);
					switch (hitResult.getType()) {
						case BLOCK:
							if (hitResult instanceof BlockHitResult) {
								this.player.gameMode.handleBlockBreakAction(((BlockHitResult) hitResult).getBlockPos(), ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, ((BlockHitResult) hitResult).getDirection(), this.player.level().getMaxBuildHeight(), 0);
								this.player.connection.ackBlockChangesUpTo(0);
							}
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
	public void handleInteract(Entity entity, boolean hit) {
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
				if(hit)
					this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, this.getSoundSource(), 1.0F, 1.0F);
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
			double d = ((PlayerExtensions)player).getAttackRange(1.0F) + 1;
			d *= d;
			if(!player.hasLineOfSight(entity)) {
				d = 6.25;
			}

			AABB aABB = entity.getBoundingBox();
			Vec3 eyePos = player.getEyePosition(0.0F);
			double dist = eyePos.distanceToSqr(((AABBExtensions)aABB).getNearestPointTo(eyePos));
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
					attackAir();
				}
			}
		}

	}
	public final HitResult redirectResult(HitResult instance) {
		if(instance.getType() == HitResult.Type.BLOCK) {
			BlockHitResult blockHitResult = (BlockHitResult)instance;
			BlockPos blockPos = blockHitResult.getBlockPos();
			boolean bl = !level().getBlockState(blockPos).canOcclude() && !level().getBlockState(blockPos).getBlock().hasCollision;
			assert player != null;
			EntityHitResult rayTraceResult = rayTraceEntity(player, 1.0F, ((PlayerExtensions)player).getAttackRange(0.0F));
			if (rayTraceResult != null && bl) {
				return rayTraceResult;
			} else {
				return instance;
			}
		}
		return instance;
	}
	@Override
	public HitResult pickResult(Entity camera) {
		double d = ((PlayerExtensions) player).getAttackRange(0.0F) + 2;
		HitResult hitResult = camera.pick(d, 1, false);
		Vec3 eyePosition = camera.getEyePosition(1.0F);
		Vec3 viewVector = camera.getViewVector(1.0F);
		boolean bl = false;
		double e = d;
		if (d > ((PlayerExtensions) player).getAttackRange(0.0F)) {
			bl = true;
		}

		e *= e;
		if (hitResult != null) {
			e = hitResult.getLocation().distanceToSqr(eyePosition);
		}
		Vec3 vec32 = eyePosition.add(viewVector.x * d, viewVector.y * d, viewVector.z * d);
		AABB aABB = camera.getBoundingBox().expandTowards(viewVector.scale(d)).inflate(1.0, 1.0, 1.0);
		EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(camera, eyePosition, vec32, aABB, (entityx) ->
			!entityx.isSpectator() && entityx.isPickable(), e);
		if (entityHitResult != null) {
			Vec3 vec33 = entityHitResult.getLocation();
			double h = eyePosition.distanceToSqr(vec33);
			if (bl && h > ((PlayerExtensions) player).getSquaredAttackRange(0.0F)) {
				hitResult = BlockHitResult.miss(vec33, Direction.getNearest(viewVector.x, viewVector.y, viewVector.z), BlockPos.containing(vec33));
			} else if (h < e || hitResult == null) {
				hitResult = entityHitResult;
			}
		}
		hitResult = redirectResult(hitResult);
		return hitResult;
	}
	@Nullable
	public EntityHitResult rayTraceEntity(Player player, float partialTicks, double blockReachDistance) {
		Vec3 from = getEyePosition(partialTicks);
		Vec3 look = getViewVector(partialTicks);
		Vec3 to = from.add(look.x * blockReachDistance, look.y * blockReachDistance, look.z * blockReachDistance);

		return ProjectileUtil.getEntityHitResult(
			player.level(),
			player,
			from,
			to,
			new AABB(from, to),
			EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != null
				&& e.isPickable()
				&& e instanceof LivingEntity)
		);
	}
	@Override
	public void adjustHitResults(HitResult newValue) {
		if (awaitingResponse)
			responseTimer++;
		if (!awaitingResponse && responseTimer > 0) {
			double mul = 2.5;
			pastPings.add(0, Mth.ceil(responseTimer * mul));
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
}
