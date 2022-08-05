package net.alexandra.atlas.atlas_combat.mixin;

import com.mojang.datafixers.util.Either;
import net.alexandra.atlas.atlas_combat.item.NewAttributes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Mixin(Player.class)
public abstract class PlayerMixin {
	@Unique
	protected boolean missedAttackRecovery;

	private static final UUID MAGIC_ATTACK_DAMAGE_UUID = UUID.fromString("13C4E5B5-0F72-4359-AB1C-625F9DF5AA2B");

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	public void readAdditionalSaveData(CompoundTag nbt, CallbackInfo ci) {
		((Player) (Object)this).getAttribute(NewAttributes.ATTACK_REACH).setBaseValue(2.5);
		((Player) (Object)this).getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2.0);
	}

	/**
	 * @author zOnlyKroks
	 */
	@Overwrite()
	public static AttributeSupplier.Builder createAttributes() {
		return LivingEntity.createLivingAttributes().add(Attributes.ATTACK_DAMAGE, 1.0)
				.add(Attributes.MOVEMENT_SPEED, 0.1F)
				.add(NewAttributes.ATTACK_SPEED)
				.add(Attributes.LUCK)
				.add(NewAttributes.ATTACK_REACH);
	}

	/**
	 * @author zOnlyKroks
	 * @reason change attacks
	 */
	@Overwrite() //var1
	public void attack(Entity target) {
		if(target.isAttackable() && !target.skipAttackInteraction((Player) (Object)this) && isAttackAvailable(1.0F)) {
			float damageBonus;

			if (target instanceof LivingEntity livingEntity) {
				damageBonus = EnchantmentHelper.getDamageBonus(((Player)(Object)this).getMainHandItem(), livingEntity.getMobType());
			} else {
				damageBonus = EnchantmentHelper.getDamageBonus(((Player)(Object)this).getMainHandItem(), MobType.UNDEFINED);
			}

			//var 2
			if (damageBonus > 0.0F) {
				((Player) (Object)this).getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(new AttributeModifier(MAGIC_ATTACK_DAMAGE_UUID, "Magic modifier", damageBonus, AttributeModifier.Operation.ADDITION));
			}

			//var 3
			float attackDamage = (float)((Player) (Object)this).getAttributeValue(Attributes.ATTACK_DAMAGE);
			if (damageBonus > 0.0F) {
				((Player) (Object)this).getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(MAGIC_ATTACK_DAMAGE_UUID);
			}

			//var 4
			float currentAttackReach = this.getCurrentAttackReach(1.0F);

			if (attackDamage > 0.0F || damageBonus > 0.0F) {
				boolean var5 = !((Player) (Object)this).onClimbable() && !((Player) (Object)this).isInWater() && !((Player) (Object)this).hasEffect(MobEffects.BLINDNESS) && !((Player) (Object)this).isPassenger() && target instanceof LivingEntity;
				boolean var6 = false;
				int var7 = 0;
				var7 += EnchantmentHelper.getKnockbackBonus(((Player) (Object)this));
				if (((Player) (Object)this).isSprinting() && var5) {
					((Player) (Object)this).level.playSound(null, ((Player) (Object)this).getX(), ((Player) (Object)this).getY(), ((Player) (Object)this).getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, ((Player) (Object)this).getSoundSource(), 1.0F, 1.0F);
					++var7;
					var6 = true;
				}

				boolean var8 = var5 && ((Player) (Object)this).fallDistance > 0.0F && !((Player) (Object)this).isOnGround();
				if (var8) {
					attackDamage *= 1.5F;
				}

				boolean var9 = !var8 && !var6 && this.checkSweepAttack();
				float var10 = 0.0F;
				boolean var11 = false;
				int var12 = EnchantmentHelper.getFireAspect(((Player) (Object)this));
				if (target instanceof LivingEntity livingEntity) {
					var10 = livingEntity.getHealth();
					if (var12 > 0 && !target.isOnFire()) {
						var11 = true;
						target.setSecondsOnFire(1);
					}
				}

				Vec3 var13 = target.getDeltaMovement();
				boolean var14 = target.hurt(DamageSource.playerAttack(((Player) (Object)this)), attackDamage);
				if (var14) {
					if (var7 > 0) {
						if (target instanceof LivingEntity livingEntity) {
							livingEntity.knockback(var7 * 0.5F,Mth.sin(((Player) (Object)this).getYRot() * 0.017453292F), (-Mth.cos(((Player) (Object)this).getYRot() * 0.017453292F)));
						} else {
							target.push((-Mth.sin(((Player) (Object)this).getYRot() * 0.017453292F) * var7 * 0.5F), 0.1, (Mth.cos(((Player) (Object)this).getYRot() * 0.017453292F) * var7 * 0.5F));
						}

						((Player) (Object)this).setDeltaMovement(((Player) (Object)this).getDeltaMovement().multiply(0.6, 1.0, 0.6));
						((Player) (Object)this).setSprinting(false);
					}

					if (var9) {
						AABB var15 = target.getBoundingBox().inflate(1.0, 0.25, 1.0);
						this.betterSweepAttack(var15, currentAttackReach, attackDamage, target);
					}

					if (target instanceof ServerPlayer serverPlayer && target.hurtMarked) {
						serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(target));
						target.hurtMarked = false;
						target.setDeltaMovement(var13);
					}

					if (var8) {
						((Player) (Object)this).level.playSound(null, ((Player) (Object)this).getX(), ((Player) (Object)this).getY(), ((Player) (Object)this).getZ(), SoundEvents.PLAYER_ATTACK_CRIT, ((Player) (Object)this).getSoundSource(), 1.0F, 1.0F);
						((Player) (Object)this).crit(target);
					}

					if (!var8 && !var9) {
						if (var5) {
							((Player) (Object)this).level.playSound(null, ((Player) (Object)this).getX(), ((Player) (Object)this).getY(), ((Player) (Object)this).getZ(), SoundEvents.PLAYER_ATTACK_STRONG, ((Player) (Object)this).getSoundSource(), 1.0F, 1.0F);
						} else {
							((Player) (Object)this).level.playSound(null, ((Player) (Object)this).getX(), ((Player) (Object)this).getY(), ((Player) (Object)this).getZ(), SoundEvents.PLAYER_ATTACK_WEAK, ((Player) (Object)this).getSoundSource(), 1.0F, 1.0F);
						}
					}

					if (damageBonus > 0.0F) {
						((Player) (Object)this).magicCrit(target);
					}

					((Player) (Object)this).setLastHurtMob(target);
					if (target instanceof LivingEntity livingEntity) {
						EnchantmentHelper.doPostHurtEffects(livingEntity, ((Player) (Object)this));
					}

					EnchantmentHelper.doPostDamageEffects(((Player) (Object)this), target);
					ItemStack var19 = ((Player) (Object)this).getMainHandItem();
					Object var16 = target;
					if (target instanceof EnderDragonPart enderDragonPart) {
						var16 = enderDragonPart.parentMob;
					}

					if (!((Player) (Object)this).level.isClientSide && !var19.isEmpty() && var16 instanceof LivingEntity livingEntity) {
						var19.hurtEnemy(livingEntity, ((Player) (Object)this));
						if (var19.isEmpty()) {
							((Player) (Object)this).setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
						}
					}

					if (target instanceof LivingEntity livingEntity) {
						float var17 = var10 - livingEntity.getHealth();
						((Player) (Object)this).awardStat(Stats.DAMAGE_DEALT, Math.round(var17 * 10.0F));
						if (var12 > 0) {
							target.setSecondsOnFire(var12 * 4);
						}

						if (((Player) (Object)this).level instanceof ServerLevel serverLevel && var17 > 2.0F) {
							int var18 = (int)(var17 * 0.5);
							serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(0.5), target.getZ(), var18, 0.1, 0.0, 0.1, 0.2);
						}
					}

					((Player) (Object)this).causeFoodExhaustion(0.1F);
				} else {
					((Player) (Object)this).level.playSound(null, ((Player) (Object)this).getX(), ((Player) (Object)this).getY(), ((Player) (Object)this).getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, ((Player) (Object)this).getSoundSource(), 1.0F, 1.0F);
					if (var11) {
						((Player) (Object)this).clearFire();
					}
				}
			}
			this.resetAttackStrengthTicker();
		}
	}

	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	public void resetAttackStrengthTicker() {
		if(missedAttackRecovery){
			((Player) (Object)this).attackStrengthTicker = 10;
		}else {
			((Player) (Object)this).attackStrengthTicker = 0;
		}
	}

	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	public float getCurrentItemAttackStrengthDelay() {
		float var1 = (float)((Player) (Object)this).getAttribute(NewAttributes.ATTACK_SPEED).getValue() - 1.5F;
		var1 = Mth.clamp(var1, 0.1F, 1024.0F);
		return (1.0F / var1 * 20.0F + 0.5F);
	}

	public float getCurrentAttackReach(float var1) {
		float var2 = 0.0F;
		float var3 = this.getAttackStrengthScale(var1);
		if (var3 > 1.95F && !((Player) (Object)this).isCrouching()) {
			var2 = 1.0F;
		}

		return (float) (((Player)(Object)this).getAttribute(NewAttributes.ATTACK_REACH).getValue() + var2);
	}

	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	public float getAttackStrengthScale(float var1) {
		int defaultTicker;
		if(missedAttackRecovery){
			defaultTicker = 10;
		}else {
			defaultTicker = 0;
		}
		return defaultTicker == 0 ? 2.0F : Mth.clamp(2.0F * (1.0F - (((Player) (Object)this).attackStrengthTicker - var1) / defaultTicker), 0.0F, 2.0F);
	}

	public boolean isAttackAvailable(float var1) {
		int defaultTicker;
		if(missedAttackRecovery){
			defaultTicker = 10;
		}else {
			defaultTicker = 0;
		}
		if (!(this.getAttackStrengthScale(var1) < 1.0F)) {
			return true;
		} else {
			return this.missedAttackRecovery && defaultTicker - (((Player) (Object)this).attackStrengthTicker - var1) > 4.0F;
		}
	}

	protected boolean checkSweepAttack() {
		return this.getAttackStrengthScale(1.0F) > 1.95F && EnchantmentHelper.getSweepingDamageRatio(((Player) (Object)this)) > 0.0F;
	}

	public void betterSweepAttack(AABB var1, float var2, float var3, Entity var4) {
		float var5 = 1.0F + EnchantmentHelper.getSweepingDamageRatio(((Player) (Object)this)) * var3;
		List<LivingEntity> var6 = ((Player) (Object)this).level.getEntitiesOfClass(LivingEntity.class, var1);
		Iterator var7 = var6.iterator();

		while(true) {
			LivingEntity var8;
			do {
				do {
					do {
						do {
							if (!var7.hasNext()) {
								((Player) (Object)this).level.playSound(null, ((Player) (Object)this).getX(), ((Player) (Object)this).getY(), ((Player) (Object)this).getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, ((Player) (Object)this).getSoundSource(), 1.0F, 1.0F);
								if (((Player) (Object)this).level instanceof ServerLevel) {
									double var11 = (-Mth.sin(((Player) (Object)this).getYRot() * 0.017453292F));
									double var12 = Mth.cos(((Player) (Object)this).getYRot() * 0.017453292F);
									((ServerLevel)((Player) (Object)this).level).sendParticles(ParticleTypes.SWEEP_ATTACK, ((Player) (Object)this).getX() + var11, ((Player) (Object)this).getY() + ((Player) (Object)this).getBbHeight() * 0.5, ((Player) (Object)this).getZ() + var12, 0, var11, 0.0, var12, 0.0);
								}

								return;
							}

							var8 = (LivingEntity)var7.next();
						} while(var8 == ((Object)this));
					} while(var8 == var4);
				} while(((Player) (Object)this).isAlliedTo(var8));
			} while(var8 instanceof ArmorStand && ((ArmorStand)var8).isMarker());

			float var9 = var2 + var8.getBbWidth() * 0.5F;
			if (((Player) (Object)this).distanceToSqr(var8) < (var9 * var9)) {
				var8.knockback(0.4F, Mth.sin(((Player) (Object)this).getYRot() * 0.017453292F), (-Mth.cos(((Player) (Object)this).getYRot() * 0.017453292F)));
				var8.hurt(DamageSource.playerAttack(((Player) (Object)this)), var5);
			}
		}
	}

}
