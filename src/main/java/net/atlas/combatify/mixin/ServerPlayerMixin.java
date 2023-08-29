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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.TimerTask;

import static net.atlas.combatify.Combatify.scheduleHitResult;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends PlayerMixin implements ServerPlayerExtensions {

	private boolean retainAttack;

	@Shadow
	public abstract Entity getCamera();

	@Shadow
	public abstract void swing(InteractionHand interactionHand);

	public HitResult[] oldHitResults = new HitResult[12];

	@Unique
	public final ServerPlayer player = ServerPlayer.class.cast(this);

	@Unique
	public boolean shouldInit = true;

	public ServerPlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "tick", at = @At(value = "HEAD"))
	public void addShieldCrouch(CallbackInfo ci) {
		if (shouldInit && Combatify.unmoddedPlayers.contains(getUUID())) {
			scheduleHitResult.get(getUUID()).schedule(new TimerTask() {
				@Override
				public void run() {
					Entity camera = getCamera();
					if (camera != null) {
						adjustHitResults(pickResult(camera));
					}
				}
			}, 0, 10);
			shouldInit = false;
		}
		if (((PlayerExtensions) this.player).isAttackAvailable(-1.0F) && retainAttack && Combatify.unmoddedPlayers.contains(getUUID())) {
			retainAttack = false;
			swing(InteractionHand.MAIN_HAND);
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
			if (Combatify.isPlayerAttacking.get(getUUID())) {
				HitResult hitResult = null;
				Entity camera = getCamera();
				if (camera != null)
					oldHitResults[11] = pickResult(camera);
				for (HitResult hitResultToChoose : oldHitResults) {
					if(hitResultToChoose == null)
						continue;
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
		oldHitResults[10] = oldHitResults[9];
		oldHitResults[9] = oldHitResults[8];
		oldHitResults[8] = oldHitResults[7];
		oldHitResults[7] = oldHitResults[6];
		oldHitResults[6] = oldHitResults[5];
		oldHitResults[5] = oldHitResults[4];
		oldHitResults[4] = oldHitResults[3];
		oldHitResults[3] = oldHitResults[2];
		oldHitResults[2] = oldHitResults[1];
		oldHitResults[1] = oldHitResults[0];
		oldHitResults[0] = newValue;
	}
}
