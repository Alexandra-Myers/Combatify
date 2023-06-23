package net.alexandra.atlas.atlas_combat.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.config.AtlasConfig;
import net.alexandra.atlas.atlas_combat.extensions.*;
import net.alexandra.atlas.atlas_combat.item.NewAttributes;
import net.alexandra.atlas.atlas_combat.util.UtilClass;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

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
	float currentAttackReach;

	@Unique
	public final Player player = ((Player) (Object)this);
	@Inject(method = "hurt", at = @At("HEAD"))
	public void injectSnowballKb(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		oldDamage = amount;
	}
	@ModifyReturnValue(method = "hurt", at = @At(value = "TAIL"))
	public boolean changeReturn(boolean original, @Local(ordinal = 0) DamageSource source, @Local(ordinal = 0) float amount) {
		boolean bl = amount == 0.0F && !original;
		return bl && oldDamage > 0.0F ? false : super.hurt(source, amount);
	}
	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	public void readAdditionalSaveData(CompoundTag nbt, CallbackInfo ci) {
		player.getAttribute(NewAttributes.BLOCK_REACH).setBaseValue(!AtlasCombat.CONFIG.bedrockBlockReach() ? 0 : 2);
		player.getAttribute(NewAttributes.ATTACK_REACH).setBaseValue(0);
		player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(!AtlasCombat.CONFIG.fistDamage() ? 2 : 1);
	}

	@ModifyConstant(method = "createAttributes", constant = @Constant(doubleValue = 1.0))
	private static double changeAttack(double constant) {
		return !AtlasCombat.CONFIG.fistDamage() ? 2 : 1;
	}
	@ModifyReturnValue(method = "createAttributes", at = @At(value = "RETURN"))
	private static AttributeSupplier.Builder createAttributes(AttributeSupplier.Builder original) {
		return original.add(NewAttributes.BLOCK_REACH, !AtlasCombat.CONFIG.bedrockBlockReach() ? 0.0 : 2.0)
			.add(NewAttributes.ATTACK_REACH);
	}
	@Redirect(method = "tick", at = @At(value = "FIELD",target = "Lnet/minecraft/world/entity/player/Player;attackStrengthTicker:I",opcode = Opcodes.PUTFIELD))
	public void redirectAttackStrengthTicker(Player instance, int value) {
		if(player.getUseItem().getItem() instanceof SwordItem swordItem) {
			((ISwordItem)swordItem).addStrengthTimer();
		}
		--instance.attackStrengthTicker;
		setIsParryTicker(getIsParryTicker() + 1);
		if(getIsParryTicker() >= 40) {
			setIsParry(false);
			setIsParryTicker(0);
		}
	}

	@ModifyExpressionValue(method = "hurtCurrentlyUsedShield", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
	public boolean hurtCurrentlyUsedShield(boolean original) {
		return this.useItem.getItem() instanceof IShieldItem || original;
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isSame(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
	public boolean redirectDurability(boolean original) {
		return true;
	}

	@Inject(method = "blockUsingShield", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;canDisableShield()Z"), cancellable = true)
	public void blockUsingShield(@NotNull LivingEntity attacker, CallbackInfo ci) {
		ci.cancel();
	}

	@Override
	public boolean customShieldInteractions(float damage, Item item) {
		player.getCooldowns().addCooldown(item, (int)(damage * 20.0F));
		player.stopUsingItem();
		player.level.broadcastEntityEvent(player, (byte)30);
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
		if (target.isAttackable() && !target.skipAttackInteraction(this) && isAttackAvailable(baseValue))
				this.resetAttackStrengthTicker(true);
	}
	@Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAttackStrengthScale(F)F", ordinal = 0))
	public void doThings(Entity target, CallbackInfo ci, @Local(ordinal = 0) LocalFloatRef attackDamage, @Local(ordinal = 1) LocalFloatRef attackDamageBonus) {
		LivingEntity livingEntity = target instanceof LivingEntity ? (LivingEntity) target : null;
		boolean bl = livingEntity != null;
		if(bl)
			((LivingEntityExtensions)livingEntity).setEnemy(player);
		if(player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof TridentItem && bl) {
			EnchantmentHelper helper = new EnchantmentHelper();
			attackDamageBonus.set(((IEnchantmentHelper)helper).getDamageBonus(player.getMainHandItem(), livingEntity));
		}
		attackDamage.set((float) ((IAttributeInstance) Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_DAMAGE))).calculateValue(attackDamageBonus.get()));
	}
	@ModifyExpressionValue(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAttackStrengthScale(F)F", ordinal = 0))
	public float redirectStrengthCheck(float original) {
		currentAttackReach = (float) this.getAttackRange(player, 2.5);
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
			&& !player.isOnGround()
			&& !player.onClimbable()
			&& !player.isInWater()
			&& !player.hasEffect(MobEffects.BLINDNESS)
			&& !player.isPassenger()
			&& target instanceof LivingEntity;
		if(!AtlasCombat.CONFIG.sprintCritsEnabled()) {
			isCrit &= !isSprinting();
		}
		bl3.set(isCrit || getIsParry());
		if (isCrit) {
			attackDamage.set(attackDamage.get() * 1.5F);
		}
		if (getIsParry()) {
			attackDamage.set(attackDamage.get() * 1.25F);
			setIsParry(false);
		}

	}
	@Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
	public void knockback(LivingEntity instance, double d, double e, double f) {
		((LivingEntityExtensions) instance).newKnockback(d, e, f);
	}
	@Inject(method = "attack", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
	public void createSweep(Entity target, CallbackInfo ci, @Local(ordinal = 1) final boolean bl2, @Local(ordinal = 2) final boolean bl3, @Local(ordinal = 3) LocalBooleanRef bl4, @Local(ordinal = 5) final boolean bl6, @Local(ordinal = 0) final float attackDamage, @Local(ordinal = 0) final double d) {
		bl4.set(false);
		if (!bl3 && !bl2 && this.onGround && d < (double)this.getSpeed())
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
	public void attackAir() {
		if (this.isAttackAvailable(baseValue)) {
			player.swing(InteractionHand.MAIN_HAND);
			float var1 = (float)((ItemExtensions)player.getItemInHand(InteractionHand.MAIN_HAND).getItem()).getAttackDamage(player);
			if (var1 > 0.0F && this.checkSweepAttack()) {
				float var2 = (float) this.getAttackRange(player, 2.5);
				double var5 = (-Mth.sin(player.yBodyRot * 0.017453292F)) * 2.0;
				double var7 = Mth.cos(player.yBodyRot * 0.017453292F) * 2.0;
				AABB var9 = player.getBoundingBox().inflate(1.0, 0.25, 1.0).move(var5, 0.0, var7);
				betterSweepAttack(var9, var2, var1, null);
			}

			this.resetAttackStrengthTicker(false);
		}
	}
	@Override
	public void resetAttackStrengthTicker(boolean hit) {
		this.missedAttackRecovery = !hit;
		if(!AtlasCombat.CONFIG.attackSpeed()) {
			if(getAttribute(Attributes.ATTACK_SPEED).getValue() - 1.5 >= 10) {
				return;
			} else if(attackSpeedsMaxed()) {
				return;
			}
		}
		int var2 = (int) (this.getCurrentItemAttackStrengthDelay() * 2);
		if (var2 > this.attackStrengthTicker) {
			this.attackStrengthStartValue = var2;
			this.attackStrengthTicker = this.attackStrengthStartValue;
		}
	}

	@Inject(method = "getCurrentItemAttackStrengthDelay", at = @At(value = "RETURN"), cancellable = true)
	public void getCurrentItemAttackStrengthDelay(CallbackInfoReturnable<Float> cir) {
		double f = getAttribute(Attributes.ATTACK_SPEED).getValue() - 1.5D;
		f = Mth.clamp(f, 0.1, 1024.0);
		cir.setReturnValue((float) (1.0F / f * 20.0F + 0.5F));
	}

	@Inject(method = "getAttackStrengthScale", at = @At(value = "RETURN"), cancellable = true)
	public void modifyAttackStrengthScale(float baseTime, CallbackInfoReturnable<Float> cir) {
		if (this.attackStrengthStartValue == 0) {
			cir.setReturnValue(2.0F);
			return;
		}
		cir.setReturnValue(Mth.clamp(2.0F * (1.0F - (this.attackStrengthTicker - baseTime) / this.attackStrengthStartValue), 0.0F, 2.0F));
	}

	@Override
	public boolean isAttackAvailable(float baseTime) {
		if (getAttackStrengthScale(baseTime) < 1.0F) {
			return (this.missedAttackRecovery && this.attackStrengthStartValue - this.attackStrengthTicker - baseTime > 4.0F);
		}
		return true;
	}

	protected boolean checkSweepAttack() {
		return getAttackStrengthScale(baseValue) > 1.95F && EnchantmentHelper.getSweepingDamageRatio(player) > 0.0F;
	}

	public void betterSweepAttack(AABB var1, float var2, float var3, Entity var4) {
		float sweepingDamageRatio = 1.0F + EnchantmentHelper.getSweepingDamageRatio(player) * var3;
		List<LivingEntity> livingEntities = player.level.getEntitiesOfClass(LivingEntity.class, var1);
		Iterator<LivingEntity> livingEntityIterator = livingEntities.iterator();

		while (true) {
			LivingEntity var8;
			do {
				do {
					do {
						do {
							if (!livingEntityIterator.hasNext()) {
								player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
								if (player.level instanceof ServerLevel serverLevel) {
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
				((LivingEntityExtensions)var8).newKnockback(0.5, Mth.sin(player.getYRot() * 0.017453292F), (-Mth.cos(player.getYRot() * 0.017453292F)));
				var8.hurt(DamageSource.playerAttack(player), sweepingDamageRatio);
			}
		}
	}

	@Override
	public boolean isItemOnCooldown(ItemStack var1) {
		return player.getCooldowns().isOnCooldown(var1.getItem());
	}

	@Override
	public double getAttackRange(LivingEntity entity, double baseAttackRange) {
		@Nullable final var attackRange = this.getAttribute(NewAttributes.ATTACK_REACH);
		int var2 = 0;
		baseAttackRange = AtlasCombat.CONFIG.attackReach() ? baseAttackRange : Mth.ceil(baseAttackRange);
		float var3 = getAttackStrengthScale(baseValue);
		if (var3 > 1.95F && !player.isCrouching()) {
			var2 = 1;
		}
		return (attackRange != null) ? (baseAttackRange + attackRange.getValue() + var2) : baseAttackRange + var2;
	}

	@Override
	public double getSquaredAttackRange(LivingEntity entity, double sqBaseAttackRange) {
		final var attackRange = getAttackRange(entity, Math.sqrt(sqBaseAttackRange));
		return attackRange * attackRange;
	}

	@Override
	public double getReach(LivingEntity entity, double baseAttackRange) {
		@Nullable final var attackRange = entity.getAttribute(NewAttributes.BLOCK_REACH);
		return (attackRange != null) ? (baseAttackRange + attackRange.getValue()) : baseAttackRange;
	}

	@Override
	public double getSquaredReach(LivingEntity entity, double sqBaseAttackRange) {
		final var attackRange = getReach(entity, Math.sqrt(sqBaseAttackRange));
		return attackRange * attackRange;
	}

	@Override
	public boolean getMissedAttackRecovery() {
		return missedAttackRecovery;
	}

	@Override
	public int getAttackStrengthStartValue() {
		return attackStrengthStartValue;
	}
	public boolean attackSpeedsMaxed() {
		AtlasConfig c = AtlasCombat.CONFIG;
		UtilClass<Float> util = new UtilClass<>();
		return util.compare(7.5F, c.swordAttackSpeed(), c.axeAttackSpeed(), c.woodenHoeAttackSpeed(), c.stoneHoeAttackSpeed(), c.ironHoeAttackSpeed(), c.goldDiaNethHoeAttackSpeed(), c.defaultAttackSpeed(), c.tridentAttackSpeed(), c.fastToolAttackSpeed(), c.fastestToolAttackSpeed(), c.slowToolAttackSpeed(), c.slowestToolAttackSpeed());
	}
}
