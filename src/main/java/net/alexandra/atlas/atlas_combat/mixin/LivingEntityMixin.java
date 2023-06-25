package net.alexandra.atlas.atlas_combat.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.enchantment.CustomEnchantmentHelper;
import net.alexandra.atlas.atlas_combat.extensions.*;
import net.alexandra.atlas.atlas_combat.util.BlockingType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(value = LivingEntity.class, priority = 1400)
public abstract class LivingEntityMixin extends Entity implements LivingEntityExtensions {

	public LivingEntityMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Unique
	boolean isParry = false;
	@Unique
	public int isParryTicker = 0;

	@Unique
	public Entity enemy;
	@Unique
	LivingEntity thisEntity = LivingEntity.class.cast(this);

	@Shadow
	public abstract double getAttributeValue(Attribute attribute);

	@Shadow
	public abstract void hurtArmor(DamageSource damageSource, float v);

	@Shadow
	public abstract int getArmorValue();

	@Shadow
	public abstract boolean hasEffect(MobEffect mobEffect);

	@Shadow
	public abstract MobEffectInstance getEffect(MobEffect mobEffect);


	@Shadow
	public abstract boolean isDamageSourceBlocked(DamageSource damageSource);

	@ModifyReturnValue(method = "isBlocking", at = @At(value="RETURN"))
	public boolean isBlocking(boolean original) {
		return !this.getBlockingItem().isEmpty();
	}

	@Inject(method = "blockedByShield", at = @At(value="HEAD"), cancellable = true)
	public void blockedByShield(LivingEntity target, CallbackInfo ci) {
		double x = target.getX() - this.getX();
		double z = target.getZ() - this.getZ();
		Item blockingItem = ((LivingEntityExtensions)target).getBlockingItem().getItem();
		if(blockingItem instanceof IShieldItem shieldItem && shieldItem.getBlockingType().equals(BlockingType.SWORD)) {
			((LivingEntityExtensions)target).newKnockback(0.4, x, z);
			newKnockback(0.4, x, z);
			ci.cancel();
			return;
		}
		((LivingEntityExtensions)target).newKnockback(0.5, x, z);
		newKnockback(0.5, x, z);
		if (((LivingEntity)(Object)this).getMainHandItem().getItem() instanceof AxeItem) {
			float damage = 1.6F + (float) CustomEnchantmentHelper.getChopping(((LivingEntity) (Object)this)) * 0.5F;
			if(target instanceof PlayerExtensions player) {
				player.customShieldInteractions(damage, blockingItem);
			}
		}
		ci.cancel();
	}
	@Inject(method = "getDamageAfterArmorAbsorb", at = @At(value = "HEAD"), cancellable = true)
	public void addPiercing(DamageSource source, float f, CallbackInfoReturnable<Float> cir) {
		if(source.getEntity() instanceof LivingEntity livingEntity) {
			Item item = livingEntity.getItemInHand(InteractionHand.MAIN_HAND).getItem();
			if(item instanceof PiercingItem piercingItem) {
				double d = piercingItem.getPiercingLevel();
				cir.setReturnValue(getNewDamageAfterArmorAbsorb(source, f, d));
			}
		}
	}
	@Inject(method = "getDamageAfterMagicAbsorb", at = @At(value = "HEAD"), cancellable = true)
	public void addPiercing1(DamageSource source, float f, CallbackInfoReturnable<Float> cir) {
		if(source.getEntity() instanceof LivingEntity livingEntity) {
			Item item = livingEntity.getItemInHand(InteractionHand.MAIN_HAND).getItem();
			if(item instanceof PiercingItem piercingItem) {
				double d = piercingItem.getPiercingLevel();
				cir.setReturnValue(getNewDamageAfterMagicAbsorb(source, f, d));
			}
		}
	}
	@ModifyConstant(method = "handleEntityEvent", constant = @Constant(intValue = 20, ordinal = 0))
	private int syncInvulnerability(int x) {
		return 10;
	}
	@Override
	public void setEnemy(Entity enemy) {
		this.enemy = enemy;
	}

