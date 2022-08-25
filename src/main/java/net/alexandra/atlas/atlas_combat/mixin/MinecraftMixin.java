package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.extensions.IMinecraft;
import net.alexandra.atlas.atlas_combat.extensions.IOptions;
import net.alexandra.atlas.atlas_combat.extensions.IPlayerGameMode;
import net.alexandra.atlas.atlas_combat.extensions.PlayerExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.*;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

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
	public abstract @org.jetbrains.annotations.Nullable Entity getCameraEntity();

	@Shadow
	@org.jetbrains.annotations.Nullable
	public Entity crosshairPickEntity;

	@Shadow
	protected int missTime;

	@Shadow
	@Final
	public ParticleEngine particleEngine;

	@Shadow
	public abstract void setConnectedToRealms(boolean b);

	@Shadow
	@Nullable
	public Screen screen;
	@Inject(method = "tick", at = @At(value = "TAIL"))
	public void injectSomething(CallbackInfo ci) {
		if (screen != null) {
			this.retainAttack = false;
		}
	}
	@Inject(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 10))
	public void injectAttack(CallbackInfo ci) {
		while(options.keyAttack.consumeClick()) {
			this.startAttack();
		}
	}
	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	private boolean startAttack() {
		Item item = player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
		Item offhandItem = player.getItemInHand(InteractionHand.OFF_HAND).getItem();
		boolean handHasShieldItem = item instanceof ShieldItem;
		boolean handHasBlockingItem = item instanceof SwordItem;
		boolean offhandHasShieldItem = offhandItem instanceof ShieldItem;
		boolean offhandHasBlockingItem = offhandItem instanceof SwordItem;
		if (player.isUsingItem() && handHasShieldItem || handHasBlockingItem) {
			if(offhandHasShieldItem || offhandHasBlockingItem) {
				player.getCooldowns().addCooldown(offhandItem, 20);
				player.stopUsingItem();
				player.level.broadcastEntityEvent(player, (byte)30);
			}
			player.getCooldowns().addCooldown(item, 20);
			player.stopUsingItem();
			player.level.broadcastEntityEvent(player, (byte)30);
		}
		if(missTime < 0) {
			return false;
		}else if (this.hitResult == null) {
			LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");
			if (this.gameMode.hasMissTime()) {
				this.missTime = 10;
			}

			return false;
		}else if (this.player.isHandsBusy()) {
			return false;
		} else {
			if (!((PlayerExtensions)this.player).isAttackAvailable(0.0F) && redirectResult(this.hitResult) != HitResult.Type.BLOCK) {
				float var1 = this.player.getAttackStrengthScale(0.0F);
				if (var1 < 0.8F) {
					return false;
				}

				if (var1 < 1.0F) {
					this.retainAttack = true;
					return true;
				}
			}

			this.retainAttack = false;
			boolean bl = false;
			if (!this.player.isHandsBusy()) {
				switch (redirectResult(this.hitResult)) {
					case ENTITY:
						if (player.distanceTo(((EntityHitResult)hitResult).getEntity()) <= ((PlayerExtensions)player).getAttackRange(player, 2.5)) {
							this.gameMode.attack(this.player, ((EntityHitResult) this.hitResult).getEntity());
						} else {
//							((IPlayerGameMode) this.gameMode).swingInAir(this.player);
							((PlayerExtensions)player).attackAir();
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
//						((IPlayerGameMode) this.gameMode).swingInAir(this.player);
						((PlayerExtensions)player).attackAir();
				}

				this.player.swing(InteractionHand.MAIN_HAND);
				return bl;
			}
		}
		return false;
	}
	public final HitResult.Type redirectResult(HitResult instance) {
		HitResult.Type type = instance.getType();
		if(type == HitResult.Type.BLOCK) {
			BlockHitResult blockHitResult = (BlockHitResult)instance;
			BlockPos blockPos = blockHitResult.getBlockPos();
			boolean bl = !level.getBlockState(blockPos).canOcclude() && !level.getBlockState(blockPos).getBlock().hasCollision;
			EntityHitResult rayTraceResult = rayTraceEntity(player, 1.0F, ((PlayerExtensions)player).getAttackRange(player, 2.5));
			Entity entity = rayTraceResult != null ? rayTraceResult.getEntity() : null;
			if (entity != null && bl) {
				crosshairPickEntity = entity;
				hitResult = rayTraceResult;
				return hitResult.getType();
			}else {
				return type;
			}

		}
		return type;
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
						this.gameMode.useItem(this.player, interactionHand);
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
	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	private void continueAttack(boolean bl) {
		if (!bl) {
			this.missTime = 0;
		}

		if (missTime <= 0 && !this.player.isUsingItem()) {
			if (bl && this.hitResult != null && this.hitResult.getType() == HitResult.Type.BLOCK) {
				BlockHitResult blockHitResult = (BlockHitResult)this.hitResult;
				BlockPos blockPos = blockHitResult.getBlockPos();
				if (!this.level.getBlockState(blockPos).isAir()) {
					Direction direction = blockHitResult.getDirection();
					if (this.gameMode.continueDestroyBlock(blockPos, direction)) {
						particleEngine.crack(blockPos, direction);
						this.player.swing(InteractionHand.MAIN_HAND);
					}
				}

				this.retainAttack = false;
			} else if (bl && ((PlayerExtensions)this.player).isAttackAvailable(-1.0F)) {
				this.startAttack();
			} else {
				this.gameMode.stopDestroyBlock();
			}
		}
	}
}
