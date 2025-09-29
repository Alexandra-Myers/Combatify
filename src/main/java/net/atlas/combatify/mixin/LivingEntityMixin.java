package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.EatingInterruptionMode;
import net.atlas.combatify.extensions.*;
import net.atlas.combatify.networking.NetworkingHandler;
import net.atlas.combatify.util.MethodHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

import static net.atlas.combatify.util.MethodHandler.arrowDisable;
import static net.atlas.combatify.util.MethodHandler.getBlockingItem;

@Mixin(value = LivingEntity.class, priority = 1400)
public abstract class LivingEntityMixin extends Entity implements LivingEntityExtensions {
	@Unique
	private int crouchBlockingTicks = 0;
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
	public abstract boolean isBlocking();

	@Shadow
	public abstract double getAttributeValue(Holder<Attribute> holder);

	@Shadow
	public int attackStrengthTicker;

	@Override
	public int combatify$getCrouchBlockingTicks() {
		return crouchBlockingTicks;
	}

	@Override
	public ItemCooldowns combatify$getFallbackCooldowns() {
		return fallbackCooldowns;
	}
	@Inject(method = "tick", at = @At(value = "RETURN"))
	public void tickCooldowns(CallbackInfo ci) {
		fallbackCooldowns.tick();
		if (MethodHandler.canCrouchShield(thisEntity) != null) crouchBlockingTicks++;
		else crouchBlockingTicks = 0;
		if (!(LivingEntity.class.cast(this) instanceof Player))
			attackStrengthTicker++;
	}

	@Override
	public void combatify$resetAttackStrengthTicker(boolean hit) {
		resetAttackStrengthTicker(false);
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

	@Inject(method = "blockedByItem", at = @At(value="HEAD"), cancellable = true)
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
	@WrapOperation(method = "applyItemBlocking", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;blockUsingItem(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)V"))
	public void applyBlockEffects(LivingEntity instance, ServerLevel serverLevel, LivingEntity attacker, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) DamageSource source) {
		original.call(instance, serverLevel, attacker);
		MethodHandler.blockedByShield(serverLevel, instance, attacker, source);
	}
	@WrapOperation(method = "applyItemBlocking", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/BlocksAttacks;resolveBlockedDamage(Lnet/minecraft/world/damagesource/DamageSource;FD)F"))
	public float applyBanner(BlocksAttacks instance, DamageSource damageSource, float amount, double angle, Operation<Float> original, @Local(ordinal = 0) ItemStack blockingItem) {
		float result = original.call(instance, damageSource, amount, angle);
		BannerPatternLayers bannerPatternLayers = blockingItem.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
		DyeColor dyeColor = blockingItem.get(DataComponents.BASE_COLOR);
		if (MethodHandler.getBlocking(blockingItem).hasBanner() && (!bannerPatternLayers.layers().isEmpty() || dyeColor != null)) {
			BlocksAttacks.DamageReduction bannerBoost = new BlocksAttacks.DamageReduction(instance.damageReductions().getFirst().horizontalBlockingAngle(), Optional.empty(), 5, 1);
			result = Mth.clamp(result + bannerBoost.resolve(damageSource, amount, angle), 0, amount);
		}
		return result;
	}
	@WrapOperation(method = "applyItemBlocking", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z"))
	public boolean applyArrowDisable(DamageSource instance, TagKey<DamageType> tagKey, Operation<Boolean> original, @Local(ordinal = 0, argsOnly = true) ServerLevel serverLevel) {
		if (original.call(instance, tagKey)) {
			switch (instance.getDirectEntity()) {
				case Arrow arrow when Combatify.CONFIG.arrowDisableMode().satisfiesConditions(arrow) ->
					arrowDisable(serverLevel, thisEntity, instance, arrow, getBlockingItem(thisEntity).stack());
				case SpectralArrow arrow when Combatify.CONFIG.arrowDisableMode().satisfiesConditions(arrow) ->
					arrowDisable(serverLevel, thisEntity, instance, arrow, getBlockingItem(thisEntity).stack());
				case null, default -> {
					// Do nothing
				}
			}
			return true;
		}
		return false;
	}
	@WrapOperation(method = "applyItemBlocking", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getUsedItemHand()Lnet/minecraft/world/InteractionHand;"))
	public InteractionHand spoofUsedHand(LivingEntity instance, Operation<InteractionHand> original, @Local(ordinal = 0, argsOnly = true) ServerLevel serverLevel) {
		InteractionHand interactionHand = original.call(instance);
		if (MethodHandler.getBlockingItem(instance).useHand() != null) return MethodHandler.getBlockingItem(instance).useHand();
		return interactionHand;
	}
	@Inject(method = "hurtServer", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;invulnerableTime:I", ordinal = 0, shift = At.Shift.AFTER))
	public void injectEatingInterruption(ServerLevel serverLevel, DamageSource source, float f, CallbackInfoReturnable<Boolean> cir) {
		Entity entity = source.getEntity();
		boolean canInterrupt = thisEntity.isUsingItem() && (getUseItem().getUseAnimation() == ItemUseAnimation.EAT || getUseItem().getUseAnimation() == ItemUseAnimation.DRINK);
		if (entity instanceof LivingEntity && canInterrupt) {
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
	@ModifyExpressionValue(method = "hurtServer", at = @At(value = "CONSTANT", args = "floatValue=10.0F", ordinal = 0))
	public float changeIFrames(float constant) {
		return constant - 10;
	}
	@WrapOperation(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
	public void modifyKB(LivingEntity instance, double d, double e, double f, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) final DamageSource source, @Local(argsOnly = true) float amount, @Share("blocked") LocalBooleanRef bl) {
		if (bl.get() && amount > 0)
			indicateDamage(e, f);
		if ((Combatify.CONFIG.fishingHookKB() && source.getDirectEntity() instanceof FishingHook) || (!source.is(DamageTypeTags.IS_PROJECTILE) && Combatify.CONFIG.midairKB()))
			MethodHandler.projectileKnockback(instance, d, e, f);
		else if (Combatify.CONFIG.ctsKB())
			MethodHandler.knockback(instance, d, e, f);
		else
			original.call(instance, d, e, f);
	}
	@ModifyReceiver(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
	public ItemStack modifyBlockingItem(ItemStack instance, DataComponentType dataComponentType) {
		return getBlockingItem(thisEntity).stack();
	}

	@ModifyExpressionValue(method = "startUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isUsingItem()Z"))
	public boolean addCooldownCheck(boolean original, @Local(ordinal = 0) ItemStack itemStack) {
		return original || MethodHandler.getCooldowns(thisEntity).isOnCooldown(itemStack);
	}

	@ModifyExpressionValue(method = "applyItemBlocking", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;getPierceLevel()B"))
	public byte ignorePiercing(byte original) {
		return Combatify.CONFIG.arrowDisableMode().pierceArrowsBlocked() ? 0 : original;
	}

	@ModifyReturnValue(method = "getItemBlockingWith", at = @At("RETURN"))
	public ItemStack removeMojangStupidity(ItemStack original) {
		return original == null && !MethodHandler.getBlockingItem(thisEntity).stack().isEmpty() ? MethodHandler.getBlockingItem(thisEntity).stack() : original;
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
