package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.atlas.combatify.extensions.MobExtensions;
import net.atlas.combatify.item.TieredShieldItem;
import net.atlas.combatify.mixin.accessor.CombatTrackerAccessor;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import static net.atlas.combatify.util.MethodHandler.*;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity implements MobExtensions {
	@Unique
	private double targetDist = Integer.MAX_VALUE;
	@Unique
	private boolean overrideSprintLogic = false;
	@Shadow
	@Final
	private static EntityDataAccessor<Byte> DATA_MOB_FLAGS_ID;

	@Shadow
	@Nullable
	public abstract LivingEntity getTarget();

	@Mutable
	@Shadow
	@Final
	private static List<EquipmentSlot> EQUIPMENT_POPULATION_ORDER;

	protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "aiStep", at = @At("HEAD"))
	public void updateSprinting(CallbackInfo ci) {
		if (isBlocking()) {
			setDeltaMovement(getDeltaMovement().multiply(0.4, 1.0, 0.4));
			setSprinting(false);
		}
		if (!this.level().isClientSide()) {
			Entity target = getTarget();
			double targetDistO;
			if (!overrideSprintLogic) {
				if (target != null && !isBaby()) {
					targetDistO = this.targetDist;
					this.targetDist = this.distanceToSqr(target);
					if (this.tickCount % 10 == 0) {
						Entity sprintingMob = this;
						Entity vehicle;
						if (sprintingMob.isPassenger() && (vehicle = sprintingMob.getVehicle()) != null)
							sprintingMob = vehicle;
						boolean meetsSprintConditions = !(isUsingItem() || isBlocking())
							&& !hasEffect(MobEffects.BLINDNESS)
							&& sprintingMob.canSprint()
							&& !isFallFlying();
						double change = targetDistO - targetDist;
						Difficulty difficulty = level().getDifficulty();
						sprintingMob.setSprinting((this.getHealth() <= getPinchHealth(this, difficulty) || shouldSprintToCloseInOnTarget(difficulty, change) || targetDist > 25.0) && meetsSprintConditions);
					}
				} else {
					targetDist = Integer.MAX_VALUE;
					Entity sprintingMob = this;
					Entity vehicle;
					if (sprintingMob.isPassenger() && (vehicle = sprintingMob.getVehicle()) != null)
						sprintingMob = vehicle;
					sprintingMob.setSprinting(false);
				}
			}
			if (Combatify.CONFIG.mobsCanGuard() && tickCount % 5 == 0) {
				boolean shouldGuard = ((CombatTrackerAccessor)getCombatTracker()).isInCombat();
				boolean isInHittingRange = false;
				for (CombatEntry combatEntry : ((CombatTrackerAccessor) getCombatTracker()).getEntries()) {
					if (combatEntry.source().is(DamageTypeTags.IS_PROJECTILE)) {
						isInHittingRange = true;
						break;
					}
					if (combatEntry.source().getEntity() instanceof LivingEntity livingEntity) {
						for (EquipmentSlot equipmentSlot : EquipmentSlotGroup.HAND.slots()) {
							Item heldItem = livingEntity.getItemBySlot(equipmentSlot).getItem();
							if (heldItem instanceof ProjectileItem || heldItem instanceof ProjectileWeaponItem) {
								isInHittingRange = true;
								break;
							}
						}
						if (livingEntity instanceof Player player) {
							double distanceToAttacker = Math.sqrt(player.distanceToSqr(MethodHandler.getNearestPointTo(this.getBoundingBox(), player.getEyePosition())));
							double reach = MethodHandler.getCurrentAttackReachWithoutChargedReach(player) + (Combatify.CONFIG.chargedReach() ? 1 : 0);
							if (distanceToAttacker <= reach) isInHittingRange = true;
						} else if (livingEntity instanceof Mob mob && mob.isWithinMeleeAttackRange(this)) {
							isInHittingRange = true;
							break;
						}
					}
				}
				shouldGuard &= isInHittingRange;
				if (!(canGuard() && shouldGuard) && combatify$isGuarding()) stopGuarding();
				else if (canGuard() && shouldGuard && !combatify$isGuarding()) startGuarding();
			}
		}
	}

	@WrapOperation(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;modifyDamage(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)F"))
	public float getDamageBonus(ServerLevel serverLevel, ItemStack itemStack, Entity entity, DamageSource damageSource, float f, Operation<Float> original) {
		return CustomEnchantmentHelper.modifyDamage(serverLevel, itemStack, entity, damageSource, f, original);
	}
	@WrapOperation(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
	public void knockback(LivingEntity instance, double d, double e, double f, Operation<Void> original) {
		if (Combatify.CONFIG.ctsKB()) MethodHandler.knockback(instance, d, e, f);
		else original.call(instance, d, e, f);
	}

	@Override
	public boolean combatify$overrideSprintLogic() {
		return overrideSprintLogic;
	}

	@Override
	public void combatify$setOverrideSprintLogic(boolean overrideSprintLogic) {
		this.overrideSprintLogic = overrideSprintLogic;
	}

	@Unique
	protected boolean canGuard() {
		return Combatify.CONFIG.mobsCanGuard() && onGround() && !getBlockingType(this.getOffhandItem()).isEmpty() && !MethodHandler.getCooldowns(Mob.class.cast(this)).isOnCooldown(this.getOffhandItem());
	}

	@Unique
	public void startGuarding() {
		this.startUsingItem(InteractionHand.OFF_HAND);
		this.setSprinting(false);
		this.setGuarding(true);
	}

	@Unique
	public void stopGuarding() {
		this.stopUsingItem();
		this.setGuarding(false);
	}

	@Unique
	public void setGuarding(boolean p_21562_) {
		byte b0 = this.entityData.get(DATA_MOB_FLAGS_ID);
		this.entityData.set(DATA_MOB_FLAGS_ID, p_21562_ ? (byte)(b0 | 8) : (byte)(b0 & -9));
	}

	@Override
	public boolean combatify$isGuarding() {
		return (this.entityData.get(DATA_MOB_FLAGS_ID) & 8) != 0;
	}

	@ModifyExpressionValue(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getKnockback(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;)F"))
	public float addSprintKB(float original) {
		return original + (Combatify.CONFIG.mobsCanSprint() && isSprinting() ? 1.0F : 0.0F);
	}

	@Inject(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
	public void resetSprint(ServerLevel serverLevel, Entity entity, CallbackInfoReturnable<Boolean> cir) {
		if (isSprinting()) setSprinting(false);
	}

	@Inject(method = "populateDefaultEquipmentSlots", at = @At("HEAD"))
	public void addOffhand(CallbackInfo ci) {
		if (Combatify.isLoaded && Combatify.mobConfigIsDirty) {
			if (Combatify.CONFIG.mobsCanGuard()) {
				List<EquipmentSlot> clone = new ArrayList<>(EQUIPMENT_POPULATION_ORDER);
				clone.add(EquipmentSlot.OFFHAND);
				EQUIPMENT_POPULATION_ORDER = clone;
			} else {
				List<EquipmentSlot> clone = new ArrayList<>(EQUIPMENT_POPULATION_ORDER);
				clone.remove(EquipmentSlot.OFFHAND);
				EQUIPMENT_POPULATION_ORDER = clone;
			}
		}
	}

	@WrapOperation(method = "populateDefaultEquipmentEnchantments", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EquipmentSlot;getType()Lnet/minecraft/world/entity/EquipmentSlot$Type;"))
	public EquipmentSlot.Type fakeType1(EquipmentSlot instance, Operation<EquipmentSlot.Type> original) {
		if (Combatify.CONFIG.mobsCanGuard() && instance == EquipmentSlot.OFFHAND) return EquipmentSlot.Type.HUMANOID_ARMOR;
		return original.call(instance);
	}

	@ModifyReturnValue(method = "getEquipmentForSlot", at = @At("RETURN"))
	private static Item enableShields(Item original, @Local(ordinal = 0, argsOnly = true) EquipmentSlot equipmentSlot, @Local(ordinal = 0, argsOnly = true) int level) {
		if (Combatify.CONFIG.mobsCanGuard() && equipmentSlot == EquipmentSlot.OFFHAND) {
			if (Combatify.CONFIG.tieredShields()) return switch (level) {
				case 0 -> Items.SHIELD;
				case 1 -> TieredShieldItem.COPPER_SHIELD;
				case 4 -> TieredShieldItem.IRON_SHIELD;
				case 5 -> TieredShieldItem.DIAMOND_SHIELD;
                default -> TieredShieldItem.GOLD_SHIELD;
            };
			else return Items.SHIELD;
		}
		return original;
	}
}
