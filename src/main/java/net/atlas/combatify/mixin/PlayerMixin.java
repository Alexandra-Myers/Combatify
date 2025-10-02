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
import net.atlas.combatify.component.CustomDataComponents;
import net.atlas.combatify.component.custom.CanSweep;
import net.atlas.combatify.config.wrapper.*;
import net.atlas.combatify.extensions.PlayerExtensions;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.function.Consumer;

import static net.atlas.combatify.util.MethodHandler.sweepAttack;

@SuppressWarnings("unused")
@Mixin(value = Player.class, priority = 1400)
public abstract class PlayerMixin extends AvatarMixin implements PlayerExtensions {

	public PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Shadow
	protected abstract void doAutoAttackOnTouch(@NotNull LivingEntity target);

	@Shadow
	public abstract float getAttackStrengthScale(float f);

	@Shadow
	public abstract float getCurrentItemAttackStrengthDelay();

	@Shadow
	public abstract void attack(Entity entity);

	@Shadow
	public abstract double entityInteractionRange();

	@Shadow
	protected abstract float getEnchantedDamage(Entity entity, float f, DamageSource damageSource);

	@Shadow
	@NotNull
	public abstract ItemStack getWeaponItem();

	@Shadow
	public abstract boolean hasContainerOpen();

	@Shadow
	public abstract void tick();

	@Shadow
	public abstract void resetAttackStrengthTicker();

	@Shadow
	protected int enchantmentSeed;
	@Unique
	protected int attackStrengthMaxValue;

	@Unique
	public boolean missedAttackRecovery;
	@Unique
	@Final
	public float baseValue = 1.0F;
	@Unique
	boolean attacked;

