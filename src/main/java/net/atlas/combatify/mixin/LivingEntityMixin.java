package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.attributes.CustomAttributes;
import net.atlas.combatify.extensions.*;
import net.atlas.combatify.networking.NetworkingHandler;
import net.atlas.combatify.util.MethodHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.UUID;

@Mixin(value = LivingEntity.class, priority = 1400)
public abstract class LivingEntityMixin extends Entity implements LivingEntityExtensions {
	@Unique
	private double piercingNegation;

	public LivingEntityMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Unique
	LivingEntity thisEntity = LivingEntity.class.cast(this);


	@Shadow
	public abstract boolean isDamageSourceBlocked(DamageSource damageSource);

	@Shadow
	protected int useItemRemaining;

	@Shadow
	public abstract ItemStack getUseItem();

	@SuppressWarnings("unused")
	@ModifyReturnValue(method = "isBlocking", at = @At(value="RETURN"))
	public boolean isBlocking(boolean original) {
		return !MethodHandler.getBlockingItem(thisEntity).stack().isEmpty();
	}

	@ModifyReturnValue(method = "createLivingAttributes", at = @At(value = "RETURN"))
	private static AttributeSupplier.Builder createAttributes(AttributeSupplier.Builder original) {
		return original.add(CustomAttributes.SHIELD_DISABLE_REDUCTION).add(CustomAttributes.SHIELD_DISABLE_TIME);
	}

	@Inject(method = "blockedByShield", at = @At(value="HEAD"), cancellable = true)
	public void blockedByShield(LivingEntity target, CallbackInfo ci) {
		double x = target.getX() - this.getX();
		double z = target.getZ() - this.getZ();
		double x2 = this.getX() - target.getX();
		double z2 = this.getZ() - target.getZ();
		ItemStack blockingItem = MethodHandler.getBlockingItem(target).stack();
		MethodHandler.disableShield(thisEntity, target, blockingItem);
		if(((ItemExtensions)blockingItem.getItem()).getBlockingType().isToolBlocker()) {
			ci.cancel();
			return;
		}
		MethodHandler.knockback(target, 0.5, x2, z2);
		MethodHandler.knockback(thisEntity, 0.5, x, z);
		ci.cancel();
	}
	@Override
	public void setPiercingNegation(double negation) {
		piercingNegation = negation;
	}
	@Override
	public double getPiercingNegation() {
		return piercingNegation;
	}
	@ModifyConstant(method = "handleDamageEvent", constant = @Constant(intValue = 20, ordinal = 0))
	private int syncInvulnerability(int x) {
		return 10;
	}

