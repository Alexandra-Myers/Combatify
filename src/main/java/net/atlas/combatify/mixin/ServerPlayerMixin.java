package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends PlayerMixin {

	private boolean retainAttack;

	@Shadow
	public abstract Entity getCamera();

	@Shadow
	public abstract void swing(InteractionHand interactionHand);

	public Vec3[] oldCameraEyePos = new Vec3[9];
	public Vec3[] oldCameraViewVectors = new Vec3[9];

	@Unique
	public final ServerPlayer player = ServerPlayer.class.cast(this);

	public ServerPlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "tick", at = @At(value = "HEAD"))
	public void addShieldCrouch(CallbackInfo ci) {
		Entity camera = getCamera();
		if (((PlayerExtensions) this.player).isAttackAvailable(-1.0F) && retainAttack && Combatify.unmoddedPlayers.contains(getUUID())) {
			swing(InteractionHand.MAIN_HAND);
		}
		if (camera != null) {
			oldCameraEyePos[8] = oldCameraEyePos[7];
			oldCameraEyePos[7] = oldCameraEyePos[6];
			oldCameraEyePos[6] = oldCameraEyePos[5];
			oldCameraEyePos[5] = oldCameraEyePos[4];
			oldCameraEyePos[4] = oldCameraEyePos[3];
			oldCameraEyePos[3] = oldCameraEyePos[2];
			oldCameraEyePos[2] = oldCameraEyePos[1];
			oldCameraEyePos[1] = oldCameraEyePos[0];
			oldCameraEyePos[0] = camera.getEyePosition(1);
			oldCameraViewVectors[8] = oldCameraViewVectors[7];
			oldCameraViewVectors[7] = oldCameraViewVectors[6];
			oldCameraViewVectors[6] = oldCameraViewVectors[5];
			oldCameraViewVectors[5] = oldCameraViewVectors[4];
			oldCameraViewVectors[4] = oldCameraViewVectors[3];
			oldCameraViewVectors[3] = oldCameraViewVectors[2];
			oldCameraViewVectors[2] = oldCameraViewVectors[1];
			oldCameraViewVectors[1] = oldCameraViewVectors[0];
			oldCameraViewVectors[0] = camera.getViewVector(1);
		}
		if(player.onGround() && Combatify.unmoddedPlayers.contains(player.getUUID())) {
			for (InteractionHand interactionHand : InteractionHand.values()) {
				if (player.isCrouching() && !player.isUsingItem()) {
					ItemStack itemStack = ((LivingEntityExtensions) this.player).getBlockingItem();

					Item blockingItem = getItemInHand(interactionHand).getItem();
					boolean bl = Combatify.CONFIG.shieldOnlyWhenCharged() && player.getAttackStrengthScale(1.0F) < 1.95F && blockingItem instanceof IShieldItem shieldItem && shieldItem.getBlockingType().requireFullCharge();
					if (!itemStack.isEmpty() && itemStack.getItem() instanceof IShieldItem shieldItem && shieldItem.getBlockingType().canCrouchBlock() && player.isCrouching() && player.getItemInHand(interactionHand) == itemStack && !bl) {
						if (!player.getCooldowns().isOnCooldown(itemStack.getItem())) {
							player.startUsingItem(interactionHand);
						}
					}
				} else if (player.isUsingItem() && !player.isCrouching()) {
					ItemStack itemStack = this.player.getItemInHand(interactionHand);
					if (!itemStack.isEmpty() && (itemStack.getItem() instanceof IShieldItem shieldItem && shieldItem.getBlockingType().canCrouchBlock())) {
						player.releaseUsingItem();
					}
				}
			}
		}
	}
	@Inject(method = "swing", at = @At(value = "HEAD"), cancellable = true)
	public void removeReset(InteractionHand hand, CallbackInfo ci) {
		super.swing(hand);
		if(Combatify.unmoddedPlayers.contains(getUUID())) {
			if (Combatify.isPlayerAttacking.get(getUUID())){
				Entity camera = getCamera();
				if (camera != null) {
					double d = ((PlayerExtensions) player).getAttackRange(0.0F) + 2;
					HitResult hitResult = camera.pick(d, 1, false);
					Vec3 eyePosition = camera.getEyePosition(1.0F);
					oldCameraEyePos[0] = eyePosition;
					Vec3 viewVector = camera.getViewVector(1.0F);
					oldCameraViewVectors[0] = viewVector;
					Vec3 basedOffPos = eyePosition;
					Vec3 basedOffVect = viewVector;
					for (int i = 0; i < 9; i++) {
						for (int j = 0; j < 9; j++) {
							if (hitResult.getType() == HitResult.Type.MISS) {
								Vec3 oldEyePos = oldCameraEyePos[i];
								Vec3 oldVector = oldCameraViewVectors[j];
								if(oldVector == null || oldEyePos == null)
									continue;
								hitResult = pickFromCamera(d, false, camera, oldEyePos, oldVector);
								if (hitResult.getType() != HitResult.Type.MISS) {
									basedOffPos = oldEyePos;
									basedOffVect = oldVector;
								}
							}
						}
					}
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
					boolean baseResultWorked = true;
					for (int j = 0; j < 9; j++) {
						for (int i = 0; i < 9; i++) {
							if (entityHitResult == null) {
								baseResultWorked = false;
								Vec3 oldVector = oldCameraViewVectors[i];
								Vec3 oldEyePos = oldCameraEyePos[j];
								if(oldVector == null || oldEyePos == null)
									continue;
								vec32 = oldEyePos.add(oldVector.x * d, oldVector.y * d, oldVector.z * d);
								aABB = camera.getBoundingBox().expandTowards(oldVector.scale(d)).inflate(1.0, 1.0, 1.0);
								entityHitResult = ProjectileUtil.getEntityHitResult(camera, oldEyePos, vec32, aABB, (entityx) ->
									!entityx.isSpectator() && entityx.isPickable(), e);
								if(entityHitResult != null) {
									basedOffPos = oldEyePos;
									basedOffVect = oldVector;
									break;
								}
							} else {
								if (baseResultWorked) {
									basedOffPos = eyePosition;
									basedOffVect = viewVector;
								}
								break;
							}
						}
					}
					if (entityHitResult != null) {
						Vec3 vec33 = entityHitResult.getLocation();
						double h = eyePosition.distanceToSqr(vec33);
						if (bl && h > ((PlayerExtensions) player).getSquaredAttackRange(0.0F)) {
							hitResult = BlockHitResult.miss(vec33, Direction.getNearest(viewVector.x, viewVector.y, viewVector.z), BlockPos.containing(vec33));
						} else if (h < e || hitResult == null) {
							hitResult = entityHitResult;
						}
					}
					hitResult = redirectResult(hitResult, basedOffPos, basedOffVect);
					Combatify.finalizingAttack.put(getUUID(), false);
					switch (hitResult.getType()) {
						case BLOCK:
							this.player.gameMode.handleBlockBreakAction(((BlockHitResult) hitResult).getBlockPos(), ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, ((BlockHitResult) hitResult).getDirection(), this.player.level().getMaxBuildHeight(), 0);
							this.player.connection.ackBlockChangesUpTo(0);
						case ENTITY:
							assert hitResult instanceof EntityHitResult;
							Entity entity = ((EntityHitResult)hitResult).getEntity();
							handleInteract(entity, true, basedOffPos);
						case MISS:
							handleInteract(player, false, basedOffPos);
					}
				}
			}
			Combatify.finalizingAttack.put(getUUID(), true);
			Combatify.isPlayerAttacking.put(getUUID(), true);
		}
		ci.cancel();
	}
	public HitResult pickFromCamera(double d, boolean bl, Entity camera, Vec3 eyePos, Vec3 viewVect) {
		Vec3 vectorEnd = eyePos.add(viewVect.x * d, viewVect.y * d, viewVect.z * d);
		return camera.level().clip(new ClipContext(eyePos, vectorEnd, ClipContext.Block.OUTLINE, bl ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, this));
	}
	public void handleInteract(Entity entity, boolean hit, Vec3 eyePos) {
		if(!isAttackAvailable(0.0F)) {
			float var1 = this.player.getAttackStrengthScale(0.0F);
			if (var1 < 0.8F) {
				return;
			}

			if (var1 < 1.0F) {
				retainAttack = true;
				return;
			}
		}
		retainAttack = false;
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
			if (eyePos.distanceToSqr(((AABBExtensions)aABB).getNearestPointTo(eyePos)) < d) {
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
	public final HitResult redirectResult(HitResult instance, Vec3 from, Vec3 look) {
		if(instance.getType() == HitResult.Type.BLOCK) {
			BlockHitResult blockHitResult = (BlockHitResult)instance;
			BlockPos blockPos = blockHitResult.getBlockPos();
			boolean bl = !level().getBlockState(blockPos).canOcclude() && !level().getBlockState(blockPos).getBlock().hasCollision;
			assert player != null;
			EntityHitResult rayTraceResult = rayTraceEntity(player, from, look, ((PlayerExtensions)player).getAttackRange(0.0F));
			if (rayTraceResult != null && bl) {
				return rayTraceResult;
			} else {
				return instance;
			}
		}
		return instance;
	}
	@Nullable
	public EntityHitResult rayTraceEntity(Player player, Vec3 from, Vec3 look, double blockReachDistance) {
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
}
