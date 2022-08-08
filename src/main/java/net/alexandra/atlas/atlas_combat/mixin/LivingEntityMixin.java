package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.enchantment.CustomEnchantmentHelper;
import net.alexandra.atlas.atlas_combat.extensions.LivingEntityExtensions;
import net.alexandra.atlas.atlas_combat.extensions.PlayerExtensions;
import net.alexandra.atlas.atlas_combat.util.ShieldUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityExtensions {

	public LivingEntityMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Shadow
	protected abstract boolean checkTotemDeathProtection(DamageSource source);

	@Shadow
	@Nullable
	protected abstract SoundEvent getDeathSound();

	@Shadow
	protected abstract float getSoundVolume();

	@Shadow
	protected abstract void playHurtSound(DamageSource source);

	@Shadow
	@Nullable
	private DamageSource lastDamageSource;

	@Shadow
	private long lastDamageStamp;

	@Shadow
	protected abstract void hurtCurrentlyUsedShield(float amount);

	@Shadow
	protected abstract void blockUsingShield(LivingEntity attacker);

	@Shadow
	@Nullable
	protected Player lastHurtByPlayer;

	@Shadow
	protected int lastHurtByPlayerTime;

	@Shadow
	protected float lastHurt;

	@Shadow
	protected abstract void actuallyHurt(DamageSource source, float amount);

	/**
	 * @author zOnlyKroks
	 */
	@Overwrite()
	public void blockedByShield(LivingEntity target) {
		target.knockback(0.5F, target.getX() - ((LivingEntity)(Object)this).getX(), target.getZ() - ((LivingEntity)(Object)this).getZ());
		if (((LivingEntity)(Object)this).getMainHandItem().getItem() instanceof AxeItem) {
			float var2 = 1.6F + (float) CustomEnchantmentHelper.getChopping(((LivingEntity) (Object)this)) * 0.5F;
			if(target instanceof PlayerExtensions player) {
				player.customShieldInteractions(var2);
			}
		}
	}

	/**
	 * @author zOnlyKroks
	 */
	@Inject(method = "hurt", at = @At("HEAD"),cancellable = true)
	public void hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity thisLivingEntity = ((LivingEntity) (Object)this);
		if (thisLivingEntity.isInvulnerableTo(source) || thisLivingEntity.level.isClientSide || thisLivingEntity.isDeadOrDying() || (source.isFire() && thisLivingEntity.hasEffect(MobEffects.FIRE_RESISTANCE))) {
			cir.setReturnValue(false);
			cir.cancel();
		}else {
			if (thisLivingEntity.isSleeping() && !thisLivingEntity.level.isClientSide) {
				thisLivingEntity.stopSleeping();
			}

			thisLivingEntity.setNoActionTime(0);
			float var3 = amount;
			if ((source == DamageSource.ANVIL || source == DamageSource.FALLING_BLOCK) && !thisLivingEntity.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
				thisLivingEntity.getItemBySlot(EquipmentSlot.HEAD).hurtAndBreak((int)(amount * 4.0F + thisLivingEntity.getRandom().nextFloat() * amount * 2.0F), thisLivingEntity, (var0) -> {
					var0.broadcastBreakEvent(EquipmentSlot.HEAD);
				});
				amount *= 0.75F;
			}

			boolean var4 = false;
			float var5 = 0.0F;
			Entity var6;
			if (amount > 0.0F && thisLivingEntity.isDamageSourceBlocked(source)) {
				var5 = Math.min(ShieldUtils.getShieldBlockDamageValue(this.getBlockingItem()), amount);
				if (!source.isProjectile() && !source.isExplosion()) {
					var6 = source.getDirectEntity();
					if (var6 instanceof LivingEntity livingEntity) {
						blockUsingShield(livingEntity);
					}
				} else {
					var5 = amount;
				}

				hurtCurrentlyUsedShield(var5);
				amount -= var5;
				var4 = true;
			}

			thisLivingEntity.animationSpeed = 1.5F;
			var6 = source.getEntity();
			int var7 = 10;
			if (var6 != null && var6 instanceof PlayerExtensions player) {
				var7 = Math.min(player.getAttackDelay((Player)var6), var7);
			}

			if (source.isProjectile()) {
				var7 = 0;
			}

			boolean var8 = true;
			if (thisLivingEntity.invulnerableTime > 0) {
				if (amount <= lastHurt) {
					cir.setReturnValue(false);
					cir.cancel();
				}

				actuallyHurt(source, amount - lastHurt);
				lastHurt = amount;
				var8 = false;
			} else {
				lastHurt = amount;
				thisLivingEntity.invulnerableTime = var7;
				actuallyHurt(source, amount);
				thisLivingEntity.hurtDuration = 10;
				thisLivingEntity.hurtTime = thisLivingEntity.hurtDuration;
			}

			thisLivingEntity.hurtDir = 0.0F;
			if (var6 != null) {
				if (var6 instanceof LivingEntity livingEntity) {
					thisLivingEntity.setLastHurtByMob(livingEntity);
					if (thisLivingEntity.isUsingItem() && (thisLivingEntity.getUseItem().getUseAnimation() == UseAnim.EAT || thisLivingEntity.getUseItem().getUseAnimation() == UseAnim.DRINK)) {
						thisLivingEntity.stopUsingItem();
					}
				}

				if (var6 instanceof Player player) {
					lastHurtByPlayerTime = 100;
					lastHurtByPlayer = player;
				} else if (var6 instanceof Wolf wolf) {
					Wolf var9 = wolf;
					if (var9.isTame()) {
						lastHurtByPlayerTime = 100;
						LivingEntity var10 = var9.getOwner();
						if (var10 != null && var10.getType() == EntityType.PLAYER) {
							lastHurtByPlayer = (Player)var10;
						} else {
							lastHurtByPlayer = null;
						}
					}
				}
			}

			if (var8) {
				if (var4) {
					thisLivingEntity.level.broadcastEntityEvent(thisLivingEntity, (byte)29);
				} else if (source instanceof EntityDamageSource entityDamageSource && entityDamageSource.isThorns()) {
					thisLivingEntity.level.broadcastEntityEvent(thisLivingEntity, (byte)33);
				} else {
					byte var13;
					if (source == DamageSource.DROWN) {
						var13 = 36;
					} else if (source.isFire()) {
						var13 = 37;
					} else if (source == DamageSource.SWEET_BERRY_BUSH) {
						var13 = 44;
					} else {
						var13 = 2;
					}

					thisLivingEntity.level.broadcastEntityEvent(thisLivingEntity, var13);
				}

				if (source != DamageSource.DROWN && (!var4 || amount > 0.0F)) {
					super.markHurt();
				}

				if (var6 != null) {
					double var14 = var6.getX() - thisLivingEntity.getX();

					double var11;
					for(var11 = var6.getZ() - thisLivingEntity.getZ(); var14 * var14 + var11 * var11 < 1.0E-4; var11 = (Math.random() - Math.random()) * 0.01) {
						var14 = (Math.random() - Math.random()) * 0.01;
					}

					thisLivingEntity.hurtDir = (float)(Mth.atan2(var11, var14) * 57.2957763671875 - (double)thisLivingEntity.getYRot());
					thisLivingEntity.knockback(0.4F, var14, var11);
				} else {
					thisLivingEntity.hurtDir = (float)((int)(Math.random() * 2.0) * 180);
				}
			}

			if (thisLivingEntity.isDeadOrDying()) {
				if (!checkTotemDeathProtection(source)) {
					SoundEvent var15 = getDeathSound();
					if (var8 && var15 != null) {
						thisLivingEntity.playSound(var15, getSoundVolume(), thisLivingEntity.getVoicePitch());
					}

					thisLivingEntity.die(source);
				}
			} else if (var8) {
				playHurtSound(source);
			}

			boolean var16 = !var4 || amount > 0.0F;
			if (var16) {
				lastDamageSource = source;
				lastDamageStamp = thisLivingEntity.level.getGameTime();
			}

			if (thisLivingEntity instanceof ServerPlayer serverPlayer) {
				CriteriaTriggers.ENTITY_HURT_PLAYER.trigger(serverPlayer, source, var3, amount, var4);
				if (var5 > 0.0F && var5 < 3.4028235E37F) {
					serverPlayer.awardStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(var5 * 10.0F));
				}
			}

			if (var6 instanceof ServerPlayer serverPlayer) {
				CriteriaTriggers.PLAYER_HURT_ENTITY.trigger(serverPlayer, thisLivingEntity, source, var3, amount, var4);
			}

			cir.setReturnValue(var16);
			cir.cancel();
		}
	}

	@Override
	public ItemStack getBlockingItem() {
		LivingEntity thisLivingEntity = ((LivingEntity) (Object)this);
		if (thisLivingEntity.isUsingItem() && !thisLivingEntity.getUseItem().isEmpty()) {
			Item var2 = thisLivingEntity.getUseItem().getItem();
			if (var2.getUseAnimation(thisLivingEntity.getUseItem()) == UseAnim.BLOCK) {
				return thisLivingEntity.getUseItem();
			}
		} else if ((thisLivingEntity.isOnGround() && thisLivingEntity.isCrouching() || thisLivingEntity.isPassenger()) && this.hasEnabledShieldOnCrouch()) {
			ItemStack var1 = thisLivingEntity.getItemInHand(InteractionHand.OFF_HAND);
			if (!var1.isEmpty() && var1.getItem().getUseAnimation(var1) == UseAnim.BLOCK && !this.isItemOnCooldown(var1)) {
				return var1;
			}
		}

		return ItemStack.EMPTY;
	}

	@Override
	public boolean isItemOnCooldown(ItemStack var1) {
		return false;
	}

	@Override
	public boolean hasEnabledShieldOnCrouch() {
		return false;
	}
}
