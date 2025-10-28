package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.item.TieredShieldItem;
import net.atlas.combatify.util.CustomEnchantmentHelper;
import net.atlas.combatify.extensions.*;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings("unused")
@Mixin(value = Player.class, priority = 1400)
public abstract class PlayerMixin extends LivingEntity implements PlayerExtensions, LivingEntityExtensions {
	/**
	 * This is a crime, I know,
	 * But it's okay we have to do this to fix a CTS bug
	 */
	@SuppressWarnings("WrongEntityDataParameterClass")
	@Unique
	private static final EntityDataAccessor<Boolean> DATA_PLAYER_USES_SHIELD_CROUCH = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);
	public PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Shadow
	protected abstract void doAutoAttackOnTouch(@NotNull LivingEntity target);

	@Shadow
	public abstract float getAttackStrengthScale(float f);

	@Shadow
	public abstract float getCurrentItemAttackStrengthDelay();

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
	@Inject(method = "defineSynchedData", at = @At("TAIL"))
	public void appendShieldOnCrouch(CallbackInfo ci) {
		entityData.define(DATA_PLAYER_USES_SHIELD_CROUCH, true);
	}
	@Inject(method = "hurt", at = @At("HEAD"))
	public void injectSnowballKb(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, @Share("originalDamage") LocalFloatRef original) {
		original.set(amount);
	}
	@Inject(method = "hurt", at = @At(value = "RETURN", ordinal = 3), cancellable = true)
	public void changeReturn(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, @Share("originalDamage") LocalFloatRef original) {
		boolean bl = amount == 0.0F && original.get() <= 0.0F;
 		if (bl) cir.setReturnValue(super.hurt(source, amount));
	}
	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	public void readAdditionalSaveData(CompoundTag nbt, CallbackInfo ci) {
		Objects.requireNonNull(player.getAttribute(ForgeMod.ENTITY_REACH.get())).setBaseValue(2.5);
		Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(!Combatify.CONFIG.fistDamage.get() ? 2 : 1);
		Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_SPEED)).setBaseValue(Combatify.CONFIG.baseHandAttackSpeed.get() + 1.5);
	}

	@ModifyExpressionValue(method = "createAttributes", at = @At(value = "CONSTANT", args = "doubleValue=1.0"))
	private static double changeAttack(double constant) {
		return !Combatify.CONFIG.fistDamage.get() ? 1 + constant : constant;
	}
	@ModifyReturnValue(method = "createAttributes", at = @At(value = "RETURN"))
	private static AttributeSupplier.Builder createAttributes(AttributeSupplier.Builder original) {
		return original.add(ForgeMod.ENTITY_REACH.get()).add(Attributes.ATTACK_SPEED, Combatify.CONFIG.baseHandAttackSpeed.get() + 1.5);
	}
	@Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At(value = "HEAD"))
	public void addServerOnlyCheck(ItemStack itemStack, boolean bl, boolean bl2, CallbackInfoReturnable<ItemEntity> cir) {
		if(Combatify.unmoddedPlayers.contains(player.getUUID())) {
			Combatify.isPlayerAttacking.put(player.getUUID(), false);
		}
	}

	@ModifyExpressionValue(method = "hurtCurrentlyUsedShield", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;canPerformAction(Lnet/minecraftforge/common/ToolAction;)Z"))
	public boolean hurtCurrentlyUsedShield(boolean original) {
		return !((ItemExtensions)useItem.getItem()).getBlockingType().isEmpty() || original;
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isSameItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
	public boolean redirectDurability(boolean original) {
		return true;
	}

	@Inject(method = "blockUsingShield", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;canDisableShield(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z"), cancellable = true)
	public void blockUsingShield(@NotNull LivingEntity attacker, CallbackInfo ci) {
		ci.cancel();
	}

	@Override
	public boolean ctsShieldDisable(float damage, Item item) {
		player.getCooldowns().addCooldown(item, (int)(damage * 20.0F));
		if (item instanceof TieredShieldItem)
			for (TieredShieldItem tieredShieldItem : Combatify.shields)
				if (item != tieredShieldItem)
					player.getCooldowns().addCooldown(tieredShieldItem, (int)(damage * 20.0F));
		player.stopUsingItem();
		player.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + this.level().random.nextFloat() * 0.4F);
		return true;
	}

	@Override
	public boolean combatify$hasEnabledShieldOnCrouch() {
		return entityData.get(DATA_PLAYER_USES_SHIELD_CROUCH);
	}

	@Override
	public void combatify$setShieldOnCrouch(boolean hasShieldOnCrouch) {
		entityData.set(DATA_PLAYER_USES_SHIELD_CROUCH, hasShieldOnCrouch);
	}

	@Inject(method = "attack", at = @At(value = "HEAD"), cancellable = true)
	public void attack(Entity target, CallbackInfo ci) {
		if(!combatify$isAttackAvailable(baseValue)) ci.cancel();
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
			this.combatify$resetAttackStrengthTicker(!Combatify.CONFIG.improvedMiscEntityAttacks.get() || !isMiscTarget);
		}
	}
	@Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAttackStrengthScale(F)F", ordinal = 0))
	public void doThings(Entity target, CallbackInfo ci, @Local(ordinal = 0) LocalFloatRef attackDamage, @Local(ordinal = 1) LocalFloatRef attackDamageBonus) {
		attacked = true;
		LivingEntity livingEntity = target instanceof LivingEntity ? (LivingEntity) target : null;
		boolean bl = livingEntity != null;
		if(player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof TridentItem && bl)
			attackDamageBonus.set(CustomEnchantmentHelper.getDamageBonus(player.getMainHandItem(), livingEntity));
		attackDamage.set((float) MethodHandler.calculateValue(player.getAttribute(Attributes.ATTACK_DAMAGE), attackDamageBonus.get()));
	}
	@ModifyExpressionValue(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAttackStrengthScale(F)F", ordinal = 0))
	public float redirectStrengthCheck(float original) {
		return 1.0F;
	}
	@Inject(method = "resetAttackStrengthTicker", at = @At(value = "HEAD"), cancellable = true)
	public void reset(CallbackInfo ci) {
		ci.cancel();
	}
	@Inject(method = "attack", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;walkDist:F"))
	public void injectCrit(Entity target, CallbackInfo ci, @Local(ordinal = 0) LocalFloatRef attackDamage, @Local(ordinal = 1) final float attackDamageBonus, @Local(ordinal = 2)LocalBooleanRef bl3) {
		attackDamage.set(attackDamage.get() - attackDamageBonus);
		if(bl3.get())
			attackDamage.set(attackDamage.get() / 1.5F);
		boolean isCrit = player.fallDistance > 0.0F
			&& !player.onGround()
			&& !player.onClimbable()
			&& !player.isInWater()
			&& !player.hasEffect(MobEffects.BLINDNESS)
			&& !player.isPassenger()
			&& target instanceof LivingEntity;
		if(!Combatify.CONFIG.sprintCritsEnabled.get()) {
			isCrit &= !isSprinting();
		}
		bl3.set(isCrit);
		if (isCrit)
			attackDamage.set(attackDamage.get() * 1.5F);

	}
	@WrapOperation(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
	public void knockback(LivingEntity instance, double d, double e, double f, Operation<Void> original) {
		MethodHandler.knockback(instance, d, e, f);
	}
	@Inject(method = "attack", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
	public void createSweep(Entity target, CallbackInfo ci, @Local(ordinal = 1) final boolean bl2, @Local(ordinal = 2) final boolean bl3, @Local(ordinal = 3) LocalBooleanRef bl4, @Local(ordinal = 5) final boolean bl6, @Local(ordinal = 0) final float attackDamage, @Local(ordinal = 0) final double d, @Share("wasSweep") LocalBooleanRef wasSweep) {
		if (!bl3 && !bl2 && this.onGround() && d < (double)this.getSpeed() && checkSweepAttack()) {
			if (bl6) {
				AABB box = target.getBoundingBox().inflate(1.0, 0.25, 1.0);
				MethodHandler.sweepAttack(player, box, (float) MethodHandler.getCurrentAttackReach(player, 1.0F), attackDamage, target);
			}
			wasSweep.set(true);
		}
	}
	@Definition(id = "flag2", local = @Local(type = boolean.class, ordinal = 3))
	@Expression("flag2 != false")
	@ModifyExpressionValue(method = "attack", at = @At("MIXINEXTRAS:EXPRESSION"))
	public boolean resweep(boolean original) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return original;
		return false;
	}
	@Override
	public void combatify$attackAir() {
		if (this.combatify$isAttackAvailable(baseValue)) {
			combatify$customSwing(InteractionHand.MAIN_HAND);
			float attackDamage = (float) Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_DAMAGE)).getValue();
			if (attackDamage > 0.0F && this.checkSweepAttack()) {
				float reach = (float) MethodHandler.getCurrentAttackReach(player,1.0F);
				double var5 = (-Mth.sin(player.yBodyRot * 0.017453292F)) * 2.0;
				double var7 = Mth.cos(player.yBodyRot * 0.017453292F) * 2.0;
				AABB box = player.getBoundingBox().inflate(1.0, 0.25, 1.0).move(var5, 0.0, var7);
				MethodHandler.sweepAttack(player, box, reach, attackDamage, null);
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

	@Override
	public void combatify$resetAttackStrengthTicker(boolean hit, boolean force) {
		resetAttackStrengthTicker(hit, force, Player::resetAttackStrengthTicker);
	}

	@Unique
	public void resetAttackStrengthTicker(boolean hit, boolean force, Consumer<Player> vanillaReset) {
		this.missedAttackRecovery = !hit;
		if ((!Combatify.CONFIG.attackSpeed.get() && getAttributeValue(Attributes.ATTACK_SPEED) - 1.5 >= 20) || Combatify.CONFIG.instaAttack.get())
			return;
		int chargeTicks = (int) (this.getCurrentItemAttackStrengthDelay()) * 2;
		if (force || chargeTicks > (attackStrengthMaxValue - attackStrengthTicker)) {
			Combatify.LOGGER.info("Ticks for charge: " + chargeTicks);
			this.attackStrengthMaxValue = chargeTicks;
			this.attackStrengthTicker = 0;
		}
	}

	@ModifyExpressionValue(method = "getCurrentItemAttackStrengthDelay", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/world/entity/ai/attributes/Attribute;)D"))
	public double modifyAttackSpeed(double original, @Share("hasVanilla") LocalBooleanRef hasVanilla) {
		hasVanilla.set((getAttribute(Attributes.ATTACK_SPEED).getModifier(Item.BASE_ATTACK_SPEED_UUID) != null || Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) && !Combatify.getState().equals(Combatify.CombatifyState.CTS_8C));
		double mod = 1.5;
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
		return attackStrengthMaxValue;
	}

	@ModifyReturnValue(method = "getAttackStrengthScale", at = @At(value = "RETURN"))
	public float modifyAttackStrengthScale(float original) {
		float charge = 2.0F;
		if (this.attackStrengthMaxValue == 0) {
			return charge;
		}
		return charge * original;
	}

	@Override
	public boolean combatify$isAttackAvailable(float baseTime) {
		if (getAttackStrengthScale(baseTime) < 1.0F) {
			return (this.missedAttackRecovery && this.attackStrengthTicker + baseTime > 4.0F);
		}
		return true;
	}

	protected boolean checkSweepAttack() {
		return getAttackStrengthScale(baseValue) > 1.95F && EnchantmentHelper.getSweepingDamageRatio(player) > 0.0F;
	}

	@Override
	public boolean combatify$getMissedAttackRecovery() {
		return missedAttackRecovery;
	}
}