	@Unique
	public final Player player = ((Player) (Object)this);
	@Inject(method = "hurtServer", at = @At("HEAD"))
	public void injectSnowballKb(ServerLevel serverLevel, DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir, @Share("originalDamage") LocalFloatRef originalDamage) {
		originalDamage.set(amount);
	}
	@Inject(method = "hurtServer", at = @At(value = "RETURN", ordinal = 3), cancellable = true)
	public void changeReturn(ServerLevel serverLevel, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, @Share("originalDamage") LocalFloatRef originalDamage) {
		boolean bl = amount == 0.0F && originalDamage.get() <= 0.0F;
 		if(bl && Combatify.CONFIG.snowballKB())
			cir.setReturnValue(super.hurtServer(serverLevel, source, amount));
	}
	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	public void readAdditionalSaveData(ValueInput valueInput, CallbackInfo ci) {
		Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(Combatify.CONFIG.fistDamage());
		Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_SPEED)).setBaseValue(Combatify.CONFIG.baseHandAttackSpeed() + 1.5);
		Objects.requireNonNull(player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE)).setBaseValue(Combatify.CONFIG.attackReach() ? 2.5 : 3);
	}

	@ModifyExpressionValue(method = "createAttributes", at = @At(value = "CONSTANT", args = "doubleValue=1.0"))
	private static double changeAttack(double constant) {
		return Combatify.CONFIG.fistDamage();
	}
	@ModifyReturnValue(method = "createAttributes", at = @At(value = "RETURN"))
	private static AttributeSupplier.Builder createAttributes(AttributeSupplier.Builder original) {
		return original.add(Attributes.ENTITY_INTERACTION_RANGE, Combatify.CONFIG.attackReach() ? 2.5 : 3).add(Attributes.ATTACK_SPEED, Combatify.CONFIG.baseHandAttackSpeed() + 1.5);
	}
	@Inject(method = "drop", at = @At(value = "HEAD"))
	public void addServerOnlyCheck(ItemStack itemStack, boolean bl, CallbackInfoReturnable<ItemEntity> cir) {
		if(Combatify.unmoddedPlayers.contains(player.getUUID()))
			Combatify.isPlayerAttacking.put(player.getUUID(), false);
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;resetAttackStrengthTicker()V"))
	public void redirectDurability(Player instance, Operation<Void> original) {
		if (Combatify.CONFIG.resetOnItemChange() || Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) resetAttackStrengthTicker(false, true, original::call);
	}

	@Inject(method = "blockUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getItemBlockingWith()Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
	public void blockUsingShield(ServerLevel serverLevel, LivingEntity livingEntity, CallbackInfo ci) {
		ci.cancel();
	}

	@Inject(method = "attack", at = @At(value = "HEAD"), cancellable = true)
	public void attack(Entity target, CallbackInfo ci) {
		if (!combatify$isAttackAvailable(baseValue)) ci.cancel();
	}
	@Inject(method = "attack", at = @At(value = "TAIL"))
	public void resetTicker(Entity target, CallbackInfo ci) {
		if (attacked) {
			boolean isMiscTarget = target.getType().equals(EntityType.END_CRYSTAL)
				|| target.getType().equals(EntityType.ITEM_FRAME)
				|| target.getType().equals(EntityType.GLOW_ITEM_FRAME)
				|| target.getType().equals(EntityType.PAINTING)
				|| target instanceof ArmorStand
				|| target instanceof Boat
				|| target instanceof AbstractMinecart
				|| target instanceof Interaction;
			this.combatify$resetAttackStrengthTicker(!Combatify.CONFIG.improvedMiscEntityAttacks() || !isMiscTarget);
		}
	}
	@WrapOperation(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;resetAttackStrengthTicker()V"))
	public void stopReset(Player instance, Operation<Void> original) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) original.call(instance);
	}
	@Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAttackStrengthScale(F)F", ordinal = 0))
	public void doThings(Entity target, CallbackInfo ci, @Local(ordinal = 0) LocalFloatRef attackDamage, @Local(ordinal = 1) float attackDamageBonus) {
		attacked = true;
		if (Combatify.CONFIG.strengthAppliesToEnchants() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA))
			attackDamage.set((float) (this.isAutoSpinAttack() ? MethodHandler.calculateValueFromBase(player.getAttribute(Attributes.ATTACK_DAMAGE), this.autoSpinAttackDmg + attackDamageBonus) : MethodHandler.calculateValue(player.getAttribute(Attributes.ATTACK_DAMAGE), attackDamageBonus)));
	}
	@ModifyExpressionValue(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAttackStrengthScale(F)F", ordinal = 0))
	public float redirectStrengthCheck(float original) {
		original = (float) Mth.clamp(original, Combatify.CONFIG.attackDecayMinCharge(), Combatify.CONFIG.attackDecayMaxCharge());
		return (!Combatify.CONFIG.attackDecay() || (missedAttackRecovery && this.attackStrengthTicker > 4.0F)) && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA) ? 1.0F : original;
	}
	@Inject(method = "resetAttackStrengthTicker", at = @At(value = "HEAD"), cancellable = true)
	public void reset(CallbackInfo ci) {
		int chargeTicks = (int) this.getCurrentItemAttackStrengthDelay() * ((Combatify.CONFIG.chargedAttacks() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) ? 2 : 1);
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA) || chargeTicks > (attackStrengthMaxValue - attackStrengthTicker)) {
			if (Combatify.CONFIG.enableDebugLogging())
				Combatify.LOGGER.info("Ticks for charge: " + chargeTicks);
			this.attackStrengthMaxValue = chargeTicks;
		} else ci.cancel();
	}
	@Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getDeltaMovement()Lnet/minecraft/world/phys/Vec3;"))
	public void injectCrit(Entity target, CallbackInfo ci, @Local(ordinal = 0) float attackDamage, @Local(ordinal = 1) float enchantDamage, @Local(ordinal = 2) float strengthScale, @Local(ordinal = 3) LocalFloatRef combinedDamage, @Local(ordinal = 2) LocalBooleanRef bl3) {
		if (Combatify.CONFIG.attackDecay() || Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) {
			enchantDamage /= strengthScale;
			float originalAttackDamage;
			if (Combatify.CONFIG.strengthAppliesToEnchants()) originalAttackDamage = (float) (this.isAutoSpinAttack() ? MethodHandler.calculateValueFromBase(player.getAttribute(Attributes.ATTACK_DAMAGE), this.autoSpinAttackDmg + enchantDamage) : MethodHandler.calculateValue(player.getAttribute(Attributes.ATTACK_DAMAGE), enchantDamage));
			else originalAttackDamage = this.isAutoSpinAttack() ? this.autoSpinAttackDmg : (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
			originalAttackDamage *= bl3.get() ? 1.5F : 1;
			attackDamage -= (float) ((0.2 + strengthScale * strengthScale * 0.8) * originalAttackDamage);
			float adjScale = (float) ((strengthScale - Combatify.CONFIG.attackDecayMinCharge()) / Combatify.CONFIG.attackDecayMaxChargeDiff());
			originalAttackDamage *= (float) (Combatify.CONFIG.attackDecayMinPercentageBase() + adjScale * adjScale * Combatify.CONFIG.attackDecayMaxPercentageBaseDiff());
			attackDamage += originalAttackDamage;
			enchantDamage *= (float) (Combatify.CONFIG.attackDecayMinPercentageEnchants() + ((strengthScale - Combatify.CONFIG.attackDecayMinCharge()) / Combatify.CONFIG.attackDecayMaxChargeDiff()) * Combatify.CONFIG.attackDecayMaxPercentageEnchantsDiff());
			combinedDamage.set(attackDamage + enchantDamage);
		}
		boolean strengthAppliesToEnchants = Combatify.CONFIG.strengthAppliesToEnchants() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA);
		if (strengthAppliesToEnchants)
			combinedDamage.set(attackDamage);
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA) || !Combatify.CONFIG.getCritImpl().execFunc("overrideCrit()"))
			return;
		if (bl3.get()) {
			if (strengthAppliesToEnchants) combinedDamage.set(combinedDamage.get() / 1.5F);
			else attackDamage /= 1.5F;
		}
		GenericAPIWrapper<?> wrapper;
		if (target instanceof Player p) wrapper = new PlayerWrapper<>(p);
		else if (target instanceof LivingEntity l) wrapper = new LivingEntityWrapper<>(l);
		else wrapper = new EntityWrapper<>(target);
		final MutableFloat finalAttackDamage = new MutableFloat(attackDamage);
		bl3.set(Combatify.CONFIG.getCritImpl().execFunc("runCrit(player, target, combinedDamage)", new PlayerWrapper<>(player), wrapper, (strengthAppliesToEnchants ? (combinedDamage) : new LocalFloatRef() {
			@Override
			public float get() {
				return finalAttackDamage.getValue();
			}

			@Override
			public void set(float v) {
				finalAttackDamage.setValue(v);
			}
		})));
		if (!strengthAppliesToEnchants) combinedDamage.set(finalAttackDamage.getValue() + enchantDamage);
	}
	@WrapOperation(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
	public void knockback(LivingEntity instance, double d, double e, double f, Operation<Void> original, @Local(ordinal = 0) DamageSource damageSource) {
		Combatify.CONFIG.knockbackMode().runKnockback(instance, damageSource, d, e, f, original::call);
	}
	@Inject(method = "attack", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
	public void createSweep(Entity target, CallbackInfo ci, @Local(ordinal = 1) final boolean bl2, @Local(ordinal = 2) final boolean bl3, @Local(ordinal = 3) LocalBooleanRef bl4, @Local(ordinal = 0) final float attackDamage, @Share("didSweep") LocalBooleanRef didSweep) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return;
		bl4.set(false);
		double d = this.getKnownMovement().horizontalDistanceSqr();
		double e = (double)this.getSpeed() * 2.5;
		boolean isSweepPossible = Combatify.CONFIG.sweepConditionsMatchMiss() || this.onGround();
		if (!bl3 && !bl2 && isSweepPossible && d < Mth.square(e) && checkSweepAttack()) {
			AABB box = target.getBoundingBox().inflate(1.0, 0.25, 1.0);
			sweepAttack(player, box, (float) MethodHandler.getCurrentAttackReach(player, 1.0F), attackDamage, (livingEntity, damage, damageSource) -> {
				float attackDamageBonus = getEnchantedDamage(livingEntity, damage, damageSource) - damage;
				if (Combatify.CONFIG.strengthAppliesToEnchants() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA))
					attackDamageBonus = (float) MethodHandler.calculateValueFromBase(player.getAttribute(Attributes.ATTACK_DAMAGE), attackDamageBonus);
				if (Combatify.CONFIG.attackDecay() || Combatify.getState().equals(Combatify.CombatifyState.VANILLA))
					attackDamageBonus *= (float) (Combatify.CONFIG.attackDecayMinPercentageEnchants() + ((getAttackStrengthScale(0.5F) - Combatify.CONFIG.attackDecayMinCharge()) / Combatify.CONFIG.attackDecayMaxChargeDiff()) * Combatify.CONFIG.attackDecayMaxPercentageEnchantsDiff());
				return damage + attackDamageBonus;
			}, target);
			didSweep.set(true);
		}
	}
	@Inject(method = "attack", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;hurtMarked:Z", shift = At.Shift.BEFORE, ordinal = 0))
	public void resweep(Entity target, CallbackInfo ci, @Local(ordinal = 3) LocalBooleanRef bl4, @Share("didSweep") LocalBooleanRef didSweep) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return;
		bl4.set(didSweep.get());
	}
	@Override
	public void combatify$attackAir() {
		if (this.combatify$isAttackAvailable(baseValue)) {
			combatify$customSwing(InteractionHand.MAIN_HAND);
			float attackDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
			if (attackDamage > 0.0F && this.checkSweepAttack() && Combatify.CONFIG.canSweepOnMiss()) {
				float currentAttackReach = (float) MethodHandler.getCurrentAttackReach(player, 1.0F);
				double dirX = -Mth.sin(getYRot() * (float) (Math.PI / 180.0)) * 2.0;
				double dirZ = Mth.cos(getYRot() * (float) (Math.PI / 180.0)) * 2.0;
				AABB sweepBox = player.getBoundingBox().inflate(1.0, 0.25, 1.0).move(dirX, 0.0, dirZ);
				if (Combatify.CONFIG.enableDebugLogging())
					Combatify.LOGGER.info("Swept");
				sweepAttack(player, sweepBox, currentAttackReach, attackDamage, (livingEntity, damage, damageSource) -> {
					float attackDamageBonus = getEnchantedDamage(livingEntity, damage, damageSource) - damage;
					if (Combatify.CONFIG.strengthAppliesToEnchants() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA))
						attackDamageBonus = (float) MethodHandler.calculateValueFromBase(player.getAttribute(Attributes.ATTACK_DAMAGE), attackDamageBonus);
					if (Combatify.CONFIG.attackDecay() || Combatify.getState().equals(Combatify.CombatifyState.VANILLA))
						attackDamageBonus *= (float) (Combatify.CONFIG.attackDecayMinPercentageEnchants() + ((getAttackStrengthScale(0.5F) - Combatify.CONFIG.attackDecayMinCharge()) / Combatify.CONFIG.attackDecayMaxChargeDiff()) * Combatify.CONFIG.attackDecayMaxPercentageEnchantsDiff());
					return damage + attackDamageBonus;
				}, null);
			}
			this.combatify$resetAttackStrengthTicker(false);
		}
	}
	@Override
	public void combatify$customSwing(InteractionHand interactionHand) {
		swing(interactionHand, false);
	}
	@Override
	public void combatify$resetAttackStrengthTicker(boolean hit) {
		resetAttackStrengthTicker(hit, false, Player::resetAttackStrengthTicker);
	}
	@Unique
	public void resetAttackStrengthTicker(boolean hit, boolean force, Consumer<Player> vanillaReset) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) {
			vanillaReset.accept(player);
			return;
		}
		this.missedAttackRecovery = !hit && Combatify.CONFIG.missedAttackRecovery();
		if ((!Combatify.CONFIG.attackSpeed() && getAttributeValue(Attributes.ATTACK_SPEED) - 1.5 >= 20) || Combatify.CONFIG.instaAttack())
			return;
		int chargeTicks = (int) (this.getCurrentItemAttackStrengthDelay()) * ((Combatify.CONFIG.chargedAttacks() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) ? 2 : 1);
		if (force || chargeTicks > (attackStrengthMaxValue - attackStrengthTicker)) {
			if (Combatify.CONFIG.enableDebugLogging())
				Combatify.LOGGER.info("Ticks for charge: " + chargeTicks);
			this.attackStrengthMaxValue = chargeTicks;
			this.attackStrengthTicker = 0;
		}
	}

	@ModifyExpressionValue(method = "getCurrentItemAttackStrengthDelay", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/core/Holder;)D"))
	public double modifyAttackSpeed(double original, @Share("hasVanilla") LocalBooleanRef hasVanilla) {
		hasVanilla.set((getAttribute(Attributes.ATTACK_SPEED).getModifier(Item.BASE_ATTACK_SPEED_ID) != null || Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) && !Combatify.getState().equals(Combatify.CombatifyState.CTS_8C));
		double mod = !Combatify.CONFIG.hasteFix() ? 1.5 : MethodHandler.calculateValueFromBase(getAttribute(Attributes.ATTACK_SPEED), 1.5);
		double speed = original - mod;
		if (hasVanilla.get() || speed <= 0)
			speed += mod;
		return Mth.clamp(speed, 0.1, 1024.0);
	}

	@ModifyReturnValue(method = "getCurrentItemAttackStrengthDelay", at = @At(value = "RETURN"))
	public float modifyAttackTicks(float original, @Share("hasVanilla") LocalBooleanRef hasVanilla) {
		return hasVanilla.get() ? original : Math.round(original);
	}

	@ModifyExpressionValue(method = "getAttackStrengthScale", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getCurrentItemAttackStrengthDelay()F"))
	public float modifyMaxCharge(float original) {
		return (Combatify.CONFIG.resetOnItemChange() || Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) ? (int) (original) * ((Combatify.CONFIG.chargedAttacks() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) ? 2 : 1) : attackStrengthMaxValue;
	}

	@ModifyReturnValue(method = "getAttackStrengthScale", at = @At(value = "RETURN"))
	public float modifyAttackStrengthScale(float original) {
		float charge = (Combatify.CONFIG.chargedAttacks() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) ? 2.0F : 1.0F;
		if (this.attackStrengthMaxValue == 0) {
			return charge;
		}
		return charge * original;
	}

	@Override
	public boolean combatify$isAttackAvailable(float baseTime) {
		if (getAttackStrengthScale(baseTime) < 1.0F && !(Combatify.CONFIG.canAttackEarly() || Combatify.getState().equals(Combatify.CombatifyState.VANILLA))) {
			return (this.missedAttackRecovery && this.attackStrengthTicker + baseTime > 4.0F);
		}
		return true;
	}

	@Unique
	protected boolean checkSweepAttack() {
		float charge = Combatify.CONFIG.chargedAttacks() ? 1.95F : 0.9F;
		boolean sweepingItem = getMainHandItem().getOrDefault(CustomDataComponents.CAN_SWEEP, CanSweep.DISABLED).enabled();
		boolean sweep = getAttackStrengthScale(baseValue) > charge && (getAttributeValue(Attributes.SWEEPING_DAMAGE_RATIO) > 0.0F || sweepingItem);
		if (!Combatify.CONFIG.sweepWithSweeping())
			return sweepingItem && sweep;
		return sweep;
	}

	@Override
	public boolean combatify$getMissedAttackRecovery() {
		return missedAttackRecovery;
	}

	@ModifyReturnValue(method = "entityInteractionRange", at = @At(value = "RETURN"))
	public double getCurrentAttackReach(double original) {
		return MethodHandler.getCurrentAttackReach(player, 0.0F);
	}
}
