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
import net.minecraft.world.item.SwordItem;
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
		((Player) (Object)this).getAttribute(NewAttributes.BASE_REACH).setBaseValue(6.0);
		((Player) (Object)this).getAttribute(NewAttributes.ATTACK_REACH).setBaseValue(2.5);
		((Player) (Object)this).getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2.0);
	}

	/**
	 * @author zOnlyKroks
	 */
	@Overwrite()
	public static AttributeSupplier.Builder createAttributes() {
		return LivingEntity.createLivingAttributes().add(Attributes.ATTACK_DAMAGE, 2.0)
				.add(Attributes.MOVEMENT_SPEED, 0.1F)
				.add(NewAttributes.ATTACK_SPEED)
				.add(Attributes.LUCK)
				.add(NewAttributes.BASE_REACH)
				.add(NewAttributes.ATTACK_REACH);
	}

	/**
	 * @author zOnlyKroks
	 * @reason change attacks
	 */
	@Overwrite()
	public void attack(Entity target) {
		if (target.isAttackable() && isAttackAvailable(1.0F)) {
			if (!target.skipAttackInteraction(((Player) (Object)this))) {
				float f = (float)((Player) (Object)this).getAttributeValue(Attributes.ATTACK_DAMAGE);
				float g;
				if (target instanceof LivingEntity) {
					g = EnchantmentHelper.getDamageBonus(((Player) (Object)this).getMainHandItem(), ((LivingEntity)target).getMobType());
				} else {
					g = EnchantmentHelper.getDamageBonus(((Player) (Object)this).getMainHandItem(), MobType.UNDEFINED);
				}
				float currentAttackReach = this.getCurrentAttackReach(1.0F);

				float h = this.getAttackStrengthScale(1.0F);
				f *= 0.2F + h * h * 0.8F;
				g *= h;
				((Player) (Object)this).resetAttackStrengthTicker();
				if (f > 0.0F || g > 0.0F) {
					boolean bl = h > 0.9F;
					boolean bl2 = false;
					int i = 0;
					i += EnchantmentHelper.getKnockbackBonus(((Player) (Object)this));
					if (((Player) (Object)this).isSprinting() && bl) {
						((Player) (Object)this).level.playSound(null, ((Player) (Object)this).getX(), ((Player) (Object)this).getY(), ((Player) (Object)this).getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, ((Player) (Object)this).getSoundSource(), 1.0F, 1.0F);
						++i;
						bl2 = true;
					}

					boolean bl3 = bl
							&& ((Player) (Object)this).fallDistance > 0.0F
							&& !((Player) (Object) this).isOnGround()
							&& !((Player) (Object)this).onClimbable()
							&& !((Player) (Object)this).isInWater()
							&& !((Player) (Object)this).hasEffect(MobEffects.BLINDNESS)
							&& !((Player) (Object)this).isPassenger()
							&& target instanceof LivingEntity;
					bl3 = bl3 && !((Player) (Object)this).isSprinting();
					if (bl3) {
						f *= 1.5F;
					}

					f += g;
					boolean bl4 = false;
					double d = (((Player) (Object)this).walkDist - ((Player) (Object)this).walkDistO);
					if (bl && !bl3 && !bl2 && ((Player) (Object) this).isOnGround() && d < (double)((Player) (Object)this).getSpeed()) {
						ItemStack itemStack = ((Player) (Object)this).getItemInHand(InteractionHand.MAIN_HAND);
						if (itemStack.getItem() instanceof SwordItem && checkSweepAttack()) {
							bl4 = true;
						}
					}

					float j = 0.0F;
					boolean bl5 = false;
					int k = EnchantmentHelper.getFireAspect(((Player) (Object)this));
					if (target instanceof LivingEntity) {
						j = ((LivingEntity)target).getHealth();
						if (k > 0 && !target.isOnFire()) {
							bl5 = true;
							target.setSecondsOnFire(1);
						}
					}

					Vec3 vec3 = target.getDeltaMovement();
					boolean bl6 = target.hurt(DamageSource.playerAttack(((Player) (Object)this)), f);
					if (bl6) {
						if (i > 0) {
							if (target instanceof LivingEntity) {
								((LivingEntity)target)
										.knockback(
												(double)((float)i * 0.5F),
												(double)Mth.sin(((Player) (Object)this).getYRot() * (float) (Math.PI / 180.0)),
												(double)(-Mth.cos(((Player) (Object)this).getYRot() * (float) (Math.PI / 180.0)))
										);
							} else {
								target.push(
										(double)(-Mth.sin(((Player) (Object)this).getYRot() * (float) (Math.PI / 180.0)) * (float)i * 0.5F),
										0.1,
										(double)(Mth.cos(((Player) (Object)this).getYRot() * (float) (Math.PI / 180.0)) * (float)i * 0.5F)
								);
							}

							((Player) (Object)this).setDeltaMovement(((Player) (Object)this).getDeltaMovement().multiply(0.6, 1.0, 0.6));
							((Player) (Object)this).setSprinting(false);
						}

						if (bl4) {
							float l = 1.0F + EnchantmentHelper.getSweepingDamageRatio(((Player) (Object)this)) * f;
							AABB box = target.getBoundingBox().inflate(1.0, 0.25, 1.0);

							for(LivingEntity livingEntity : ((Player) (Object)this).level.getEntitiesOfClass(LivingEntity.class, box)) {
								if (livingEntity != ((Player) (Object)this)
										&& livingEntity != target
										&& !((Player) (Object)this).isAlliedTo(livingEntity)
										&& (!(livingEntity instanceof ArmorStand) || !((ArmorStand)livingEntity).isMarker())
										&& ((Player) (Object)this).distanceToSqr(livingEntity) < 9.0) {
									livingEntity.knockback(
											0.4F, (double)Mth.sin(((Player) (Object)this).getYRot() * (float) (Math.PI / 180.0)), (double)(-Mth.cos(((Player) (Object)this).getYRot() * (float) (Math.PI / 180.0)))
									);
									livingEntity.hurt(DamageSource.playerAttack(((Player) (Object)this)), l);
								}
							}

							((Player) (Object)this).level.playSound(null, ((Player) (Object)this).getX(), ((Player) (Object)this).getY(), ((Player) (Object)this).getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, ((Player) (Object)this).getSoundSource(), 1.0F, 1.0F);
							this.betterSweepAttack(box, currentAttackReach, f, target);
						}

						if (target instanceof ServerPlayer && target.hurtMarked) {
							((ServerPlayer)target).connection.send(new ClientboundSetEntityMotionPacket(target));
							target.hurtMarked = false;
							target.setDeltaMovement(vec3);
						}

						if (bl3) {
							((Player) (Object)this).level.playSound(null, ((Player) (Object)this).getX(), ((Player) (Object)this).getY(), ((Player) (Object)this).getZ(), SoundEvents.PLAYER_ATTACK_CRIT, ((Player) (Object)this).getSoundSource(), 1.0F, 1.0F);
							((Player) (Object)this).crit(target);
						}

						if (!bl3 && !bl4) {
							if (bl) {
								((Player) (Object)this).level.playSound(null, ((Player) (Object)this).getX(), ((Player) (Object)this).getY(), ((Player) (Object)this).getZ(), SoundEvents.PLAYER_ATTACK_STRONG, ((Player) (Object)this).getSoundSource(), 1.0F, 1.0F);
							} else {
								((Player) (Object)this).level.playSound(null, ((Player) (Object)this).getX(), ((Player) (Object)this).getY(), ((Player) (Object)this).getZ(), SoundEvents.PLAYER_ATTACK_WEAK, ((Player) (Object)this).getSoundSource(), 1.0F, 1.0F);
							}
						}

						if (g > 0.0F) {
							((Player) (Object)this).magicCrit(target);
						}

						((Player) (Object)this).setLastHurtMob(target);
						if (target instanceof LivingEntity) {
							EnchantmentHelper.doPostHurtEffects((LivingEntity)target, ((Player) (Object)this));
						}

						EnchantmentHelper.doPostDamageEffects(((Player) (Object)this), target);
						ItemStack itemStack2 = ((Player) (Object)this).getMainHandItem();
						Entity entity = target;
						if (target instanceof EnderDragonPart) {
							entity = ((EnderDragonPart)target).parentMob;
						}

						if (!((Player) (Object)this).level.isClientSide && !itemStack2.isEmpty() && entity instanceof LivingEntity) {
							itemStack2.hurtEnemy((LivingEntity)entity, ((Player) (Object)this));
							if (itemStack2.isEmpty()) {
								((Player) (Object)this).setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
							}
						}

						if (target instanceof LivingEntity) {
							float m = j - ((LivingEntity)target).getHealth();
							((Player) (Object)this).awardStat(Stats.DAMAGE_DEALT, Math.round(m * 10.0F));
							if (k > 0) {
								target.setSecondsOnFire(k * 4);
							}

							if (((Player) (Object)this).level instanceof ServerLevel && m > 2.0F) {
								int n = (int)((double)m * 0.5);
								((ServerLevel)((Player) (Object)this).level).sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(0.5), target.getZ(), n, 0.1, 0.0, 0.1, 0.2);
							}
						}

						((Player) (Object)this).causeFoodExhaustion(0.1F);
					} else {
						((Player) (Object)this).level.playSound(null, ((Player) (Object)this).getX(), ((Player) (Object)this).getY(), ((Player) (Object)this).getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, ((Player) (Object)this).getSoundSource(), 1.0F, 1.0F);
						if (bl5) {
							target.clearFire();
						}
					}
				}

			}
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
