package net.atlas.combatify.util;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.component.CustomDataComponents;
import net.atlas.combatify.component.custom.Blocker;
import net.atlas.combatify.config.ConfigurableEntityData;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.config.item.WeaponStats;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.atlas.combatify.item.LongSwordItem;
import net.atlas.combatify.mixin.accessor.LivingEntityAccessor;
import net.atlas.combatify.util.blocking.BlockingType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.minecraft.world.entity.LivingEntity.getSlotForHand;

public class MethodHandler {
	public static int changeIFrames(int original, DamageSource source) {
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
	public static float getAttackStrengthScale(LivingEntity entity, float baseTime) {
		if (entity instanceof Player player)
			return player.getAttackStrengthScale(baseTime);
		return Combatify.CONFIG.chargedAttacks() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA) ? 2.0f : 1.0f;
	}
	public static Vec3 getNearestPointTo(AABB box, Vec3 vec3) {
		double x = Mth.clamp(vec3.x, box.minX, box.maxX);
		double y = Mth.clamp(vec3.y, box.minY, box.maxY);
		double z = Mth.clamp(vec3.z, box.minZ, box.maxZ);

		return new Vec3(x, y, z);
	}

	public static double calculateValueBlacklistChargedReachModifier(@Nullable AttributeInstance attributeInstance) {
		double attributeInstanceBaseValue = attributeInstance.getBaseValue();

		for(AttributeModifier attributeModifier : attributeInstance.getModifiersOrEmpty(AttributeModifier.Operation.ADD_VALUE)) {
			if (attributeModifier.is(Combatify.CHARGED_REACH_ID)) continue;
			attributeInstanceBaseValue += attributeModifier.amount();
		}

		return calculateValueFromBase(attributeInstance, attributeInstanceBaseValue);
	}

