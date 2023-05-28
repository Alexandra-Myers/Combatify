package net.alexandra.atlas.atlas_combat.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.config.AtlasConfig;
import net.alexandra.atlas.atlas_combat.extensions.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements IMinecraft {
	@Shadow
	@Final
	public Options options;

	@Shadow
	@Nullable
	public LocalPlayer player;
	@Unique
	public boolean retainAttack;

	@Shadow
	@Nullable
	public HitResult hitResult;
	@Shadow
	private int rightClickDelay;
	@Shadow
	@Final
	private static Logger LOGGER;

	@Shadow
	@Nullable
	public MultiPlayerGameMode gameMode;

	@Shadow
	@Nullable
	public ClientLevel level;

	@Shadow
	protected abstract boolean startAttack();
	@Shadow
	public abstract void startUseItem();
	@Shadow
	public abstract @org.jetbrains.annotations.Nullable Entity getCameraEntity();

	@Shadow
	@org.jetbrains.annotations.Nullable
	public Entity crosshairPickEntity;

	@Shadow
	protected int missTime;

	@Unique
	Entity lastPickedEntity = null;

	@Shadow
	@Final
	public ParticleEngine particleEngine;

	@Shadow
	public abstract void setConnectedToRealms(boolean b);

	@Shadow
	@Nullable
	public Screen screen;

	@Shadow
	public abstract void stop();

	@Inject(method = "tick", at = @At(value = "TAIL"))
	public void injectSomething(CallbackInfo ci) {
		if(crosshairPickEntity != null && hitResult != null && (this.hitResult).distanceTo(this.crosshairPickEntity) <= ((PlayerExtensions)player).getAttackRange(player, 2.5)) {
			lastPickedEntity = crosshairPickEntity;
		}
		if (screen != null) {
			this.retainAttack = false;
		}
	}
	@ModifyExpressionValue(method = "handleKeybinds",
			slice = @Slice(
					from = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z", ordinal = 0)
			),
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 0))
	public boolean allowBlockHitting(boolean original) {
		if (!original) return false;
		assert player != null;
		boolean bl = !(player.getUseItem().getItem() instanceof ShieldItem);
		if(bl && ((PlayerExtensions) this.player).isAttackAvailable(0.0F)) {
			assert hitResult != null;
			if (hitResult.getType() == HitResult.Type.BLOCK) {
				startAttack();
			}
		}
		return bl;
	}
	@Inject(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 2))
	public void checkIfCrouch(CallbackInfo ci) {
		if(((PlayerExtensions) player).hasEnabledShieldOnCrouch() && player.isCrouching()) {
			while(options.keyUse.consumeClick()) {
				startUseItem();
			}
			while(options.keyAttack.consumeClick()) {
				startAttack();
			}
		}
	}
	@Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V"))
	public void checkIfCrouch(MultiPlayerGameMode instance, Player player) {
		if(((PlayerExtensions) player).hasEnabledShieldOnCrouch() && player.isCrouching()) {
		} else {
			instance.releaseUsingItem(player);
		}
	}
	@ModifyExpressionValue(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 4))
	public boolean redirectContinue(boolean original) {
		return original || retainAttack;
	}
	@Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;startAttack()Z"))
	public boolean redirectAttack(Minecraft instance) {
		if (!((PlayerExtensions) this.player).isAttackAvailable(0.0F)) {
			assert hitResult != null;
			if (redirectResult(hitResult).getType() != HitResult.Type.BLOCK) {
				float var1 = this.player.getAttackStrengthScale(0.0F);
				if (var1 < 0.8F) {
					return false;
				}

				if (var1 < 1.0F) {
					this.retainAttack = true;
					return false;
				}
			}
		}
		return startAttack();
	}
	@Inject(method = "startAttack", at = @At(value = "HEAD"), cancellable = true)
	private void startAttack(CallbackInfoReturnable<Boolean> cir) {
		if(missTime < 0) {
			cir.setReturnValue(false);
			cir.cancel();
		}else if (this.hitResult == null) {
			LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");
			if (this.gameMode.hasMissTime()) {
				this.missTime = 10;
			}

			cir.setReturnValue(false);
			cir.cancel();
		}else if (this.player.isHandsBusy()) {
			cir.setReturnValue(false);
			cir.cancel();
		} else {
			this.retainAttack = false;
			boolean bl = false;
			switch (redirectResult(this.hitResult).getType()) {
				case ENTITY:
					if (player.distanceTo(((EntityHitResult)hitResult).getEntity()) <= ((PlayerExtensions)player).getAttackRange(player, 2.5)) {
						this.gameMode.attack(this.player, ((EntityHitResult) this.hitResult).getEntity());
					} else {
						((IPlayerGameMode)gameMode).swingInAir(player);
					}
					break;
				case BLOCK:
					BlockHitResult blockHitResult = (BlockHitResult)this.hitResult;
					BlockPos blockPos = blockHitResult.getBlockPos();
					if (!this.level.getBlockState(blockPos).isAir()) {
						this.gameMode.startDestroyBlock(blockPos, blockHitResult.getDirection());
						if (this.level.getBlockState(blockPos).isAir()) {
							bl = true;
						}
						break;
					}
				case MISS:
					EntityHitResult result = findEntity(player, 1.0F, ((PlayerExtensions)player).getAttackRange(player, 2.5));
					if(result != null && AtlasConfig.refinedCoyoteTime) {
						if(!(result.getEntity() instanceof Player)) {
							if (result.getEntity() instanceof Guardian
									|| result.getEntity() instanceof Cat
									|| result.getEntity() instanceof Vex
									|| (result.getEntity() instanceof LivingEntity entity && entity.isBaby())
									|| result.getEntity() instanceof Fox
									|| result.getEntity() instanceof Bee
									|| result.getEntity() instanceof Bat
									|| result.getEntity() instanceof AbstractFish
									|| result.getEntity() instanceof Rabbit) {
								result = findEntity(player, 1.0F, ((PlayerExtensions)player).getAttackRange(player, 2.5));
							} else {
								result = findNormalEntity(player, 1.0F, ((PlayerExtensions) player).getAttackRange(player, 2.5));
							}
							if(result != null) {
								this.gameMode.attack(this.player, result.getEntity());
							} else {
								((IPlayerGameMode)gameMode).swingInAir(player);
							}
						} else {
							((IPlayerGameMode)gameMode).swingInAir(player);
						}
					} else {
						((IPlayerGameMode)gameMode).swingInAir(player);
					}
			}

			this.player.swing(InteractionHand.MAIN_HAND);
			cir.setReturnValue(bl);
			cir.cancel();
		}
		cir.setReturnValue(false);
		cir.cancel();
	}
	@Override
	public final HitResult redirectResult(HitResult instance) {
		if(instance.getType() == HitResult.Type.BLOCK) {
			BlockHitResult blockHitResult = (BlockHitResult)instance;
			BlockPos blockPos = blockHitResult.getBlockPos();
			boolean bl = !level.getBlockState(blockPos).canOcclude() && !level.getBlockState(blockPos).getBlock().hasCollision;
			EntityHitResult rayTraceResult = rayTraceEntity(player, 1.0F, ((PlayerExtensions)player).getAttackRange(player, 2.5));
			Entity entity = rayTraceResult != null ? rayTraceResult.getEntity() : null;
			if (entity != null && bl) {
				crosshairPickEntity = entity;
				hitResult = rayTraceResult;
				return hitResult;
			}else {
				return instance;
			}

		}
		return instance;
	}
	@Unique
	@Override
	public final void startUseItem(InteractionHand interactionHand) {
		if (!gameMode.isDestroying()) {
			this.rightClickDelay = 4;
			if (!this.player.isHandsBusy()) {
				if (this.hitResult == null) {
					LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");
				}
					ItemStack itemStack = this.player.getItemInHand(interactionHand);
					if (!itemStack.isEmpty()) {
						this.gameMode.useItem(this.player, level, interactionHand);
					}
				}
		}
	}
	@Nullable
	@Override
	public EntityHitResult rayTraceEntity(Player player, float partialTicks, double blockReachDistance) {
		Vec3 from = player.getEyePosition(partialTicks);
		Vec3 look = player.getViewVector(partialTicks);
		Vec3 to = from.add(look.x * blockReachDistance, look.y * blockReachDistance, look.z * blockReachDistance);

		return ProjectileUtil.getEntityHitResult(
				player.level,
				player,
				from,
				to,
				new AABB(from, to),
				EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != null
				&& e.isPickable()
				&& e instanceof LivingEntity)
		);
	}
	@Nullable
	@Override
	public EntityHitResult findEntity(Player player, float partialTicks, double blockReachDistance) {
		Vec3 from = player.getEyePosition(partialTicks);
		Vec3 look = player.getViewVector(partialTicks);
		Vec3 to = from.add(look.x * blockReachDistance, look.y * blockReachDistance, look.z * blockReachDistance);

		for (double i = -1.0; i <= 1.0; i += 0.1) {
			for (double j = -1.0; j <= 1.0; j += 0.1) {
				for (double k = -1.0; k <= 1.0; k += 0.1) {
					EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
							player.level,
							player,
							from,
							to,
							new AABB(from, to.add(i, j, k)),
							EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != null
									&& e.isPickable()
									&& e instanceof LivingEntity)
					);
					if(entityHitResult != null) {
						boolean bl3 = entityHitResult.getEntity() == lastPickedEntity;
						if(bl3
								|| entityHitResult.getEntity() instanceof Guardian
								|| entityHitResult.getEntity() instanceof Cat
								|| entityHitResult.getEntity() instanceof Vex
								|| (entityHitResult.getEntity() instanceof LivingEntity entity && entity.isBaby())
								|| entityHitResult.getEntity() instanceof Fox
								|| entityHitResult.getEntity() instanceof Bee
								|| entityHitResult.getEntity() instanceof Bat
								|| entityHitResult.getEntity() instanceof AbstractFish
								|| entityHitResult.getEntity() instanceof Rabbit) {
							return entityHitResult;
						}
					}
				}
			}
		}
		return null;
	}
	@Nullable
	@Override
	public EntityHitResult findNormalEntity(Player player, float partialTicks, double blockReachDistance) {
		Vec3 from = player.getEyePosition(partialTicks);
		Vec3 look = player.getViewVector(partialTicks);
		Vec3 to = from.add(look.x * blockReachDistance, look.y * blockReachDistance, look.z * blockReachDistance);

		for (double i = -0.5; i <= 0.5; i += 0.1) {
			for (double j = -0.5; j <= 0.5; j += 0.1) {
				for (double k = -0.5; k <= 0.5; k += 0.1) {
					EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
							player.level,
							player,
							from,
							to,
							new AABB(from, to.add(i, j, k)),
							EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != null
									&& e.isPickable()
									&& e instanceof LivingEntity)
					);
					if(entityHitResult != null) {
						boolean bl3 = entityHitResult.getEntity() == lastPickedEntity;
						if(bl3
								|| entityHitResult.getEntity() instanceof Guardian
								|| entityHitResult.getEntity() instanceof Cat
								|| entityHitResult.getEntity() instanceof Vex
								|| (entityHitResult.getEntity() instanceof LivingEntity entity && entity.isBaby())
								|| entityHitResult.getEntity() instanceof Fox
								|| entityHitResult.getEntity() instanceof Bee
								|| entityHitResult.getEntity() instanceof Bat
								|| entityHitResult.getEntity() instanceof AbstractFish
								|| entityHitResult.getEntity() instanceof Rabbit) {
							return entityHitResult;
						}
					}
				}
			}
		}
		return null;
	}
	@Nullable
	@Override
	public EntityHitResult findEntity(Player player, float partialTicks, double blockReachDistance, int strengthMultiplier) {
		if(strengthMultiplier <= 50) {
			strengthMultiplier = 50;
		}
		Vec3 from = player.getEyePosition(partialTicks);
		Vec3 look = player.getViewVector(partialTicks);
		Vec3 to = from.add(look.x * blockReachDistance, look.y * blockReachDistance, look.z * blockReachDistance);

		for (double i = -1.0; i <= 1.0; i += 0.1) {
			for (double j = -1.0; j <= 1.0; j += 0.1) {
				for (double k = -1.0; k <= 1.0; k += 0.1) {
					EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
							player.level,
							player,
							from,
							to,
							new AABB(from, to.add(i * (strengthMultiplier / 100), j * (strengthMultiplier / 100), k * (strengthMultiplier / 100))),
							EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != null
									&& e.isPickable()
									&& e instanceof LivingEntity)
					);
					if(entityHitResult != null) {
						boolean bl3 = entityHitResult.getEntity() == lastPickedEntity;
						if(bl3
								|| entityHitResult.getEntity() instanceof Guardian
								|| entityHitResult.getEntity() instanceof Cat
								|| entityHitResult.getEntity() instanceof Vex
								|| (entityHitResult.getEntity() instanceof LivingEntity entity && entity.isBaby())
								|| entityHitResult.getEntity() instanceof Fox
								|| entityHitResult.getEntity() instanceof Bee
								|| entityHitResult.getEntity() instanceof Bat
								|| entityHitResult.getEntity() instanceof AbstractFish
								|| entityHitResult.getEntity() instanceof Rabbit) {
							return entityHitResult;
						}
					}
				}
			}
		}
		return null;
	}
	@Nullable
	@Override
	public EntityHitResult findNormalEntity(Player player, float partialTicks, double blockReachDistance, int strengthMultiplier) {
		if(strengthMultiplier <= 50) {
			strengthMultiplier = 50;
		}
		Vec3 from = player.getEyePosition(partialTicks);
		Vec3 look = player.getViewVector(partialTicks);
		Vec3 to = from.add(look.x * blockReachDistance, look.y * blockReachDistance, look.z * blockReachDistance);

		for (double i = -0.5; i <= 0.5; i += 0.1) {
			for (double j = -0.5; j <= 0.5; j += 0.1) {
				for (double k = -0.5; k <= 0.5; k += 0.1) {
					EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
							player.level,
							player,
							from,
							to,
							new AABB(from, to.add(i * (strengthMultiplier / 100), j * (strengthMultiplier / 100), k * (strengthMultiplier / 100))),
							EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != null
									&& e.isPickable()
									&& e instanceof LivingEntity)
					);
					if(entityHitResult != null) {
						boolean bl3 = entityHitResult.getEntity() == lastPickedEntity;
						if(bl3
								|| entityHitResult.getEntity() instanceof Guardian
								|| entityHitResult.getEntity() instanceof Cat
								|| entityHitResult.getEntity() instanceof Vex
								|| (entityHitResult.getEntity() instanceof LivingEntity entity && entity.isBaby())
								|| entityHitResult.getEntity() instanceof Fox
								|| entityHitResult.getEntity() instanceof Bee
								|| entityHitResult.getEntity() instanceof Bat
								|| entityHitResult.getEntity() instanceof AbstractFish
								|| entityHitResult.getEntity() instanceof Rabbit) {
							return entityHitResult;
						}
					}
				}
			}
		}
		return null;
	}
	@Inject(method = "continueAttack", at = @At(value = "HEAD"), cancellable = true)
	private void continueAttack(boolean bl, CallbackInfo ci) {
		if (missTime <= 0 && !this.player.isUsingItem()) {
			if (bl && this.hitResult != null && this.hitResult.getType() == HitResult.Type.BLOCK) {
				this.retainAttack = false;
			} else if (bl && ((PlayerExtensions)this.player).isAttackAvailable(-1.0F) && ((IOptions)options).autoAttack() && AtlasConfig.autoAttackAllowed) {
				this.startAttack();
				ci.cancel();
			}
		}
	}
	@Override
	public void initiateAttack() {
		startAttack();
	}
}
