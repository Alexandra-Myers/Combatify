package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.item.TieredShieldItem;
import net.atlas.combatify.util.CustomEnchantmentHelper;
import net.atlas.combatify.extensions.*;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
@Mixin(value = Player.class, priority = 1400)
public abstract class PlayerMixin extends LivingEntity implements PlayerExtensions, LivingEntityExtensions {
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

	@Unique
	protected int attackStrengthStartValue;

	@Unique
	public boolean missedAttackRecovery;
	@Unique
	@Final
	public float baseValue = 1.0F;
	@Unique
	float oldDamage;
	@Unique
	boolean attacked;

	@Unique
	public final Player player = ((Player) (Object)this);
	@Inject(method = "hurt", at = @At("HEAD"))
	public void injectSnowballKb(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		oldDamage = amount;
	}
	@Inject(method = "hurt", at = @At(value = "RETURN", ordinal = 3), cancellable = true)
	public void changeReturn(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		boolean bl = amount == 0.0F && oldDamage <= 0.0F;
 		if(bl && Combatify.CONFIG.snowballKB())
			cir.setReturnValue(super.hurt(source, amount));
	}
	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	public void readAdditionalSaveData(CompoundTag nbt, CallbackInfo ci) {
		Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(!Combatify.CONFIG.fistDamage() ? 2 : 1);
		Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_SPEED)).setBaseValue(Combatify.CONFIG.baseHandAttackSpeed() + 1.5);
		Objects.requireNonNull(player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE)).setBaseValue(Combatify.CONFIG.attackReach() ? 2.5 : 3);
	}

	@ModifyExpressionValue(method = "createAttributes", at = @At(value = "CONSTANT", args = "doubleValue=1.0"))
	private static double changeAttack(double constant) {
		return !Combatify.CONFIG.fistDamage() ? 1 + constant : constant;
	}
	@ModifyReturnValue(method = "createAttributes", at = @At(value = "RETURN"))
	private static AttributeSupplier.Builder createAttributes(AttributeSupplier.Builder original) {
		return original.add(Attributes.ENTITY_INTERACTION_RANGE, Combatify.CONFIG.attackReach() ? 2.5 : 3).add(Attributes.ATTACK_SPEED, Combatify.CONFIG.baseHandAttackSpeed() + 1.5);
	}
	@Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At(value = "HEAD"))
	public void addServerOnlyCheck(ItemStack itemStack, boolean bl, boolean bl2, CallbackInfoReturnable<ItemEntity> cir) {
		if(Combatify.unmoddedPlayers.contains(player.getUUID()))
			Combatify.isPlayerAttacking.put(player.getUUID(), false);
	}
	@Redirect(method = "tick", at = @At(value = "FIELD",target = "Lnet/minecraft/world/entity/player/Player;attackStrengthTicker:I",opcode = Opcodes.PUTFIELD))
	public void redirectAttackStrengthTicker(Player instance, int value) {
		--instance.attackStrengthTicker;
	}

	@ModifyExpressionValue(method = "hurtCurrentlyUsedShield", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
	public boolean hurtCurrentlyUsedShield(boolean original) {
		return !((ItemExtensions)useItem.getItem()).getBlockingType().isEmpty() || original;
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;resetAttackStrengthTicker()V"))
	public void redirectDurability(Player instance, Operation<Void> original) {
		if (Combatify.CONFIG.resetOnItemChange()) {
			((PlayerExtensions) instance).resetAttackStrengthTicker(false);
			return;
		}
		original.call(instance);
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
		this.level().broadcastEntityEvent(this, (byte)30);
		return true;
	}

	@Override
	public boolean hasEnabledShieldOnCrouch() {
		return true;
	}

	@Inject(method = "attack", at = @At(value = "HEAD"), cancellable = true)
	public void attack(Entity target, CallbackInfo ci) {
		if(!isAttackAvailable(baseValue)) ci.cancel();
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
			this.resetAttackStrengthTicker(!Combatify.CONFIG.improvedMiscEntityAttacks() || !isMiscTarget);
		}
	}
	@Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAttackStrengthScale(F)F", ordinal = 0))
	public void doThings(Entity target, CallbackInfo ci, @Local(ordinal = 0) LocalFloatRef attackDamage, @Local(ordinal = 1) LocalFloatRef attackDamageBonus) {
		attacked = true;
		LivingEntity livingEntity = target instanceof LivingEntity ? (LivingEntity) target : null;
		boolean bl = livingEntity != null;
		if(player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof TridentItem && bl)
			attackDamageBonus.set(CustomEnchantmentHelper.getDamageBonus(player.getMainHandItem(), livingEntity));
		if (Combatify.CONFIG.strengthAppliesToEnchants())
			attackDamage.set((float) MethodHandler.calculateValue(player.getAttribute(Attributes.ATTACK_DAMAGE), attackDamageBonus.get()));
	}
	@ModifyExpressionValue(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAttackStrengthScale(F)F", ordinal = 0))
	public float redirectStrengthCheck(float original) {
		return !Combatify.CONFIG.attackDecay() || (missedAttackRecovery && this.attackStrengthStartValue - this.attackStrengthTicker > 4.0F) ? 1.0F : Math.min(original, 1.0F);
	}
	@Inject(method = "resetAttackStrengthTicker", at = @At(value = "HEAD"), cancellable = true)
	public void reset(CallbackInfo ci) {
		ci.cancel();
	}
	@Inject(method = "attack", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;walkDist:F"))
	public void injectCrit(Entity target, CallbackInfo ci, @Local(ordinal = 0) LocalFloatRef attackDamage, @Local(ordinal = 1) final float attackDamageBonus, @Local(ordinal = 2)LocalBooleanRef bl3) {
		if (Combatify.CONFIG.strengthAppliesToEnchants())
			attackDamage.set(attackDamage.get() - attackDamageBonus);
		if (Combatify.CONFIG.attackDecay() && !Combatify.CONFIG.sprintCritsEnabled())
			return;
		if (bl3.get())
			attackDamage.set(attackDamage.get() / 1.5F);
		boolean isCrit = player.fallDistance > 0.0F
			&& !player.onGround()
			&& !player.onClimbable()
			&& !player.isInWater()
			&& !player.hasEffect(MobEffects.BLINDNESS)
			&& !player.isPassenger()
			&& target instanceof LivingEntity;
		if (!Combatify.CONFIG.sprintCritsEnabled())
			isCrit &= !isSprinting();
		if (!Combatify.CONFIG.attackDecay())
			isCrit &= player.getAttackStrengthScale(0.5F) > 0.9;
		bl3.set(isCrit);
		if (isCrit)
			attackDamage.set(attackDamage.get() * 1.5F);
	}
	@Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
	public void knockback(LivingEntity instance, double d, double e, double f) {
		MethodHandler.knockback(instance, d, e, f);
	}
	@Inject(method = "attack", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
	public void createSweep(Entity target, CallbackInfo ci, @Local(ordinal = 1) final boolean bl2, @Local(ordinal = 2) final boolean bl3, @Local(ordinal = 3) LocalBooleanRef bl4, @Local(ordinal = 0) final float attackDamage, @Local(ordinal = 0) final double d) {
		bl4.set(false);
		if (!bl3 && !bl2 && this.onGround() && d < (double)this.getSpeed())
			bl4.set(checkSweepAttack());
		if(bl4.get()) {
			AABB box = target.getBoundingBox().inflate(1.0, 0.25, 1.0);
			this.betterSweepAttack(box, (float) MethodHandler.getCurrentAttackReach(player, 1.0F), attackDamage, target);
			bl4.set(false);
		}
	}
	@Inject(method = "attack", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;hurtMarked:Z", shift = At.Shift.BEFORE, ordinal = 0))
	public void resweep(Entity target, CallbackInfo ci, @Local(ordinal = 3) LocalBooleanRef bl4) {
		bl4.set(checkSweepAttack());
	}
	@Override
	public void attackAir() {
		if (this.isAttackAvailable(baseValue)) {
			customSwing(InteractionHand.MAIN_HAND);
			float attackDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
			if (attackDamage > 0.0F && this.checkSweepAttack() && Combatify.CONFIG.canSweepOnMiss()) {
				float currentAttackReach = (float) MethodHandler.getCurrentAttackReach(player, 1.0F);
				double dirX = (-Mth.sin(player.yBodyRot * 0.017453292F)) * 2.0;
				double dirZ = Mth.cos(player.yBodyRot * 0.017453292F) * 2.0;
				AABB sweepBox = player.getBoundingBox().inflate(1.0, 0.25, 1.0).move(dirX, 0.0, dirZ);
				Combatify.LOGGER.info("Swept");
				betterSweepAttack(sweepBox, currentAttackReach, attackDamage, null);
			}
			this.resetAttackStrengthTicker(false);
		}
	}
	@Override
	public void customSwing(InteractionHand interactionHand) {
		swing(interactionHand, false);
	}
	@Override
	public void resetAttackStrengthTicker(boolean hit) {
		this.missedAttackRecovery = !hit && Combatify.CONFIG.missedAttackRecovery();
		if (!Combatify.CONFIG.attackSpeed()) {
			if(Objects.requireNonNull(getAttribute(Attributes.ATTACK_SPEED)).getValue() - 1.5 >= 20) {
				return;
			}
		}
		if (Combatify.CONFIG.instaAttack()) {
			return;
		}
		int var2 = (int) (this.getCurrentItemAttackStrengthDelay()) * (Combatify.CONFIG.chargedAttacks() ? 2 : 1);
		Combatify.LOGGER.info("Ticks for charge: " + var2);
		if (var2 > this.attackStrengthTicker) {
			this.attackStrengthStartValue = var2;
			this.attackStrengthTicker = this.attackStrengthStartValue;
		}
	}

	@Inject(method = "getCurrentItemAttackStrengthDelay", at = @At(value = "RETURN"), cancellable = true)
	public void getCurrentItemAttackStrengthDelay(CallbackInfoReturnable<Float> cir) {
		boolean hasVanilla = getAttribute(Attributes.ATTACK_SPEED).getModifier(Item.BASE_ATTACK_SPEED_UUID) != null;
		float f = (float)(getAttributeValue(Attributes.ATTACK_SPEED) - 1.5f);
		if (hasVanilla)
			f += 1.5f;
		f = Mth.clamp(f, 0.1F, 1024.0F);
		cir.setReturnValue(1.0F / f * 20.0F + (hasVanilla ? 0 : 0.5F));
	}

	@Inject(method = "getAttackStrengthScale", at = @At(value = "HEAD"), cancellable = true)
	public void modifyAttackStrengthScale(float baseTime, CallbackInfoReturnable<Float> cir) {
		float charge = Combatify.CONFIG.chargedAttacks() ? 2.0F : 1.0F;
		if (this.attackStrengthStartValue == 0) {
			cir.setReturnValue(charge);
			return;
		}
		cir.setReturnValue(Mth.clamp(charge * (1.0F - (this.attackStrengthTicker - baseTime) / this.attackStrengthStartValue), 0.0F, charge));
	}

	@Override
	public boolean isAttackAvailable(float baseTime) {
		if (getAttackStrengthScale(baseTime) < 1.0F && !Combatify.CONFIG.canAttackEarly()) {
			return (this.missedAttackRecovery && this.attackStrengthStartValue - (this.attackStrengthTicker - baseTime) > 4.0F);
		}
		return true;
	}

	protected boolean checkSweepAttack() {
		float charge = Combatify.CONFIG.chargedAttacks() ? 1.95F : 0.9F;
		boolean sweepingItem = ((ItemExtensions)getMainHandItem().getItem()).canSweep();
		boolean sweep = getAttackStrengthScale(baseValue) > charge && (EnchantmentHelper.getSweepingDamageRatio(player) > 0.0F || sweepingItem);
		if (!Combatify.CONFIG.sweepWithSweeping())
			return sweepingItem && sweep;
		return sweep;
	}

	public void betterSweepAttack(AABB box, float reach, float damage, Entity entity) {
		float sweepingDamageRatio = 1.0F + EnchantmentHelper.getSweepingDamageRatio(player) * damage;
		List<LivingEntity> livingEntities = player.level().getEntitiesOfClass(LivingEntity.class, box);

		for (LivingEntity livingEntity : livingEntities) {
			if (livingEntity == this || livingEntity == entity || this.isAlliedTo(livingEntity) || livingEntity instanceof ArmorStand armorStand && armorStand.isMarker())
				continue;
			if (Combatify.CONFIG.sweepingNegatedForTamed() && livingEntity instanceof OwnableEntity ownableEntity && ownableEntity.getOwnerUUID() == player.getUUID())
				continue;
			float correctReach = reach + livingEntity.getBbWidth() * 0.5F;
			if (player.distanceToSqr(livingEntity) < (correctReach * correctReach)) {
				MethodHandler.knockback(livingEntity, 0.4, Mth.sin(player.getYRot() * 0.017453292F), (-Mth.cos(player.getYRot() * 0.017453292F)));
				livingEntity.hurt(damageSources().playerAttack(player), sweepingDamageRatio);
			}
		}
		player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
		if (player.level() instanceof ServerLevel serverLevel) {
			double dirX = -Mth.sin(player.getYRot() * 0.017453292F);
			double dirZ = Mth.cos(player.getYRot() * 0.017453292F);
			serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX() + dirX, player.getY() + player.getBbHeight() * 0.5, player.getZ() + dirZ, 0, dirX, 0.0, dirZ, 0.0);
		}
	}

	@Override
	public boolean getMissedAttackRecovery() {
		return missedAttackRecovery;
	}
	@Override
	public int getAttackStrengthStartValue() {
		return attackStrengthStartValue;
	}
	@ModifyReturnValue(method = "entityInteractionRange", at = @At(value = "RETURN"))
	public double getCurrentAttackReach(double original) {
		return MethodHandler.getCurrentAttackReach(player, 0.0F);
	}
}
