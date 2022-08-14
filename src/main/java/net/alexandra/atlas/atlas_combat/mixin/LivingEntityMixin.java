package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.enchantment.CustomEnchantmentHelper;
import net.alexandra.atlas.atlas_combat.extensions.IShieldItem;
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
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityExtensions {

	public LivingEntityMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Shadow
	public abstract boolean checkTotemDeathProtection(DamageSource source);

	@Shadow
	@Nullable
	public abstract SoundEvent getDeathSound();

	@Shadow
	public abstract float getSoundVolume();

	@Shadow
	public abstract void playHurtSound(DamageSource source);

	@Shadow
	@Nullable
	public DamageSource lastDamageSource;

	@Shadow
	public long lastDamageStamp;

	@Shadow
	public abstract void hurtCurrentlyUsedShield(float amount);

	@Shadow
	public abstract void blockUsingShield(LivingEntity attacker);

	@Shadow
	@Nullable
	public Player lastHurtByPlayer;

	@Shadow
	public int lastHurtByPlayerTime;

	@Shadow
	public float lastHurt;

	@Shadow
	public abstract void actuallyHurt(DamageSource source, float amount);

	@Shadow
	public abstract double getAttributeValue(Attribute attribute);

	@Shadow
	protected abstract void hurtHelmet(DamageSource par1, float par2);

	@Shadow
	protected int noActionTime;

	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	public boolean isBlocking() {
		return !this.getBlockingItem().isEmpty();
	}
	/**
	 * @author zOnlyKroks
	 * @reason
	 */
	@Overwrite()
	public void blockedByShield(LivingEntity target) {
		newKnockback(0.5F, target.getX() - ((LivingEntity)(Object)this).getX(), target.getZ() - ((LivingEntity)(Object)this).getZ());
		if (((LivingEntity)(Object)this).getMainHandItem().getItem() instanceof AxeItem) {
			float damage = 1.6F + (float) CustomEnchantmentHelper.getChopping(((LivingEntity) (Object)this)) * 0.5F;
			if(target instanceof PlayerExtensions player) {
				player.customShieldInteractions(damage);
			}
		}
	}

	/**
	 * @author zOnlyKroks
	 */
	@Inject(method = "hurt", at = @At("HEAD"),cancellable = true)
	public void hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity thisEntity = ((LivingEntity)(Object)this);
		if (this.isInvulnerableTo(source)) {
			cir.setReturnValue(false);
			cir.cancel();
		} else if (this.level.isClientSide) {
			cir.setReturnValue(false);
			cir.cancel();
		} else if (thisEntity.isDeadOrDying()) {
			cir.setReturnValue(false);
			cir.cancel();
		} else if (source.isFire() && thisEntity.hasEffect(MobEffects.FIRE_RESISTANCE)) {
			cir.setReturnValue(false);
			cir.cancel();
		} else {
			if (thisEntity.isSleeping() && !this.level.isClientSide) {
				thisEntity.stopSleeping();
			}

			noActionTime = 0;
			float f = amount;
			boolean bl = false;
			float g = 0.0F;
			Entity entity;
			if (amount > 0.0F && this.isDamageSourceBlocked(source)) {
				float blockStrength = ShieldUtils.getShieldBlockDamageValue(this.getBlockingItem());
				if(source.isExplosion() || source.isProjectile()) {
					hurtCurrentlyUsedShield(amount);
					amount = 0.0F;
					g = f;
				}else if (blockStrength >= amount) {
					hurtCurrentlyUsedShield(amount);
					amount -= blockStrength;
					g = f - amount;
				} else if (blockStrength < amount) {
					hurtCurrentlyUsedShield(blockStrength);
					amount -= blockStrength;
					g = f - blockStrength;
				}
				if (!source.isProjectile() && !source.isExplosion()) {
					entity = source.getDirectEntity();
					if (entity instanceof LivingEntity) {
						this.blockUsingShield((LivingEntity)entity);
					}
				}
				bl = true;
			}
			Entity entity2 = source.getEntity();
			int invulnerableTime = 10;
			if (entity2 != null && entity2 instanceof Player) {
				Player player = (Player)entity2;
				invulnerableTime = Math.min(((PlayerExtensions)player).getAttackDelay(player), invulnerableTime);
			}

			if (source.isProjectile()) {
				invulnerableTime = 0;
			}
			thisEntity.animationSpeed = 1.5F;
			boolean bl2 = true;
			if (this.invulnerableTime > 0) {
				if (amount <= this.lastHurt) {
					cir.setReturnValue(false);
					cir.cancel();
				}

				this.actuallyHurt(source, amount - this.lastHurt);
				this.lastHurt = amount;
				bl2 = false;
			} else if(source.isFire()) {
				this.lastHurt = amount;
				this.invulnerableTime = 15;
				this.actuallyHurt(source, amount);
				thisEntity.hurtDuration = 10;
				thisEntity.hurtTime = thisEntity.hurtDuration;
			} else {
				this.lastHurt = amount;
				this.invulnerableTime = invulnerableTime;
				this.actuallyHurt(source, amount);
				thisEntity.hurtDuration = 10;
				thisEntity.hurtTime = thisEntity.hurtDuration;
			}

			if (source.isDamageHelmet() && !thisEntity.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
				hurtHelmet(source, amount);
				amount *= 0.75F;
			}

			thisEntity.hurtDir = 0.0F;
			if (entity2 != null) {
				if (entity2 instanceof LivingEntity && !source.isNoAggro()) {
					thisEntity.setLastHurtByMob((LivingEntity)entity2);
				}

				if (entity2 instanceof Player) {
					this.lastHurtByPlayerTime = 100;
					this.lastHurtByPlayer = (Player)entity2;
				} else if (entity2 instanceof Wolf wolf && wolf.isTame()) {
					this.lastHurtByPlayerTime = 100;
					LivingEntity livingEntity = wolf.getOwner();
					if (livingEntity != null && livingEntity.getType() == EntityType.PLAYER) {
						this.lastHurtByPlayer = (Player)livingEntity;
					} else {
						this.lastHurtByPlayer = null;
					}
				}
			}

			if (bl2) {
				if (bl) {
					this.level.broadcastEntityEvent(thisEntity, (byte)29);
				} else if (source instanceof EntityDamageSource && ((EntityDamageSource)source).isThorns()) {
					this.level.broadcastEntityEvent(thisEntity, (byte)33);
				} else {
					byte b;
					if (source == DamageSource.DROWN) {
						b = 36;
					} else if (source.isFire()) {
						b = 37;
					} else if (source == DamageSource.SWEET_BERRY_BUSH) {
						b = 44;
					} else if (source == DamageSource.FREEZE) {
						b = 57;
					} else {
						b = 2;
					}

					this.level.broadcastEntityEvent(thisEntity, b);
				}

				if (source != DamageSource.DROWN && (!bl || amount > 0.0F)) {
					this.markHurt();
				}

				if (entity2 != null) {
					double d = entity2.getX() - this.getX();

					double e;
					for(e = entity2.getZ() - this.getZ(); d * d + e * e < 1.0E-4; e = (Math.random() - Math.random()) * 0.01) {
						d = (Math.random() - Math.random()) * 0.01;
					}

					thisEntity.hurtDir = (float)(Mth.atan2(e, d) * 180.0F / (float)Math.PI - (double)this.getYRot());
					newKnockback(0.5F, d, e);
				} else {
					thisEntity.hurtDir = (float)((int)(Math.random() * 2.0) * 180);
				}
			}

			if (thisEntity.isDeadOrDying()) {
				if (!this.checkTotemDeathProtection(source)) {
					SoundEvent soundEvent = this.getDeathSound();
					if (bl2 && soundEvent != null) {
						this.playSound(soundEvent, this.getSoundVolume(), thisEntity.getVoicePitch());
					}

					thisEntity.die(source);
				}
			} else if (bl2) {
				this.playHurtSound(source);
			}

			boolean bl3 = !bl || amount > 0.0F;
			if (bl3) {
				this.lastDamageSource = source;
				this.lastDamageStamp = this.level.getGameTime();
			}

			if (((LivingEntity)(Object)this) instanceof ServerPlayer) {
				CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((ServerPlayer)thisEntity, source, f, amount, bl);
				if (g > 0.0F && g < 3.4028235E37F) {
					((ServerPlayer)thisEntity).awardStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(g * 10.0F));
				}
			}

			if (entity2 instanceof ServerPlayer) {
				CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayer)entity2, thisEntity, source, f, amount, bl);
			}
			cir.setReturnValue(bl3);
			cir.cancel();
		}
	}

	/**
	 * @author
	 * @reason
	 */
	@Override
	public void newKnockback(float var1, double var2, double var4) {
		double var6 = getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
		ItemStack var8 = this.getBlockingItem();
		if (!var8.isEmpty()) {
			var6 = Math.min(1.0, var6 + (double)((IShieldItem)var8.getItem()).getShieldKnockbackResistanceValue(var8));
		}

		var1 = (float)((double)var1 * (1.0 - var6));
		if (!(var1 <= 0.0F)) {
			this.hasImpulse = true;
			Vec3 var9 = this.getDeltaMovement();
			Vec3 var10 = (new Vec3(var2, 0.0, var4)).normalize().scale((double)var1);
			this.setDeltaMovement(var9.x / 2.0 - var10.x, this.onGround ? Math.min(0.4, (double)var1 * 0.75) : Math.min(0.4, var9.y + (double)var1 * 0.5), var9.z / 2.0 - var10.z);
		}
	}
	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	public boolean isDamageSourceBlocked(DamageSource source) {
		Entity entity = source.getDirectEntity();
		boolean bl = false;
		if (entity instanceof AbstractArrow) {
			AbstractArrow arrow = (AbstractArrow)entity;
			if (arrow.getPierceLevel() > 0) {
				bl = true;
			}
		}

		if (!source.isBypassArmor() && this.isBlocking() && !bl) {
			Vec3 sourcePos = source.getSourcePosition();
			if (sourcePos != null) {
				Vec3 currentVector = this.getViewVector(1.0F);
				if (currentVector.y > -0.99 && currentVector.y < 0.99) {
					currentVector = (new Vec3(currentVector.x, 0.0, currentVector.z)).normalize();
					Vec3 sourceVector = sourcePos.vectorTo(this.position());
					sourceVector = (new Vec3(sourceVector.x, 0.0, sourceVector.z)).normalize();
					if (sourceVector.dot(currentVector) * 3.1415927410125732 < -0.8726646304130554) {
						return true;
					}
				}
			}
		}

		return false;
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
			for(InteractionHand hand : InteractionHand.values()) {
				ItemStack var1 = thisLivingEntity.getItemInHand(hand);
				if (!var1.isEmpty() && var1.getItem().getUseAnimation(var1) == UseAnim.BLOCK && !this.isItemOnCooldown(var1)) {
					return var1;
				}
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
