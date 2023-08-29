package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.AABBExtensions;
import net.atlas.combatify.extensions.PlayerExtensions;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
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

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends PlayerMixin {

	@Shadow
	public abstract Entity getCamera();

	@Unique
	public final ServerPlayer player = ServerPlayer.class.cast(this);

	public ServerPlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}
	@Inject(method = "swing", at = @At(value = "HEAD"), cancellable = true)
	public void removeReset(InteractionHand hand, CallbackInfo ci) {
		super.swing(hand);
		if(Combatify.unmoddedPlayers.contains(getUUID())) {
			if (Combatify.isPlayerAttacking.get(getUUID()) && Combatify.finalizingAttack.get(getUUID())){
				Entity camera = getCamera();
				if (camera != null) {
					double d = ((PlayerExtensions) player).getAttackRange(0.0F) + 2;
					HitResult hitResult = camera.pick(d, 1, false);
					Vec3 vec3 = camera.getEyePosition(1);
					boolean bl = false;
					double e = d;
					if (d > ((PlayerExtensions) player).getAttackRange(0.0F)) {
						bl = true;
					}

					e *= e;
					if (hitResult != null) {
						e = hitResult.getLocation().distanceToSqr(vec3);
					}

					Vec3 vec32 = camera.getViewVector(1.0F);
					Vec3 vec33 = vec3.add(vec32.x * d, vec32.y * d, vec32.z * d);
					AABB aABB = camera.getBoundingBox().expandTowards(vec32.scale(d)).inflate(1.0, 1.0, 1.0);
					EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(camera, vec3, vec33, aABB, (entityx) ->
						!entityx.isSpectator() && entityx.isPickable(), e);
					if (entityHitResult != null) {
						Vec3 vec34 = entityHitResult.getLocation();
						double h = vec3.distanceToSqr(vec34);
						if (bl && h > ((PlayerExtensions) player).getSquaredAttackRange(0.0F)) {
							hitResult = BlockHitResult.miss(vec34, Direction.getNearest(vec32.x, vec32.y, vec32.z), BlockPos.containing(vec34));
						} else if (h < e || hitResult == null) {
							hitResult = entityHitResult;
						}
					}
					redirectResult(hitResult);
					Combatify.finalizingAttack.put(getUUID(), false);
					switch (hitResult.getType()) {
						case BLOCK:
							this.player.gameMode.handleBlockBreakAction(((BlockHitResult) hitResult).getBlockPos(), ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, ((BlockHitResult) hitResult).getDirection(), this.player.level().getMaxBuildHeight(), 0);
							this.player.connection.ackBlockChangesUpTo(0);
						case ENTITY:
							handleInteract(((EntityHitResult) hitResult).getEntity(), true);
						case MISS:
							handleInteract(player, false);
					}
					Combatify.finalizingAttack.put(getUUID(), true);
				}
			}
			Combatify.isPlayerAttacking.put(getUUID(), true);
		}
		ci.cancel();
	}
	public void handleInteract(Entity entity, boolean hit) {
		if(!isAttackAvailable(1.0F))
			return;
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
			Vec3 vec3 = player.getEyePosition(0.0F);
			if (vec3.distanceToSqr(((AABBExtensions)aABB).getNearestPointTo(vec3)) < d) {
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
	@Nullable
	public EntityHitResult rayTraceEntity(Player player, float partialTicks, double blockReachDistance) {
		Vec3 from = player.getEyePosition(partialTicks);
		Vec3 look = player.getViewVector(partialTicks);
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
