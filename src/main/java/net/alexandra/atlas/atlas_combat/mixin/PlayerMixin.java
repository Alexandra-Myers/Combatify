package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.item.NewAttributes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
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
import org.objectweb.asm.Opcodes;
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
	@Shadow
	public abstract InteractionResult interactOn(Entity entity, InteractionHand hand);

	@Unique
	public boolean missedAttackRecovery;

	private static final UUID MAGIC_ATTACK_DAMAGE_UUID = UUID.fromString("13C4E5B5-0F72-4359-AB1C-625F9DF5AA2B");
	@Unique
	public final Player player = ((Player) (Object)this);

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	public void readAdditionalSaveData(CompoundTag nbt, CallbackInfo ci) {
		player.getAttribute(NewAttributes.BASE_REACH).setBaseValue(6.0);
		player.getAttribute(NewAttributes.ATTACK_REACH).setBaseValue(6.0);
		player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2.0);
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

	@Redirect(method = "tick", at = @At(value = "FIELD",target = "Lnet/minecraft/world/entity/player/Player;attackStrengthTicker:I",opcode = Opcodes.PUTFIELD))
	public void tickInject(Player instance, int value) {
		--instance.attackStrengthTicker;
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isSameIgnoreDurability(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
	public boolean redirectDurability(ItemStack left, ItemStack right) {
		return true;
	}

	/**
	 * @author zOnlyKroks
	 * @reason change attacks
	 */
	@Overwrite()
	public void attack(Entity target) {
		if (target.isAttackable() && isAttackAvailable(1.0F)) {
			if (!target.skipAttackInteraction(player)) {
				float attackDamage = (float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
				float attackDamageBonus;
				if (target instanceof LivingEntity livingEntity) {
					attackDamageBonus = EnchantmentHelper.getDamageBonus(player.getMainHandItem(), livingEntity.getMobType());
				} else {
					attackDamageBonus = EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.UNDEFINED);
				}
				float currentAttackReach = this.getCurrentAttackReach(1.0F);

				float attackStrengthScale = this.getAttackStrengthScale(1.0F);
				attackDamage *= 0.2F + attackStrengthScale * attackStrengthScale * 0.8F;
				attackDamageBonus *= attackStrengthScale;
				this.player.resetAttackStrengthTicker();
				if (attackDamage > 0.0F || attackDamageBonus > 0.0F) {
					boolean bl = attackStrengthScale > 0.9F;
					boolean bl2 = false;
					int knockbackBonus = 0;
					knockbackBonus += EnchantmentHelper.getKnockbackBonus(player);
					if (player.isSprinting() && bl) {
						player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, player.getSoundSource(), 1.0F, 1.0F);
						++knockbackBonus;
						bl2 = true;
					}

					boolean isCrit = bl
							&& player.fallDistance > 0.0F
							&& !player.isOnGround()
							&& !player.onClimbable()
							&& !player.isInWater()
							&& !player.hasEffect(MobEffects.BLINDNESS)
							&& !player.isPassenger()
							&& target instanceof LivingEntity;
					isCrit = isCrit && !player.isSprinting();
					if (isCrit) {
						attackDamage *= 1.5F;
					}

					attackDamage += attackDamageBonus;
					boolean bl4 = false;
					double d = (player.walkDist - player.walkDistO);
					if (bl && !isCrit && !bl2 && player.isOnGround() && d < player.getSpeed()) {
						ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
						if (itemStack.getItem() instanceof SwordItem && checkSweepAttack()) {
							bl4 = true;
						}
					}

					float j = 0.0F;
					boolean bl5 = false;
					int getFireAspectLvL = EnchantmentHelper.getFireAspect(player);
					if (target instanceof LivingEntity livingEntity) {
						j = livingEntity.getHealth();
						if (getFireAspectLvL > 0 && !target.isOnFire()) {
							bl5 = true;
							target.setSecondsOnFire(1);
						}
					}

					Vec3 vec3 = target.getDeltaMovement();
					boolean bl6 = target.hurt(DamageSource.playerAttack(player), attackDamage);
					if (bl6) {
						if (knockbackBonus > 0) {
							if (target instanceof LivingEntity livingEntity) {
								livingEntity
										.knockback((
												knockbackBonus * 0.5F),
												Mth.sin(player.getYRot() * (float) (Math.PI / 180.0)),
												-Mth.cos(player.getYRot() * (float) (Math.PI / 180.0))
										);
							} else {
								target.push(
										(-Mth.sin(player.getYRot() * (float) (Math.PI / 180.0)) * knockbackBonus * 0.5F),
										0.1,
										(Mth.cos(player.getYRot() * (float) (Math.PI / 180.0)) * knockbackBonus * 0.5F)
								);
							}

							player.setDeltaMovement(player.getDeltaMovement().multiply(0.6, 1.0, 0.6));
							player.setSprinting(false);
						}

						if (bl4) {
							float l = 1.0F + EnchantmentHelper.getSweepingDamageRatio(player) * attackDamage;
							AABB box = target.getBoundingBox().inflate(1.0, 0.25, 1.0);

							for(LivingEntity livingEntity : player.level.getEntitiesOfClass(LivingEntity.class, box)) {
								if (livingEntity != player
										&& livingEntity != target
										&& !player.isAlliedTo(livingEntity)
										&& (!(livingEntity instanceof ArmorStand armorStand) || !armorStand.isMarker())
										&& player.distanceToSqr(livingEntity) < 9.0) {
									livingEntity.knockback(
											0.4F, Mth.sin(player.getYRot() * (float) (Math.PI / 180.0)), (-Mth.cos(player.getYRot() * (float) (Math.PI / 180.0)))
									);
									livingEntity.hurt(DamageSource.playerAttack(player), l);
								}
							}

							player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
							this.betterSweepAttack(box, currentAttackReach, attackDamage, target);
						}

						if (target instanceof ServerPlayer serverPlayer && target.hurtMarked) {
							serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(target));
							target.hurtMarked = false;
							target.setDeltaMovement(vec3);
						}

						if (isCrit) {
							player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, player.getSoundSource(), 1.0F, 1.0F);
							player.crit(target);
						}

						if (!isCrit && !bl4) {
							if (bl) {
								player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, player.getSoundSource(), 1.0F, 1.0F);
							} else {
								player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, player.getSoundSource(), 1.0F, 1.0F);
							}
						}

						if (attackDamageBonus > 0.0F) {
							player.magicCrit(target);
						}

						player.setLastHurtMob(target);
						if (target instanceof LivingEntity livingEntity) {
							EnchantmentHelper.doPostHurtEffects(livingEntity, player);
						}

						EnchantmentHelper.doPostDamageEffects(player, target);
						ItemStack itemStack2 = player.getMainHandItem();
						Entity entity = target;
						if (target instanceof EnderDragonPart enderDragonPart) {
							entity = enderDragonPart.parentMob;
						}

						if (!player.level.isClientSide && !itemStack2.isEmpty() && entity instanceof LivingEntity livingEntity) {
							itemStack2.hurtEnemy(livingEntity, player);
							if (itemStack2.isEmpty()) {
								player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
							}
						}

						if (target instanceof LivingEntity livingEntity) {
							float m = j - livingEntity.getHealth();
							player.awardStat(Stats.DAMAGE_DEALT, Math.round(m * 10.0F));
							if (getFireAspectLvL > 0) {
								target.setSecondsOnFire(getFireAspectLvL * 4);
							}

							if (player.level instanceof ServerLevel serverLevel && m > 2.0F) {
								int n = (int)(m * 0.5);
								serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(0.5), target.getZ(), n, 0.1, 0.0, 0.1, 0.2);
							}
						}

						player.causeFoodExhaustion(0.1F);
					} else {
						player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, player.getSoundSource(), 1.0F, 1.0F);
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
			player.attackStrengthTicker = 10;
		}else {
			player.attackStrengthTicker = 0;
		}
	}

	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	public float getCurrentItemAttackStrengthDelay() {
		float var1 = (float)player.getAttribute(NewAttributes.ATTACK_SPEED).getValue() - 1.5F;
		var1 = Mth.clamp(var1, 0.1F, 1024.0F);
		return (1.0F / var1 * 20.0F + 0.5F);
	}

	public float getCurrentAttackReach(float var1) {
		float var2 = 0.0F;
		float var3 = this.getAttackStrengthScale(var1);
		if (var3 > 1.95F && !player.isCrouching()) {
			var2 = 1.0F;
		}

		return (float) (player.getAttribute(NewAttributes.ATTACK_REACH).getValue() + var2);
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
		return defaultTicker == 0 ? 2.0F : Mth.clamp(2.0F * (1.0F - (player.attackStrengthTicker - var1) / defaultTicker), 0.0F, 2.0F);
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
			return this.missedAttackRecovery && defaultTicker - (player.attackStrengthTicker - var1) > 4.0F;
		}
	}

	protected boolean checkSweepAttack() {
		return this.getAttackStrengthScale(1.0F) > 1.95F && EnchantmentHelper.getSweepingDamageRatio(player) > 0.0F;
	}

	public void betterSweepAttack(AABB var1, float var2, float var3, Entity var4) {
		float sweepingDamageRatio = 1.0F + EnchantmentHelper.getSweepingDamageRatio(player) * var3;
		List<LivingEntity> livingEntities = player.level.getEntitiesOfClass(LivingEntity.class, var1);
		Iterator<LivingEntity> livingEntityIterator = livingEntities.iterator();

		while(true) {
			LivingEntity var8;
			do {
				do {
					do {
						do {
							if (!livingEntityIterator.hasNext()) {
								player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
								if (player.level instanceof ServerLevel serverLevel) {
									double var11 = (-Mth.sin(player.getYRot() * 0.017453292F));
									double var12 = Mth.cos(player.getYRot() * 0.017453292F);
									serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX() + var11, player.getY() + player.getBbHeight() * 0.5, player.getZ() + var12, 0, var11, 0.0, var12, 0.0);
								}

								return;
							}

							var8 = livingEntityIterator.next();
						} while(var8 == player);
					} while(var8 == var4);
				} while(player.isAlliedTo(var8));
			} while(var8 instanceof ArmorStand armorStand && armorStand.isMarker());

			float var9 = var2 + var8.getBbWidth() * 0.5F;
			if (player.distanceToSqr(var8) < (var9 * var9)) {
				var8.knockback(0.4F, Mth.sin(player.getYRot() * 0.017453292F), (-Mth.cos(player.getYRot() * 0.017453292F)));
				var8.hurt(DamageSource.playerAttack(player), sweepingDamageRatio);
			}
		}
	}

}
