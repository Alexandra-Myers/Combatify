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
import net.atlas.combatify.item.NewAttributes;
import net.atlas.combatify.item.TieredShieldItem;
import net.atlas.combatify.util.CustomEnchantmentHelper;
import net.atlas.combatify.extensions.*;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
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
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Iterator;
import java.util.List;
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

	@Shadow
	public abstract void attack(Entity entity);

	@Unique
	protected int attackStrengthMaxValue;

	@Unique
	public boolean missedAttackRecovery;
	@Unique
	@Final
	public float baseValue = 1.0F;
	@Unique
	float oldDamage;
	@Unique
	float currentAttackReach;
	@Unique
	boolean attacked;

	@Unique
	public final Player player = ((Player) (Object)this);
	@Inject(method = "defineSynchedData", at = @At("TAIL"))
	public void appendShieldOnCrouch(CallbackInfo ci) {
		entityData.define(DATA_PLAYER_USES_SHIELD_CROUCH, true);
	}
	@Inject(method = "hurt", at = @At("HEAD"))
	public void injectSnowballKb(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		oldDamage = amount;
	}
	@Inject(method = "hurt", at = @At(value = "RETURN", ordinal = 3), cancellable = true)
	public void changeReturn(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		boolean bl = amount == 0.0F && oldDamage <= 0.0F;
 		if(bl)
			cir.setReturnValue(super.hurt(source, amount));
	}
	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	public void readAdditionalSaveData(CompoundTag nbt, CallbackInfo ci) {
		Objects.requireNonNull(player.getAttribute(NewAttributes.ATTACK_REACH)).setBaseValue(2.5);
		Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(!Combatify.CONFIG.fistDamage() ? 2 : 1);
		Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_SPEED)).setBaseValue(Combatify.CONFIG.baseHandAttackSpeed() + 1.5);
	}

	@ModifyExpressionValue(method = "createAttributes", at = @At(value = "CONSTANT", args = "doubleValue=1.0"))
	private static double changeAttack(double constant) {
		return !Combatify.CONFIG.fistDamage() ? 1 + constant : constant;
	}
	@ModifyReturnValue(method = "createAttributes", at = @At(value = "RETURN"))
	private static AttributeSupplier.Builder createAttributes(AttributeSupplier.Builder original) {
		return original.add(NewAttributes.ATTACK_REACH).add(Attributes.ATTACK_SPEED, Combatify.CONFIG.baseHandAttackSpeed() + 1.5);
	}
	@Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At(value = "HEAD"))
	public void addServerOnlyCheck(ItemStack itemStack, boolean bl, boolean bl2, CallbackInfoReturnable<ItemEntity> cir) {
		if(Combatify.unmoddedPlayers.contains(player.getUUID())) {
			Combatify.isPlayerAttacking.put(player.getUUID(), false);
		}
	}

	@ModifyExpressionValue(method = "hurtCurrentlyUsedShield", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
	public boolean hurtCurrentlyUsedShield(boolean original) {
		return !((ItemExtensions)useItem.getItem()).getBlockingType().isEmpty() || original;
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isSameItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
	public boolean redirectDurability(boolean original) {
		return true;
	}

	@Inject(method = "blockUsingShield", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;canDisableShield()Z"), cancellable = true)
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
			this.combatify$resetAttackStrengthTicker(!Combatify.CONFIG.improvedMiscEntityAttacks() || !isMiscTarget);
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
		currentAttackReach = (float) MethodHandler.getCurrentAttackReach(player, 1.0F);
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
		if(!Combatify.CONFIG.sprintCritsEnabled()) {
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
	public void createSweep(Entity target, CallbackInfo ci, @Local(ordinal = 1) final boolean bl2, @Local(ordinal = 2) final boolean bl3, @Local(ordinal = 3) LocalBooleanRef bl4, @Local(ordinal = 5) final boolean bl6, @Local(ordinal = 0) final float attackDamage, @Local(ordinal = 0) final double d) {
		bl4.set(false);
		if (!bl3 && !bl2 && this.onGround() && d < (double)this.getSpeed())
			bl4.set(checkSweepAttack());
		if(bl6) {
			if(bl4.get()) {
				AABB box = target.getBoundingBox().inflate(1.0, 0.25, 1.0);
				this.betterSweepAttack(box, currentAttackReach, attackDamage, target);
				bl4.set(false);
			}
		}
	}
	@Inject(method = "attack", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;hurtMarked:Z", shift = At.Shift.BEFORE, ordinal = 0))
	public void resweep(Entity target, CallbackInfo ci, @Local(ordinal = 3) LocalBooleanRef bl4) {
		bl4.set(checkSweepAttack());
	}
	@Override
	public void combatify$attackAir() {
		if (this.combatify$isAttackAvailable(baseValue)) {
			combatify$customSwing(InteractionHand.MAIN_HAND);
			float attackDamage = (float) Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_DAMAGE)).getValue();
			if (attackDamage > 0.0F && this.checkSweepAttack()) {
				float var2 = (float) MethodHandler.getCurrentAttackReach(player, 1.0F);
				double var5 = (-Mth.sin(player.yBodyRot * 0.017453292F)) * 2.0;
				double var7 = Mth.cos(player.yBodyRot * 0.017453292F) * 2.0;
				AABB var9 = player.getBoundingBox().inflate(1.0, 0.25, 1.0).move(var5, 0.0, var7);
				betterSweepAttack(var9, var2, attackDamage, null);
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
		if ((!Combatify.CONFIG.attackSpeed() && getAttributeValue(Attributes.ATTACK_SPEED) - 1.5 >= 20) || Combatify.CONFIG.instaAttack())
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

	@Unique
	protected boolean checkSweepAttack() {
		return getAttackStrengthScale(baseValue) > 1.95F && EnchantmentHelper.getSweepingDamageRatio(player) > 0.0F;
	}

	@Unique
	public void betterSweepAttack(AABB var1, float var2, float var3, Entity var4) {
		float sweepingDamageRatio = 1.0F + EnchantmentHelper.getSweepingDamageRatio(player) * var3;
		List<LivingEntity> livingEntities = player.level().getEntitiesOfClass(LivingEntity.class, var1);
		Iterator<LivingEntity> livingEntityIterator = livingEntities.iterator();

		while (true) {
			LivingEntity var8;
			do {
				do {
					do {
						do {
							if (!livingEntityIterator.hasNext()) {
								player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
								if (player.level() instanceof ServerLevel serverLevel) {
									double var11 = -Mth.sin(player.getYRot() * 0.017453292F);
									double var12 = Mth.cos(player.getYRot() * 0.017453292F);
									serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX() + var11, player.getY() + player.getBbHeight() * 0.5, player.getZ() + var12, 0, var11, 0.0, var12, 0.0);
								}

								return;
							}

							var8 = livingEntityIterator.next();
						} while (var8 == player);
					} while (var8 == var4);
				} while (player.isAlliedTo(var8));
			} while (var8 instanceof ArmorStand armorStand && armorStand.isMarker());

			float var9 = var2 + var8.getBbWidth() * 0.5F;
			if (player.distanceToSqr(var8) < (var9 * var9)) {
				MethodHandler.knockback(var8, 0.4, Mth.sin(player.getYRot() * 0.017453292F), (-Mth.cos(player.getYRot() * 0.017453292F)));
				var8.hurt(damageSources().playerAttack(player), sweepingDamageRatio);
			}
		}
	}

	@Override
	public boolean combatify$getMissedAttackRecovery() {
		return missedAttackRecovery;
	}
}