	public static double calculateValue(@Nullable AttributeInstance attributeInstance, float damageBonus) {
		if(attributeInstance == null)
			return damageBonus;
		double attributeInstanceBaseValue = attributeInstance.getBaseValue() + damageBonus;

		for(AttributeModifier attributeModifier : attributeInstance.getModifiersOrEmpty(AttributeModifier.Operation.ADD_VALUE)) {
			attributeInstanceBaseValue += attributeModifier.amount();
		}

		return calculateValueFromBase(attributeInstance, attributeInstanceBaseValue);
	}
	public static double calculateValueFromBase(@Nullable AttributeInstance attributeInstance, double attributeInstanceBaseValue) {
		if(attributeInstance == null)
			return attributeInstanceBaseValue;

		double attributeInstanceFinalValue = attributeInstanceBaseValue;

		for(AttributeModifier attributeModifier2 : attributeInstance.getModifiersOrEmpty(AttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
			attributeInstanceFinalValue += attributeInstanceBaseValue * attributeModifier2.amount();
		}

		for(AttributeModifier attributeModifier2 : attributeInstance.getModifiersOrEmpty(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
			attributeInstanceFinalValue *= 1.0 + attributeModifier2.amount();
		}

		return attributeInstance.getAttribute().value().sanitizeValue(attributeInstanceFinalValue);
	}
	public static float getFatigueForTime(int f) {
		if (f < 60 || !Combatify.CONFIG.bowFatigue() || Combatify.getState().equals(Combatify.CombatifyState.VANILLA))
			return 0.5F;
		else
			return f >= 200 ? 10.5F : 0.5F + 10.0F * (float)(f - 60) / 140.0F;
	}

	public static void sweepAttack(Player player, AABB box, float reach, float damage, TriFunction<LivingEntity, Float, DamageSource, Float> enchantFunction, Entity entity) {
		float sweepingDamageRatio = (float) (1.0F + player.getAttributeValue(Attributes.SWEEPING_DAMAGE_RATIO) * damage);
		List<LivingEntity> livingEntities = player.level().getEntitiesOfClass(LivingEntity.class, box);
		DamageSource damageSource = Optional.ofNullable(player.getWeaponItem().getItem().getDamageSource(player)).orElse(player.damageSources().playerAttack(player));

		for (LivingEntity livingEntity : livingEntities) {
			if (livingEntity == player || livingEntity == entity || player.isAlliedTo(livingEntity) || livingEntity instanceof ArmorStand armorStand && armorStand.isMarker())
				continue;
			if (Combatify.CONFIG.sweepingNegatedForTamed()
				&& (livingEntity instanceof OwnableEntity ownableEntity
				&& player.getUUID().equals(ownableEntity.getOwnerUUID())
				|| livingEntity.is(player.getVehicle())
				|| livingEntity.isPassengerOfSameVehicle(player)))
				continue;
			float correctReach = reach + livingEntity.getBbWidth() * 0.5F;
			if (player.distanceToSqr(livingEntity) < (correctReach * correctReach) && player.level() instanceof ServerLevel serverLevel && livingEntity.hurtServer(serverLevel, damageSource, enchantFunction.apply(livingEntity, sweepingDamageRatio, damageSource))) {
				MethodHandler.knockback(livingEntity, 0.4, Mth.sin(player.getYRot() * 0.017453292F), (-Mth.cos(player.getYRot() * 0.017453292F)));
				EnchantmentHelper.doPostAttackEffects(serverLevel, livingEntity, damageSource);
			}
		}
		player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
		player.sweepAttack();
	}

	public static double getKnockbackResistance(LivingEntity entity) {
		double knockbackRes = entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
		ItemStack blockingItem = getBlockingItem(entity).stack();
		boolean delay = getBlockingType(blockingItem).hasDelay() && Combatify.CONFIG.shieldDelay() > 0 && blockingItem.getUseDuration(entity) - entity.getUseItemRemainingTicks() < Combatify.CONFIG.shieldDelay();
		if (!blockingItem.isEmpty() && !delay) {
			Blocker blocker = getBlocking(blockingItem);
			if (!blocker.blockingType().defaultKbMechanics())
				knockbackRes = Math.max(knockbackRes, blocker.tooltip().getShieldKnockbackResistanceValue(blockingItem, entity.getRandom()));
			else
				knockbackRes = Math.min(1.0, knockbackRes + blocker.tooltip().getShieldKnockbackResistanceValue(blockingItem, entity.getRandom()));
		}
		return knockbackRes;
	}

	public static void knockback(LivingEntity entity, double strength, double x, double z) {
		if (!Combatify.CONFIG.ctsKB()) {
			entity.knockback(strength, x, z);
			return;
		}
		if (entity instanceof Creaking creaking && !creaking.canMove()) return;
		double knockbackRes = getKnockbackResistance(entity);

		strength *= 1.0 - knockbackRes;
		if (!(strength <= 0.0F)) {
			entity.hasImpulse = true;
			Vec3 delta = entity.getDeltaMovement();
			while (x * x + z * z < 1.0E-5) {
				x = (Math.random() - Math.random()) * 0.01;
				z = (Math.random() - Math.random()) * 0.01;
			}
			Vec3 diff = (new Vec3(x, 0.0, z)).normalize().scale(strength);
			entity.setDeltaMovement(delta.x / 2.0 - diff.x, entity.onGround() ? Math.min(0.4, strength * 0.75) : Math.min(0.4, delta.y + strength * 0.5), delta.z / 2.0 - diff.z);
		}
	}
	public static void projectileKnockback(LivingEntity entity, double strength, double x, double z) {
		if (entity instanceof Creaking creaking && !creaking.canMove()) return;
		double knockbackRes = getKnockbackResistance(entity);

		strength *= 1.0 - knockbackRes;
		if (!(strength <= 0.0F)) {
			entity.hasImpulse = true;
			Vec3 delta = entity.getDeltaMovement();
			while (x * x + z * z < 1.0E-5) {
				x = (Math.random() - Math.random()) * 0.01;
				z = (Math.random() - Math.random()) * 0.01;
			}
			Vec3 diff = (new Vec3(x, 0.0, z)).normalize().scale(strength);
			entity.setDeltaMovement(delta.x / 2.0 - diff.x, Math.min(0.4, strength * 0.75), delta.z / 2.0 - diff.z);
		}
	}
	public static HitResult pickFromPos(Entity entity, double reach) {
		Vec3 viewVector = entity.getViewVector(1);
		Vec3 pos = entity.getEyePosition(1);
		Vec3 endPos = pos.add(viewVector.scale(reach));
		return entity.level().clip(new ClipContext(pos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
	}
	public static EntityHitResult rayTraceEntity(Entity entity, float partialTicks, double blockReachDistance) {
		Vec3 from = entity.getEyePosition(partialTicks);
		Vec3 look = entity.getViewVector(partialTicks);
		Vec3 to = from.add(look.x * blockReachDistance, look.y * blockReachDistance, look.z * blockReachDistance);

		return ProjectileUtil.getEntityHitResult(
			entity.level(),
			entity,
			from,
			to,
			new AABB(from, to),
			EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != null
				&& e.isPickable()
				&& e instanceof LivingEntity)
		);
	}
	public static HitResult redirectResult(Player player, HitResult instance) {
		if (Combatify.CONFIG.swingThroughGrass() && instance.getType() == HitResult.Type.BLOCK) {
			BlockHitResult blockHitResult = (BlockHitResult) instance;
			BlockPos blockPos = blockHitResult.getBlockPos();
			Level level = player.level();
			boolean bl = !level.getBlockState(blockPos).canOcclude() && !level.getBlockState(blockPos).getBlock().hasCollision;
			EntityHitResult rayTraceResult = MethodHandler.rayTraceEntity(player, 1.0F, getCurrentAttackReach(player, 0.0F));
			if (rayTraceResult != null && bl) {
				double distanceTo = player.distanceTo(rayTraceResult.getEntity());
				HitResult newResult = pickFromPos(player, distanceTo);
				if (newResult.getType() != HitResult.Type.MISS)
					return instance;
				return rayTraceResult;
			} else {
				return instance;
			}
		}
		return instance;
	}
	public static void hurtCurrentlyUsedShield(LivingEntity livingEntity, float f) {
		if (!(livingEntity instanceof Player player)) return;
		FakeUseItem fakeUseItem = getBlockingItem(player);
		ItemStack blockingItem = fakeUseItem.stack();
		if (!blockingItem.isEmpty()) {
			if (!player.level().isClientSide) {
				player.awardStat(Stats.ITEM_USED.get(blockingItem.getItem()));
			}

			if (f >= 3.0F) {
				int i = 1 + Mth.floor(f);
				InteractionHand interactionHand = fakeUseItem.useHand();
				blockingItem.hurtAndBreak(i, player, getSlotForHand(interactionHand));
				fakeUseItem = getBlockingItem(player);
				if (fakeUseItem.stack().isEmpty()) {
					if (interactionHand == InteractionHand.MAIN_HAND) player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
					else player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);

					if (fakeUseItem.isReal()) ((LivingEntityAccessor)player).setUseItem(ItemStack.EMPTY);
					player.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + player.level().random.nextFloat() * 0.4F);
				}
			}
		}
	}
	public static void disableShield(LivingEntity attacker, LivingEntity target, DamageSource damageSource, ItemStack blockingItem) {
		ItemStack attackingItem = attacker.getMainHandItem();
		double piercingLevel = getPiercingLevel(attackingItem);
		if (!(target.level() instanceof ServerLevel serverLevel)) piercingLevel += CustomEnchantmentHelper.getBreach(attackingItem, attacker.getRandom());
		else piercingLevel += CustomEnchantmentHelper.getArmorModifier(serverLevel, attackingItem, target, damageSource);
		if (!(Combatify.CONFIG.armorPiercingDisablesShields() || attacker.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof LongSwordItem))
			piercingLevel = 0;
		boolean canDisable = attacker.canDisableShield() || piercingLevel > 0;
		float damage = Combatify.CONFIG.shieldDisableTime().floatValue();
		ConfigurableEntityData configurableEntityData;
		if ((configurableEntityData = forEntity(target)) != null) {
			if (configurableEntityData.shieldDisableTime() != null)
				damage = configurableEntityData.shieldDisableTime().floatValue();
		}
		modifyTime: {
			if (attacker.level() instanceof ServerLevel serverLevel) {
				if (canDisable) {
					damage = CustomEnchantmentHelper.modifyShieldDisable(serverLevel, null, target, attacker, damageSource, damage);
					break modifyTime;
				}
				float newDamage = CustomEnchantmentHelper.modifyShieldDisable(serverLevel, null, target, attacker, damageSource, 0);
				if (newDamage > 0) {
					damage = newDamage;
					canDisable = true;
				}
			}
		}
		if (canDisable && damage > 0 && getBlockingType(blockingItem).canBeDisabled()) {
			if (piercingLevel > 0)
				attacker.combatify$setPiercingNegation(piercingLevel);
			disableShield(target, damage, blockingItem);
		}
	}
	public static void arrowDisable(LivingEntity target, DamageSource damageSource, AbstractArrow abstractArrow, ItemStack blockingItem) {
		if (!getBlockingType(blockingItem).canBeDisabled()) return;
		float damage = Combatify.CONFIG.shieldDisableTime().floatValue();
		ConfigurableEntityData configurableEntityData;
		if ((configurableEntityData = forEntity(target)) != null) {
			if (configurableEntityData.shieldDisableTime() != null)
				damage = configurableEntityData.shieldDisableTime().floatValue();
		}
		if (target.level() instanceof ServerLevel serverLevel) {
			damage = CustomEnchantmentHelper.modifyShieldDisable(serverLevel, abstractArrow.getPickupItemStackOrigin(), target, abstractArrow, damageSource, damage);
		}
		disableShield(target, Math.max(damage, 0), blockingItem);
	}
	public static void disableShield(LivingEntity target, float damage, ItemStack item) {
		getCooldowns(target).addCooldown(item, (int)(damage * 20.0F));
		if (Combatify.shields.contains(item.getItem()))
			for (Item shield : Combatify.shields)
				if (item.getItem() != shield)
					getCooldowns(target).addCooldown(new ItemStack(shield), (int)(damage * 20.0F));
		target.stopUsingItem();
		target.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + target.level().random.nextFloat() * 0.4F);
		target.level().broadcastEntityEvent(target, (byte)30);
	}
	public static FakeUseItem getBlockingItem(LivingEntity entity) {
		if (entity.isUsingItem() && !entity.getUseItem().isEmpty()) {
			if (entity.getUseItem().getUseAnimation() == ItemUseAnimation.BLOCK) {
				return new FakeUseItem(entity.getUseItem(), entity.getUsedItemHand(), true);
			}
		} else if (((entity.onGround() && entity.isCrouching()) || entity.isPassenger()) && (entity.combatify$hasEnabledShieldOnCrouch() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA))) {
			for (InteractionHand hand : InteractionHand.values()) {
				ItemStack stack = entity.getItemInHand(hand);
				boolean stillRequiresCharge = Combatify.CONFIG.shieldOnlyWhenCharged() && entity instanceof Player player && player.getAttackStrengthScale(1.0F) < Combatify.CONFIG.shieldChargePercentage() / 100F && getBlockingType(stack).requireFullCharge();
				boolean canUse = !(entity instanceof Player player) || getBlocking(stack).canUse(stack, entity.level(), player, hand);
				if (!stillRequiresCharge && !stack.isEmpty() && stack.getUseAnimation() == ItemUseAnimation.BLOCK && !isItemOnCooldown(entity, stack) && getBlockingType(stack).canCrouchBlock() && canUse) {
					return new FakeUseItem(stack, hand, false);
				}
			}
		}

