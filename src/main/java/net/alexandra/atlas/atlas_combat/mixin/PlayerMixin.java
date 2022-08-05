package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.item.NewAttributes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
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
	protected int attackStrengthStartValue;
	@Unique
	protected boolean missedAttackRecovery;

	private static final UUID MAGIC_ATTACK_DAMAGE_UUID = UUID.fromString("13C4E5B5-0F72-4359-AB1C-625F9DF5AA2B");

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	public void readAdditionalSaveData(CompoundTag nbt, CallbackInfo ci) {
		((Player) (Object)this).getAttribute(NewAttributes.ATTACK_REACH).setBaseValue(2.5);
		((Player) (Object)this).getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2.0);
	}

	@Redirect(method = "createAttributes", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;createLivingAttributes()Lnet/minecraft/world/entity/ai/attributes/AttributeSupplier$Builder;"))
	private static AttributeSupplier.Builder redirectCreateAttributes() {
		return LivingEntity.createLivingAttributes().add(Attributes.ATTACK_DAMAGE, 1.0)
				.add(Attributes.MOVEMENT_SPEED, 0.1F)
				.add(Attributes.ATTACK_SPEED)
				.add(Attributes.LUCK)
				.add(NewAttributes.ATTACK_REACH);
	}

	/**
	 * @author zOnlyKroks
	 * @reason change attacks
	 */
	@Overwrite() //var1
	public void attack(Entity target) {
		if(!target.isAttackable() || target.skipAttackInteraction((Player) (Object)this) || !isAttackAvailable(1.0F)) return;

		float damageBonus;

		if (target instanceof LivingEntity) {
			damageBonus = EnchantmentHelper.getDamageBonus(((Player)(Object)this).getMainHandItem(), ((LivingEntity)target).getMobType());
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
				((Player) (Object)this).level.playSound((Player) null, ((Player) (Object)this).getX(), ((Player) (Object)this).getY(), ((Player) (Object)this).getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, ((Player) (Object)this).getSoundSource(), 1.0F, 1.0F);
				++var7;
				var6 = true;
			}

			boolean var8 = var5 && ((Player) (Object)this).fallDistance > 0.0F && !((Player) (Object)this).isOnGround();
			if (var8) {
				if (this.getAttackStrengthScale(1.0F) > 1.95F) {
					attackDamage *= 1.5F;
				} else {
					attackDamage *= 1.5F;
				}
			}

			boolean var9 = !var8 && !var6 && this.checkSweepAttack();
			float var10 = 0.0F;
			boolean var11 = false;
			int var12 = EnchantmentHelper.getFireAspect(((Player) (Object)this));
			if (target instanceof LivingEntity) {
				var10 = ((LivingEntity)target).getHealth();
				if (var12 > 0 && !target.isOnFire()) {
					var11 = true;
					target.setSecondsOnFire(1);
				}
			}

			Vec3 var13 = target.getDeltaMovement();
			boolean var14 = target.hurt(DamageSource.playerAttack(((Player) (Object)this)), attackDamage);
			if (var14) {
				if (var7 > 0) {
					if (target instanceof LivingEntity) {
						((LivingEntity) target).knockback((float) var7 * 0.5F, (double) Mth.sin(((Player) (Object)this).getYRot() * 0.017453292F), (-Mth.cos(((Player) (Object)this).getYRot() * 0.017453292F)));
					} else {
						target.push((double) (-Mth.sin(((Player) (Object)this).getYRot() * 0.017453292F) * var7 * 0.5F), 0.1, (Mth.cos(((Player) (Object)this).getYRot() * 0.017453292F) * (float) var7 * 0.5F));
					}

					((Player) (Object)this).setDeltaMovement(((Player) (Object)this).getDeltaMovement().multiply(0.6, 1.0, 0.6));
					((Player) (Object)this).setSprinting(false);
				}

				if (var9) {
					AABB var15 = target.getBoundingBox().inflate(1.0, 0.25, 1.0);
					this.betterSweepAttack(var15, currentAttackReach, attackDamage, target);
				}

				if (target instanceof ServerPlayer && target.hurtMarked) {
					((ServerPlayer)target).connection.send(new ClientboundSetEntityMotionPacket(target));
					target.hurtMarked = false;
					target.setDeltaMovement(var13);
				}

				if (var8) {
					((Player) (Object)this).level.playSound((Player)null, ((Player) (Object)this).getX(), ((Player) (Object)this).getY(), ((Player) (Object)this).getZ(), SoundEvents.PLAYER_ATTACK_CRIT, ((Player) (Object)this).getSoundSource(), 1.0F, 1.0F);
					((Player) (Object)this).crit(target);
				}

				if (!var8 && !var9) {
					if (var5) {
						((Player) (Object)this).level.playSound((Player)null, ((Player) (Object)this).getX(), ((Player) (Object)this).getY(), ((Player) (Object)this).getZ(), SoundEvents.PLAYER_ATTACK_STRONG, ((Player) (Object)this).getSoundSource(), 1.0F, 1.0F);
					} else {
						((Player) (Object)this).level.playSound((Player)null, ((Player) (Object)this).getX(), ((Player) (Object)this).getY(), ((Player) (Object)this).getZ(), SoundEvents.PLAYER_ATTACK_WEAK, ((Player) (Object)this).getSoundSource(), 1.0F, 1.0F);
					}
				}

				if (damageBonus > 0.0F) {
					((Player) (Object)this).magicCrit(target);
				}

				((Player) (Object)this).setLastHurtMob(target);
				if (target instanceof LivingEntity) {
					EnchantmentHelper.doPostHurtEffects((LivingEntity)target, ((Player) (Object)this));
				}

				EnchantmentHelper.doPostDamageEffects(((Player) (Object)this), target);
				ItemStack var19 = ((Player) (Object)this).getMainHandItem();
				Object var16 = target;
				if (target instanceof EnderDragonPart) {
					var16 = ((EnderDragonPart)target).parentMob;
				}

				if (!((Player) (Object)this).level.isClientSide && !var19.isEmpty() && var16 instanceof LivingEntity) {
					var19.hurtEnemy((LivingEntity)var16, ((Player) (Object)this));
					if (var19.isEmpty()) {
						((Player) (Object)this).setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
					}
				}

				if (target instanceof LivingEntity) {
					float var17 = var10 - ((LivingEntity)target).getHealth();
					((Player) (Object)this).awardStat(Stats.DAMAGE_DEALT, Math.round(var17 * 10.0F));
					if (var12 > 0) {
						target.setSecondsOnFire(var12 * 4);
					}

					if (((Player) (Object)this).level instanceof ServerLevel && var17 > 2.0F) {
						int var18 = (int)((double)var17 * 0.5);
						((ServerLevel)((Player) (Object)this).level).sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(0.5), target.getZ(), var18, 0.1, 0.0, 0.1, 0.2);
					}
				}

				((Player) (Object)this).causeFoodExhaustion(0.1F);
			} else {
				((Player) (Object)this).level.playSound((Player)null, ((Player) (Object)this).getX(), ((Player) (Object)this).getY(), ((Player) (Object)this).getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, ((Player) (Object)this).getSoundSource(), 1.0F, 1.0F);
				if (var11) {
					((Player) (Object)this).clearFire();
				}
			}
		}
		this.resetAttackStrengthTicker(true);
	}

	public void resetAttackStrengthTicker(boolean var1) {
		this.missedAttackRecovery = !var1;
		int var2 = this.getAttackDelay() * 2;
		if (var2 > ((Player) (Object)this).attackStrengthTicker) {
			this.attackStrengthStartValue = var2;
			((Player) (Object)this).attackStrengthTicker = this.attackStrengthStartValue;
		}
	}

	public int getAttackDelay() {
		float var1 = (float)((Player) (Object)this).getAttribute(Attributes.ATTACK_SPEED).getValue() - 1.5F;
		var1 = Mth.clamp(var1, 0.1F, 1024.0F);
		return (int)(1.0F / var1 * 20.0F + 0.5F);
	}

	public float getCurrentAttackReach(float var1) {
		float var2 = 0.0F;
		float var3 = this.getAttackStrengthScale(var1);
		if (var3 > 1.95F && !((Player) (Object)this).isCrouching()) {
			var2 = 1.0F;
		}

		return (float) (((Player)(Object)this).getAttribute(NewAttributes.ATTACK_REACH).getValue() + var2);
	}

	public float getAttackStrengthScale(float var1) {
		return this.attackStrengthStartValue == 0 ? 2.0F : Mth.clamp(2.0F * (1.0F - (((Player) (Object)this).attackStrengthTicker - var1) / (float)this.attackStrengthStartValue), 0.0F, 2.0F);
	}

	public boolean isAttackAvailable(float var1) {
		if (!(this.getAttackStrengthScale(var1) < 1.0F)) {
			return true;
		} else {
			return this.missedAttackRecovery && (float)this.attackStrengthStartValue - (((Player) (Object)this).attackStrengthTicker - var1) > 4.0F;
		}
	}

	protected boolean checkSweepAttack() {
		return this.getAttackStrengthScale(1.0F) > 1.95F && EnchantmentHelper.getSweepingDamageRatio(((Player) (Object)this)) > 0.0F;
	}

	public void betterSweepAttack(AABB var1, float var2, float var3, Entity var4) {
		float var5 = 1.0F + EnchantmentHelper.getSweepingDamageRatio(((Player) (Object)this)) * var3;
		List var6 = ((Player) (Object)this).level.getEntitiesOfClass(LivingEntity.class, var1);
		Iterator var7 = var6.iterator();

		while(true) {
			LivingEntity var8;
			do {
				do {
					do {
						do {
							if (!var7.hasNext()) {
								((Player) (Object)this).level.playSound((Player)null, ((Player) (Object)this).getX(), ((Player) (Object)this).getY(), ((Player) (Object)this).getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, ((Player) (Object)this).getSoundSource(), 1.0F, 1.0F);
								if (((Player) (Object)this).level instanceof ServerLevel) {
									double var11 = (double)(-Mth.sin(((Player) (Object)this).getYRot() * 0.017453292F));
									double var12 = (double)Mth.cos(((Player) (Object)this).getYRot() * 0.017453292F);
									((ServerLevel)((Player) (Object)this).level).sendParticles(ParticleTypes.SWEEP_ATTACK, ((Player) (Object)this).getX() + var11, ((Player) (Object)this).getY() + (double)((Player) (Object)this).getBbHeight() * 0.5, ((Player) (Object)this).getZ() + var12, 0, var11, 0.0, var12, 0.0);
								}

								return;
							}

							var8 = (LivingEntity)var7.next();
						} while(var8 == ((Player) (Object)this));
					} while(var8 == var4);
				} while(((Player) (Object)this).isAlliedTo(var8));
			} while(var8 instanceof ArmorStand && ((ArmorStand)var8).isMarker());

			float var9 = var2 + var8.getBbWidth() * 0.5F;
			if (((Player) (Object)this).distanceToSqr(var8) < (double)(var9 * var9)) {
				var8.knockback(0.4F, (double)Mth.sin(((Player) (Object)this).getYRot() * 0.017453292F), (double)(-Mth.cos(((Player) (Object)this).getYRot() * 0.017453292F)));
				var8.hurt(DamageSource.playerAttack(((Player) (Object)this)), var5);
			}
		}
	}

}
