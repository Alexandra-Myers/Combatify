package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.EatingInterruptionMode;
import net.atlas.combatify.extensions.*;
import net.atlas.combatify.networking.NetworkingHandler;
import net.atlas.combatify.util.MethodHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

import static net.atlas.combatify.util.MethodHandler.getBlocking;

@Mixin(value = LivingEntity.class, priority = 1400)
public abstract class LivingEntityMixin extends Entity implements LivingEntityExtensions {
	@Unique
	private double piercingNegation;
	@Unique
	private ItemCooldowns fallbackCooldowns = MethodHandler.createItemCooldowns();
	@Unique
	protected int attackStrengthMaxValue;

	public LivingEntityMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Unique
	LivingEntity thisEntity = LivingEntity.class.cast(this);

	@Shadow
	protected int useItemRemaining;

	@Shadow
	public abstract ItemStack getUseItem();

	@Shadow
	public abstract void indicateDamage(double d, double e);

	@Shadow
	public abstract double getAttributeValue(Holder<Attribute> holder);

	@Shadow
	public int attackStrengthTicker;

	@Override
	public ItemCooldowns combatify$getFallbackCooldowns() {
		return fallbackCooldowns;
	}
	@Inject(method = "tick", at = @At(value = "RETURN"))
	public void tickCooldowns(CallbackInfo ci) {
		fallbackCooldowns.tick();
		if (!(LivingEntity.class.cast(this) instanceof Player))
			attackStrengthTicker++;
	}

	@Override
	public void combatify$resetAttackStrengthTicker(boolean hit) {
		resetAttackStrengthTicker(false);
	}

	@Override
	public void combatify$resetAttackStrengthTicker(boolean hit, boolean force) {
		resetAttackStrengthTicker(force);
	}

	@Unique
	public void resetAttackStrengthTicker(boolean force) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) {
			return;
		}
		if ((!Combatify.CONFIG.attackSpeed() && getAttributeValue(Attributes.ATTACK_SPEED) - 1.5 >= 20) || Combatify.CONFIG.instaAttack())
			return;
		int chargeTicks = MethodHandler.getCurrentItemAttackStrengthDelay(LivingEntity.class.cast(this));
		if (force || chargeTicks > (attackStrengthMaxValue - attackStrengthTicker)) {
			if (Combatify.CONFIG.enableDebugLogging())
				Combatify.LOGGER.info("Ticks for charge: " + chargeTicks);
			this.attackStrengthMaxValue = chargeTicks;
			this.attackStrengthTicker = 0;
		}
	}

	@Override
	public boolean combatify$isAttackAvailable(float baseTime) {
		return attackStrengthMaxValue - (attackStrengthTicker + baseTime) <= 0;
	}

	@SuppressWarnings("unused")
	@ModifyReturnValue(method = "isBlocking", at = @At(value="RETURN"))
	public boolean isBlocking(boolean original) {
		return !MethodHandler.getBlockingItem(thisEntity).stack().isEmpty();
	}

	@Inject(method = "blockedByShield", at = @At(value="HEAD"), cancellable = true)
	public void blockedByShield(LivingEntity target, CallbackInfo ci) {
		ci.cancel();
	}
	@Override
	public void combatify$setPiercingNegation(double negation) {
		piercingNegation = negation;
	}
	@Override
	public double combatify$getPiercingNegation() {
		return piercingNegation;
	}
	@ModifyConstant(method = "handleDamageEvent", constant = @Constant(intValue = 20, ordinal = 0))
	private int syncInvulnerability(int x) {
		return 10;
	}

	@WrapOperation(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isDamageSourceBlocked(Lnet/minecraft/world/damagesource/DamageSource;)Z"))
	public boolean shield(LivingEntity instance, DamageSource source, Operation<Boolean> original, @Local(ordinal = 0, argsOnly = true) LocalFloatRef amount, @Local(ordinal = 2) LocalFloatRef protectedDamage, @Local(ordinal = 0) LocalBooleanRef wasBlocked, @Share("blocked") LocalBooleanRef blocked) {
		ItemStack itemStack = MethodHandler.getBlockingItem(thisEntity).stack();
		if (level() instanceof ServerLevel serverLevel && amount.get() > 0.0F && original.call(instance, source)) {
			getBlocking(itemStack).block(serverLevel, instance, source, itemStack, amount, protectedDamage, wasBlocked);
		}
		blocked.set(wasBlocked.get());
		return false;
	}
	@Inject(method = "hurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;invulnerableTime:I", ordinal = 0, shift = At.Shift.AFTER))
	public void injectEatingInterruption(DamageSource source, float f, CallbackInfoReturnable<Boolean> cir) {
		Entity entity = source.getEntity();
		boolean canInterrupt = thisEntity.isUsingItem() && (getUseItem().getUseAnimation() == UseAnim.EAT || getUseItem().getUseAnimation() == UseAnim.DRINK);
		if (entity instanceof LivingEntity && level() instanceof ServerLevel serverLevel && canInterrupt) {
			useItemRemaining = switch (Combatify.CONFIG.eatingInterruptionMode()) {
                case FULL_RESET -> thisEntity.getUseItem().getUseDuration(thisEntity);
				case DELAY -> useItemRemaining + invulnerableTime;
				case null, default -> useItemRemaining;
			};
			if (Combatify.CONFIG.eatingInterruptionMode() != EatingInterruptionMode.OFF) {
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
	@WrapOperation(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
	public void modifyKB(LivingEntity instance, double d, double e, double f, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) final DamageSource source, @Local(argsOnly = true) float amount, @Share("blocked") LocalBooleanRef bl) {
		if (bl.get() && amount > 0)
			indicateDamage(e, f);
		Combatify.CONFIG.knockbackMode().runKnockback(instance, source, d, e, f, original::call);
	}

	@ModifyExpressionValue(method = "startUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isUsingItem()Z"))
	public boolean addCooldownCheck(boolean original, @Local(ordinal = 0) ItemStack itemStack) {
		return original || MethodHandler.getCooldowns(thisEntity).isOnCooldown(itemStack.getItem());
	}

	@ModifyExpressionValue(method = "isDamageSourceBlocked", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;getPierceLevel()B"))
	public byte isDamageSourceBlocked(byte original) {
		return Combatify.CONFIG.arrowDisableMode().pierceArrowsBlocked() ? 0 : original;
	}

	@ModifyReturnValue(method = "isDamageSourceBlocked", at = @At(value = "RETURN", ordinal = 0))
	public boolean isDamageSourceBlocked(boolean original) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return original;
		return Combatify.CONFIG.shieldProtectionArc() == 360D || original;
	}

	@ModifyExpressionValue(method = "isDamageSourceBlocked", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;dot(Lnet/minecraft/world/phys/Vec3;)D"))
	public double modifyDotResultToGetRadians(double original) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return original;
        return Combatify.CONFIG.shieldProtectionArc() == 180D ? original : Math.acos(original) / -1;
	}

	@ModifyExpressionValue(method = "isDamageSourceBlocked", at = @At(value = "CONSTANT", args = "doubleValue=0.0", ordinal = 1))
	public double modifyCompareValue(double original) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return original;
		return Combatify.CONFIG.shieldProtectionArc() == 180D ? original : Math.toRadians(Combatify.CONFIG.shieldProtectionArc()) / -1;
	}
	@Override
	public boolean combatify$hasEnabledShieldOnCrouch() {
		return true;
	}

	@Override
	public void combatify$setUseItemRemaining(int ticks) {
		this.useItemRemaining = ticks;
	}
}