	@Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isDamageSourceBlocked(Lnet/minecraft/world/damagesource/DamageSource;)Z"))
	public boolean shield(LivingEntity instance, DamageSource source, @Local(ordinal = 0, argsOnly = true) LocalFloatRef amount, @Local(ordinal = 1) LocalFloatRef f, @Local(ordinal = 2) LocalFloatRef g, @Local(ordinal = 0) LocalBooleanRef bl) {
		ItemStack itemStack = MethodHandler.getBlockingItem(thisEntity).stack();
		if (amount.get() > 0.0F && isDamageSourceBlocked(source)) {
			if (itemStack.getItem() instanceof ItemExtensions shieldItem) {
				if (shieldItem.getBlockingType().hasDelay() && Combatify.CONFIG.shieldDelay() > 0 && itemStack.getUseDuration(thisEntity) - useItemRemaining < Combatify.CONFIG.shieldDelay()) {
					if (Combatify.CONFIG.disableDuringShieldDelay())
						if (source.getDirectEntity() instanceof LivingEntity attacker)
							MethodHandler.disableShield(attacker, instance, itemStack);
					return false;
				}
				shieldItem.getBlockingType().block(instance, null, itemStack, source, amount, f, g, bl);
			}
		}
		return false;
	}
	@ModifyExpressionValue(method = "hurt", at = @At(value = "CONSTANT", args = "intValue=20", ordinal = 0))
	public int changeIFrames(int original, @Local(ordinal = 0, argsOnly = true) final DamageSource source, @Local(ordinal = 0, argsOnly = true) final float amount) {
		Entity entity2 = source.getEntity();
		int invulnerableTime = original - 10;
		if (!Combatify.CONFIG.instaAttack() && Combatify.CONFIG.iFramesBasedOnWeapon() && entity2 instanceof Player player && !(player.getAttributeValue(Attributes.ATTACK_SPEED) - 1.5 >= 20 && !Combatify.CONFIG.attackSpeed())) {
			int base = (int) Math.min(player.getCurrentItemAttackStrengthDelay(), invulnerableTime);
			invulnerableTime = base >= 4 && !Combatify.CONFIG.canAttackEarly() ? base - 2 : base;
		}

		if (source.is(DamageTypeTags.IS_PROJECTILE) && !Combatify.CONFIG.projectilesHaveIFrames())
			invulnerableTime = 0;
		if (source.is(DamageTypes.MAGIC) && !Combatify.CONFIG.magicHasIFrames())
			invulnerableTime = 0;
		return invulnerableTime;
	}
	@Inject(method = "hurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;invulnerableTime:I", ordinal = 0))
	public void injectEatingInterruption(DamageSource source, float f, CallbackInfoReturnable<Boolean> cir) {
		Entity entity = source.getEntity();
		if (entity instanceof LivingEntity && Combatify.CONFIG.eatingInterruption()) {
			if (thisEntity.isUsingItem() && (getUseItem().getUseAnimation() == UseAnim.EAT || getUseItem().getUseAnimation() == UseAnim.DRINK)) {
				useItemRemaining = thisEntity.getUseItem().getUseDuration(thisEntity);
				if (level() instanceof ServerLevel serverLevel)
					for (UUID playerUUID : Combatify.moddedPlayers)
						if (serverLevel.getPlayerByUUID(playerUUID) instanceof ServerPlayer serverPlayer)
							ServerPlayNetworking.send(serverPlayer, new NetworkingHandler.RemainingUseSyncPacket(getId(), useItemRemaining));
			}
		}
	}
	@ModifyExpressionValue(method = "hurt", at = @At(value = "CONSTANT", args = "floatValue=10.0F", ordinal = 0))
	public float changeIFrames(float constant) {
		return constant - 10;
	}
	@Redirect(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
	public void modifyKB(LivingEntity instance, double d, double e, double f, @Local(ordinal = 0, argsOnly = true) final DamageSource source) {
		if ((Combatify.CONFIG.fishingHookKB() && source.getDirectEntity() instanceof FishingHook) || (!source.is(DamageTypeTags.IS_PROJECTILE) && Combatify.CONFIG.midairKB()))
			MethodHandler.projectileKnockback(thisEntity, 0.4, e, f);
		else
			MethodHandler.knockback(thisEntity, 0.4, e, f);
	}

	@ModifyExpressionValue(method = "isDamageSourceBlocked", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;getPierceLevel()B"))
	public byte isDamageSourceBlocked(byte original) {
		return Combatify.CONFIG.arrowDisableMode().pierceArrowsBlocked() ? 0 : original;
	}

	@Inject(method = "isDamageSourceBlocked", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;calculateViewVector(FF)Lnet/minecraft/world/phys/Vec3;"), cancellable = true)
	public void isDamageSourceBlocked(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		Vec3 currentVector = this.calculateViewVector(0.0F, this.getYHeadRot()).normalize();
		Vec3 sourceVector = Objects.requireNonNull(source.getSourcePosition()).vectorTo(this.position());
		sourceVector = (new Vec3(sourceVector.x, 0.0, sourceVector.z)).normalize();
		cir.setReturnValue(sourceVector.dot(currentVector) * 3.1415927410125732 < -0.8726646304130554);
	}
	@Override
	public boolean hasEnabledShieldOnCrouch() {
		return true;
	}

	@Override
	public void setUseItemRemaining(int ticks) {
		this.useItemRemaining = ticks;
	}
}