		return new FakeUseItem(ItemStack.EMPTY, null, true);
	}
	public static boolean isItemOnCooldown(LivingEntity entity, ItemStack var1) {
		return getCooldowns(entity).isOnCooldown(var1);
	}
	public static double updatePlayerReach(Player player, AttributeInstance attackRange, float strengthScale) {
		double chargedBonus = 0;
		float charge = Combatify.CONFIG.chargedAttacks() ? 1.95F : 0.9F;
		if (attackRange != null) {
			Item item = player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
			chargedBonus = item.getChargedAttackBonus();
			AttributeModifier modifier = new AttributeModifier(Combatify.CHARGED_REACH_ID, chargedBonus, AttributeModifier.Operation.ADD_VALUE);
			if (strengthScale > charge && !player.isCrouching() && Combatify.CONFIG.chargedReach())
				attackRange.addOrUpdateTransientModifier(modifier);
			else
				attackRange.removeModifier(modifier);
		}
		return chargedBonus;
	}
	public static double getCurrentAttackReach(Player player, float baseTime) {
		@Nullable final var attackRange = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return attackRange != null ? attackRange.getValue() : 3;
		double baseAttackRange = Combatify.CONFIG.attackReach() ? 2.5 : 3;
		float strengthScale = player.getAttackStrengthScale(baseTime);
		double chargedBonus = updatePlayerReach(player, attackRange, strengthScale);
		if (strengthScale < (Combatify.CONFIG.chargedAttacks() ? 1.95 : 0.9) || player.isCrouching() || !Combatify.CONFIG.chargedReach()) {
			chargedBonus = 0;
		}
		return (attackRange != null) ? attackRange.getValue() : baseAttackRange + chargedBonus;
	}

	public static double getCurrentAttackReachWithoutChargedReach(Player player) {
		@Nullable final var attackRange = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return attackRange != null ? attackRange.getValue() : 3;
		double baseAttackRange = Combatify.CONFIG.attackReach() ? 2.5 : 3;
		return (attackRange != null) ? calculateValueBlacklistChargedReachModifier(attackRange) : baseAttackRange;
	}
	public static void voidReturnLogic(ThrownTrident trident, EntityDataAccessor<Byte> ID_LOYALTY) {
		int j = trident.getEntityData().get(ID_LOYALTY);
		if (Combatify.CONFIG.tridentVoidReturn() && trident.getY() < trident.level().getMinY() && j > 0) {
			if (!trident.isAcceptibleReturnOwner()) {
				trident.discard();
			} else {
				trident.setNoPhysics(true);
				Vec3 vec3 = trident.getEyePosition().subtract(trident.position());
				trident.setPosRaw(trident.getX(), trident.getY() + vec3.y * 0.015 * j, trident.getZ());
				if (trident.level().isClientSide) {
					trident.yOld = trident.getY();
				}

				double d = 0.05 * j;
				trident.setDeltaMovement(trident.getDeltaMovement().scale(0.95).add(vec3.normalize().scale(d)));
				if (trident.clientSideReturnTridentTickCount == 0) {
					trident.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
				}

				++trident.clientSideReturnTridentTickCount;
			}
		}
	}
	public static Vec3 project(Vec3 originalVec, Vec3 newVec) {
	    Vec3 normalized = newVec.normalize();
	    double d = originalVec.dot(newVec);
	    return normalized.multiply(d, d, d);
	}

	public static void blockedByShield(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker, DamageSource damageSource) {
		ItemStack blockingItem = MethodHandler.getBlockingItem(target).stack();
		EquipmentSlot equipmentSlot = target.getEquipmentSlotForItem(blockingItem);
		getBlocking(blockingItem).doEffect(serverLevel, equipmentSlot, blockingItem, target, attacker, damageSource);
	}
	public static ItemCooldowns createItemCooldowns() {
		return new ItemCooldowns();
	}
	public static ItemCooldowns getCooldowns(LivingEntity livingEntity) {
		if (livingEntity instanceof Player player) return player.getCooldowns();
		return livingEntity.combatify$getFallbackCooldowns();
	}

	public static boolean isMobGuarding(Mob mob) {
		return mob.combatify$isGuarding();
	}
	public static float getPinchHealth(LivingEntity livingEntity, Difficulty difficulty) {
		return switch (difficulty) {
			case PEACEFUL, EASY -> 0.0F;
			case NORMAL -> livingEntity.getMaxHealth() / 4;
			case HARD -> livingEntity.getMaxHealth() / 2;
		};
	}

	public static boolean shouldSprintToCloseInOnTarget(Difficulty difficulty, double change) {
		if (difficulty == Difficulty.PEACEFUL || difficulty == Difficulty.EASY) return false;
		return change < (difficulty == Difficulty.NORMAL ? -0.25 : 0);
	}

	public static boolean processSprintAbility(Entity entity, Operation<Boolean> base) {
		return switch (entity) {
			case AbstractPiglin ignored -> true;
			case AbstractSkeleton skeleton -> {
				ItemStack itemstack = skeleton.getItemInHand(ProjectileUtil.getWeaponHoldingHand(skeleton, Items.BOW));
				yield !itemstack.is(Items.BOW);
			}
			case Zombie zombie -> !(zombie.isEyeInFluid(FluidTags.WATER) || zombie.isEyeInFluid(FluidTags.LAVA));
			case null, default -> base.call();
		};
	}

	public static double getPiercingLevel(ItemStack itemStack) {
		return itemStack.getOrDefault(CustomDataComponents.PIERCING_LEVEL, 0F);
	}

	@SuppressWarnings("deprecation")
	public static ConfigurableItemData forItem(Item item) {
		if (Combatify.ITEMS != null && !Combatify.ITEMS.isModifying) {
			List<ConfigurableItemData> results = new ArrayList<>();
			Combatify.ITEMS.items.get().forEach(configDataWrapper -> {
				ConfigurableItemData result = configDataWrapper.match(item.builtInRegistryHolder());
				if (result != null) results.add(result);
			});
			Double chargedReach = null;
			Double useSeconds = null;
			for (ConfigurableItemData configurableItemData : results) {
				chargedReach = conditionalChange(configurableItemData.weaponStats().chargedReach(), chargedReach);
				useSeconds = conditionalChange(configurableItemData.useDuration(), useSeconds);
			}
			WeaponStats weaponStats = new WeaponStats(chargedReach);
			ConfigurableItemData configurableItemData = new ConfigurableItemData(weaponStats, useSeconds);
			if (configurableItemData.equals(ConfigurableItemData.EMPTY)) return null;
			return configurableItemData;
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	public static ConfigurableEntityData forEntityType(EntityType<?> entityType) {
		if (Combatify.ITEMS != null && !Combatify.ITEMS.isModifying) {
			List<ConfigurableEntityData> results = new ArrayList<>();
			Combatify.ITEMS.entities.get().forEach(configDataWrapper -> {
				ConfigurableEntityData result = configDataWrapper.match(entityType.builtInRegistryHolder());
				if (result != null) results.add(result);
			});
			Integer attackInterval = null;
			Double shieldDisableTime = null;
			Boolean isMiscEntity = null;
			for (ConfigurableEntityData configurableEntityData : results) {
				attackInterval = conditionalChange(configurableEntityData.attackInterval(), attackInterval);
				shieldDisableTime = conditionalChange(configurableEntityData.shieldDisableTime(), shieldDisableTime);
				isMiscEntity = conditionalChange(configurableEntityData.isMiscEntity(), isMiscEntity);
			}
			ConfigurableEntityData configurableEntityData = new ConfigurableEntityData(Optional.ofNullable(attackInterval), Optional.ofNullable(shieldDisableTime), Optional.ofNullable(isMiscEntity));
			if (configurableEntityData.equals(ConfigurableEntityData.EMPTY)) return null;
			return configurableEntityData;
		}
		return null;
	}

	public static ConfigurableEntityData forEntity(Entity entity) {
		return forEntityType(entity.getType());
	}

	public static <T> T conditionalChange(T source, T destination) {
		return source == null ? destination : source;
	}

	public static BlockingType getBlockingType(ItemStack itemStack) {
		return getBlocking(itemStack).blockingType();
	}

	public static Blocker getBlocking(ItemStack itemStack) {
		return itemStack.getOrDefault(CustomDataComponents.BLOCKER, Blocker.EMPTY);
	}
}