	@Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isDamageSourceBlocked(Lnet/minecraft/world/damagesource/DamageSource;)Z"))
	public boolean shield(LivingEntity instance, DamageSource source, @Local(ordinal = 0) LocalFloatRef amount, @Local(ordinal = 1) LocalFloatRef f, @Local(ordinal = 2) LocalFloatRef g, @Local(ordinal = 0) LocalBooleanRef bl) {
		Entity entity = null;
		if (amount.get() > 0.0F && isDamageSourceBlocked(source)) {
			if(this.getBlockingItem().getItem() instanceof IShieldItem shieldItem) {
				shieldItem.block(instance, entity, this.getBlockingItem(), source, amount, f, g, bl);
			}
		}
		return false;
	}
	@ModifyConstant(method = "hurt", constant = @Constant(intValue = 20, ordinal = 0))
	public int changeIFrames(int constant, @Local(ordinal = 0) final DamageSource source, @Local(ordinal = 0) final float amount) {
		Entity entity2 = source.getEntity();
		enemy = entity2;
		int invulnerableTime = 10;
		if (entity2 instanceof Player player) {
			invulnerableTime = (int) Math.min(player.getCurrentItemAttackStrengthDelay(), invulnerableTime);
		}

		if (source.isProjectile()) {
			invulnerableTime = 0;
		}
		return invulnerableTime;

	}
	@Inject(method = "hurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;invulnerableTime:I", ordinal = 0))
	public void injectEatingInterruption(DamageSource source, float f, CallbackInfoReturnable<Boolean> cir) {
		if(thisEntity.isUsingItem() && thisEntity.getUseItem().isEdible() && !source.isFire() && !source.isMagic() && !source.isFall() && AtlasCombat.CONFIG.eatingInterruption()) {
			thisEntity.stopUsingItem();
		}
	}
	@ModifyExpressionValue(method = "hurt", at = @At(value = "CONSTANT", args = "floatValue=10.0F", ordinal = 0))
	public float changeIFrames(float constant) {
		return constant - 10;
	}
	@Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
	public void modifyKB(LivingEntity instance, double d, double e, double f, @Local(ordinal = 0) final DamageSource source) {
		if ((AtlasCombat.CONFIG.fishingHookKB() && source.getDirectEntity() instanceof FishingHook) || (!source.isProjectile() && AtlasCombat.CONFIG.midairKB())) {
			projectileKnockback(0.4, e, f);
		} else {
			newKnockback(0.4, e, f);
		}
	}

	@Override
	public void newKnockback(double strength, double x, double z) {
		double var6 = getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
		ItemStack var8 = this.getBlockingItem();
		if (!var8.isEmpty()) {
			var6 = Math.min(1.0, var6 + ((IShieldItem)var8.getItem()).getShieldKnockbackResistanceValue(var8));
		}

		strength = strength * (1.0 - var6);
		if (!(strength <= 0.0F)) {
			this.hasImpulse = true;
			Vec3 var9 = this.getDeltaMovement();
			Vec3 var10 = (new Vec3(x, 0.0, z)).normalize().scale(strength);
			this.setDeltaMovement(var9.x / 2.0 - var10.x, this.onGround ? Math.min(0.4, strength * 0.75) : Math.min(0.4, var9.y + strength * 0.5), var9.z / 2.0 - var10.z);
		}
	}
	@Override
	public void projectileKnockback(double strength, double x, double z) {
		double var6 = getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
		ItemStack var8 = this.getBlockingItem();
		if (!var8.isEmpty()) {
			var6 = Math.min(1.0, var6 + ((IShieldItem)var8.getItem()).getShieldKnockbackResistanceValue(var8));
		}

		strength = strength * (1.0 - var6);
		if (!(strength <= 0.0F)) {
			this.hasImpulse = true;
			Vec3 var9 = this.getDeltaMovement();
			Vec3 var10 = (new Vec3(x, 0.0, z)).normalize().scale(strength);
			this.setDeltaMovement(var9.x / 2.0 - var10.x, Math.min(0.4, strength * 0.75), var9.z / 2.0 - var10.z);
		}
	}

	@Inject(method = "isDamageSourceBlocked", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;"), cancellable = true)
	public void isDamageSourceBlocked(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		Vec3 currentVector = this.getViewVector(1.0F);
		if (currentVector.y > -0.99 && currentVector.y < 0.99) {
			currentVector = (new Vec3(currentVector.x, 0.0, currentVector.z)).normalize();
			Vec3 sourceVector = Objects.requireNonNull(source.getSourcePosition()).vectorTo(this.position());
			sourceVector = (new Vec3(sourceVector.x, 0.0, sourceVector.z)).normalize();
			cir.setReturnValue(sourceVector.dot(currentVector) * 3.1415927410125732 < -0.8726646304130554);
			return;
		}
		cir.setReturnValue(false);
	}

	@Override
	public ItemStack getBlockingItem() {
		LivingEntity thisLivingEntity = ((LivingEntity) (Object)this);
		if (thisLivingEntity.isUsingItem() && !thisLivingEntity.getUseItem().isEmpty()) {
			if (thisLivingEntity.getUseItem().getUseAnimation() == UseAnim.BLOCK) {
				return thisLivingEntity.getUseItem();
			}
		} else if ((thisLivingEntity.isOnGround() && thisLivingEntity.isCrouching() && this.hasEnabledShieldOnCrouch() || thisLivingEntity.isPassenger()) && this.hasEnabledShieldOnCrouch()) {
			for(InteractionHand hand : InteractionHand.values()) {
				ItemStack var1 = thisLivingEntity.getItemInHand(hand);
				if (!var1.isEmpty() && var1.getUseAnimation() == UseAnim.BLOCK && !this.isItemOnCooldown(var1) && !(var1.getItem() instanceof IShieldItem shieldItem && shieldItem.getBlockingType().equals(BlockingType.SWORD))) {
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
		return true;
	}
	@Override
	public boolean getIsParry() {
		return isParry;
	}
	@Override
	public void setIsParry(boolean isParry) {
		this.isParry = isParry;
	}
	@Override
	public int getIsParryTicker() {
		return isParryTicker;
	}
	@Override
	public void setIsParryTicker(int isParryTicker) {
		this.isParryTicker = isParryTicker;
	}
	@Override
	public float getNewDamageAfterArmorAbsorb(DamageSource source, float amount, double piercingLevel) {
		if (!source.isBypassArmor()) {
			hurtArmor(source, amount);
			double armourStrength = getArmorValue();
			double toughness = this.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
			amount = CombatRules.getDamageAfterAbsorb(amount, (float)(armourStrength - (armourStrength * piercingLevel)), (float)(toughness - (toughness * piercingLevel)));
		}

		return amount;
	}

	@Override
	public float getNewDamageAfterMagicAbsorb(DamageSource source, float amount, double piercingLevel) {
		if (source.isBypassMagic()) {
			return amount;
		} else {
			if (hasEffect(MobEffects.DAMAGE_RESISTANCE) && source != DamageSource.OUT_OF_WORLD) {
				int i = (getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 5;
				int j = 25 - i;
				float f = amount * (float)(j - (j * piercingLevel));
				float g = amount;
				amount = Math.max(f / 25.0F, 0.0F);
				float h = g - amount;
				if (h > 0.0F && h < 3.4028235E37F) {
					if (((LivingEntity)(Object)this) instanceof ServerPlayer serverPlayer) {
						serverPlayer.awardStat(Stats.DAMAGE_RESISTED, Math.round(h * 10.0F));
					} else if (source.getEntity() instanceof ServerPlayer) {
						((ServerPlayer)source.getEntity()).awardStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(h * 10.0F));
					}
				}
			}

			if (amount <= 0.0F) {
				return 0.0F;
			} else if (source.isBypassEnchantments()) {
				return amount;
			} else {
				int i = EnchantmentHelper.getDamageProtection(this.getArmorSlots(), source);
				if (i > 0) {
					amount = CombatRules.getDamageAfterMagicAbsorb(amount, (float)(i - (i * piercingLevel)));
				}

				return amount;
			}
		}
	}
}
