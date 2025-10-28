package net.atlas.combatify.util;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.extensions.LivingEntityExtensions;
import net.atlas.combatify.item.TieredShieldItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MethodHandler {
	public static Vec3 getNearestPointTo(AABB box, Vec3 vec3) {
		double x = Mth.clamp(vec3.x, box.minX, box.maxX);
		double y = Mth.clamp(vec3.y, box.minY, box.maxY);
		double z = Mth.clamp(vec3.z, box.minZ, box.maxZ);

		return new Vec3(x, y, z);
	}
	public static double calculateValue(@Nullable AttributeInstance attributeInstance, float damageBonus) {
		if(attributeInstance == null)
			return damageBonus;
		double attributeInstanceBaseValue = attributeInstance.getBaseValue();

		for(AttributeModifier attributeModifier : attributeInstance.getModifiersOrEmpty(AttributeModifier.Operation.ADDITION)) {
			attributeInstanceBaseValue += attributeModifier.getAmount();
		}

		double withDamageBonus = attributeInstanceBaseValue + damageBonus;

		for(AttributeModifier attributeModifier2 : attributeInstance.getModifiersOrEmpty(AttributeModifier.Operation.MULTIPLY_BASE)) {
			withDamageBonus += attributeInstanceBaseValue * attributeModifier2.getAmount();
		}

		for(AttributeModifier attributeModifier2 : attributeInstance.getModifiersOrEmpty(AttributeModifier.Operation.MULTIPLY_TOTAL)) {
			withDamageBonus *= 1.0 + attributeModifier2.getAmount();
		}

		return attributeInstance.getAttribute().sanitizeValue(withDamageBonus);
	}
	public static void sweepAttack(Player player, AABB box, float reach, float damage, Entity entity) {
		float sweepingDamageRatio = 1.0F + EnchantmentHelper.getSweepingDamageRatio(player) * damage;
		List<LivingEntity> livingEntities = player.level().getEntitiesOfClass(LivingEntity.class, box);
		DamageSource damageSource = player.damageSources().playerAttack(player);

		for (LivingEntity livingEntity : livingEntities) {
			if (livingEntity == player || livingEntity == entity || player.isAlliedTo(livingEntity) || livingEntity instanceof ArmorStand armorStand && armorStand.isMarker())
				continue;
			float correctReach = reach + livingEntity.getBbWidth() * 0.5F;
			livingEntity.hurt(damageSource, sweepingDamageRatio);
			if (player.distanceToSqr(livingEntity) < (correctReach * correctReach)) {
				MethodHandler.knockback(livingEntity, 0.4, Mth.sin(player.getYRot() * 0.017453292F), (-Mth.cos(player.getYRot() * 0.017453292F)));
			}
		}
		player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
		player.sweepAttack();
	}
	public static void tryDisableShield(LivingEntity attacker, LivingEntity target) {
		double x = target.getX() - attacker.getX();
		double z = target.getZ() - attacker.getZ();
		double x2 = attacker.getX() - target.getX();
		double z2 = attacker.getZ() - target.getZ();
		ItemStack blockingStack = MethodHandler.getBlockingItem(target).stack();
		Item blockingItem = blockingStack.getItem();
		double piercingLevel = 0;
		Item item = attacker.getMainHandItem().getItem();
		piercingLevel += ((ItemExtensions)item).getPiercingLevel();
		if (Combatify.CONFIG.piercer.get())
			piercingLevel += net.atlas.combatify.enchantment.CustomEnchantmentHelper.getPierce(attacker) * 0.1;
		boolean bl = attacker.getMainHandItem().canDisableShield(blockingStack, target, attacker) || piercingLevel > 0;
		ItemExtensions shieldItem = (ItemExtensions) blockingItem;
		if (bl && shieldItem.getBlockingType().canBeDisabled()) {
			if (piercingLevel > 0)
				((LivingEntityExtensions) target).setPiercingNegation(piercingLevel);
			float damage = Combatify.CONFIG.shieldDisableTime.get().floatValue() + (float) net.atlas.combatify.enchantment.CustomEnchantmentHelper.getChopping(attacker) * Combatify.CONFIG.cleavingDisableTime.get().floatValue();
			if(Combatify.CONFIG.defender.get())
				damage -= (float) (CustomEnchantmentHelper.getDefense(target) * Combatify.CONFIG.defenderDisableReduction.get());
			if(target instanceof Player player)
				MethodHandler.disableShield(player, damage, blockingItem);
		}
		if(shieldItem.getBlockingType().isToolBlocker()) return;
		MethodHandler.knockback(target, 0.5, x2, z2);
		MethodHandler.knockback(attacker, 0.5, x, z);
	}
	public static void disableShield(Player player, float damage, Item item) {
		player.getCooldowns().addCooldown(item, (int)(damage * 20.0F));
		if (item instanceof TieredShieldItem)
			for (TieredShieldItem tieredShieldItem : Combatify.shields)
				if (item != tieredShieldItem)
					player.getCooldowns().addCooldown(tieredShieldItem, (int)(damage * 20.0F));
		player.stopUsingItem();
		player.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + player.level().random.nextFloat() * 0.4F);
	}
	public static float getFatigueForTime(int f) {
		if (f < 60) {
			return 0.5F;
		} else {
			return f >= 200 ? 10.5F : 0.5F + 10.0F * (float)(f - 60) / 140.0F;
		}
	}
	public static void knockback(LivingEntity entity, double strength, double x, double z) {
		LivingKnockBackEvent event = ForgeHooks.onLivingKnockBack(entity, (float)strength, x, z);
		if (!event.isCanceled()) {
			strength = event.getStrength();
			x = event.getRatioX();
			z = event.getRatioZ();
			double knockbackRes = entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
			ItemStack blockingItem = getBlockingItem(entity).stack();
			if (!blockingItem.isEmpty()) {
				BlockingType blockingType = ((ItemExtensions) blockingItem.getItem()).getBlockingType();
				if (!blockingType.defaultKbMechanics())
					knockbackRes = Math.max(knockbackRes, blockingType.getShieldKnockbackResistanceValue(blockingItem));
				else
					knockbackRes = Math.min(1.0, knockbackRes + blockingType.getShieldKnockbackResistanceValue(blockingItem));
			}

			strength *= 1.0 - knockbackRes;
			if (!(strength <= 0.0F)) {
				entity.hasImpulse = true;
				Vec3 delta = entity.getDeltaMovement();
				Vec3 diff = (new Vec3(x, 0.0, z)).normalize().scale(strength);
				entity.setDeltaMovement(delta.x / 2.0 - diff.x, entity.onGround() ? Math.min(0.4, strength * 0.75) : Math.min(0.4, delta.y + strength * 0.5), delta.z / 2.0 - diff.z);
			}
		}
	}
	public static void projectileKnockback(LivingEntity entity, double strength, double x, double z) {
		LivingKnockBackEvent event = ForgeHooks.onLivingKnockBack(entity, (float)strength, x, z);
		if (!event.isCanceled()) {
			strength = event.getStrength();
			x = event.getRatioX();
			z = event.getRatioZ();
			double knockbackRes = entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
			ItemStack blockingItem = getBlockingItem(entity).stack();
			if (!blockingItem.isEmpty()) {
				BlockingType blockingType = ((ItemExtensions) blockingItem.getItem()).getBlockingType();
				if (!blockingType.defaultKbMechanics())
					knockbackRes = Math.max(knockbackRes, blockingType.getShieldKnockbackResistanceValue(blockingItem));
				else
					knockbackRes = Math.min(1.0, knockbackRes + blockingType.getShieldKnockbackResistanceValue(blockingItem));
			}

			strength *= 1.0 - knockbackRes;
			if (!(strength <= 0.0F)) {
				entity.hasImpulse = true;
				Vec3 delta = entity.getDeltaMovement();
				Vec3 diff = (new Vec3(x, 0.0, z)).normalize().scale(strength);
				entity.setDeltaMovement(delta.x / 2.0 - diff.x, Math.min(0.4, strength * 0.75), delta.z / 2.0 - diff.z);
			}
		}
	}
	public static HitResult pickCollisions(Entity entity, double reach) {
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
		if (instance.getType() == HitResult.Type.BLOCK) {
			double reach = MethodHandler.getCurrentAttackReachWithoutChargedReach(player) + (!player.isCrouching() ? getChargedReach(player.getItemInHand(InteractionHand.MAIN_HAND)) + 0.25 : 0.25);
			EntityHitResult rayTraceResult = rayTraceEntity(player, 1.0F, reach);
			Entity entity = rayTraceResult != null ? rayTraceResult.getEntity() : null;
			if (entity != null) {
				double dist = player.getEyePosition().distanceToSqr(MethodHandler.getNearestPointTo(entity.getBoundingBox(), player.getEyePosition()));
				reach *= reach;
				if (dist > reach)
					return instance;
				double distanceTo = player.distanceTo(rayTraceResult.getEntity());
				HitResult newResult = pickCollisions(player, distanceTo);
				if (newResult.getType() != HitResult.Type.MISS)
					return instance;
				return rayTraceResult;
			} else {
				return instance;
			}
		}
		return instance;
	}

	public static FakeUseItem getBlockingItem(LivingEntity entity) {
		if (entity.isUsingItem() && !entity.getUseItem().isEmpty()) {
			if (entity.getUseItem().getUseAnimation() == UseAnim.BLOCK) {
				return new FakeUseItem(entity.getUseItem(), entity.getUsedItemHand(), true);
			}
		} else if (((entity.onGround() && entity.isCrouching()) || entity.isPassenger()) && (((LivingEntityExtensions)entity).combatify$hasEnabledShieldOnCrouch() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA))) {
			for (InteractionHand hand : InteractionHand.values()) {
				ItemStack stack = entity.getItemInHand(hand);
				boolean stillRequiresCharge = Combatify.CONFIG.shieldOnlyWhenCharged.get() && entity instanceof Player player && player.getAttackStrengthScale(1.0F) < Combatify.CONFIG.shieldChargePercentage.get() / 100F && ((ItemExtensions) stack.getItem()).getBlockingType().requireFullCharge();
				boolean canUse = entity instanceof Player player && ((ItemExtensions) stack.getItem()).getBlockingType().canUse(player.level(), player, hand);
				if (canUse && !stillRequiresCharge && !stack.isEmpty() && stack.getUseAnimation() == UseAnim.BLOCK && !isItemOnCooldown(entity, stack) && ((ItemExtensions) stack.getItem()).getBlockingType().canCrouchBlock()) {
					return new FakeUseItem(stack, hand, false);
				}
			}
		}

		return new FakeUseItem(ItemStack.EMPTY, null, true);
	}
	public static boolean isItemOnCooldown(LivingEntity entity, ItemStack var1) {
		return entity instanceof Player player && player.getCooldowns().isOnCooldown(var1.getItem());
	}
	public static double getChargedReach(ItemStack itemInHand) {
		return ((ItemExtensions)itemInHand.getItem()).getChargedAttackBonus();
	}
	public static double getCurrentAttackReach(Player player, float baseTime) {
		double chargedBonus = 0;
		float strengthScale = player.getAttackStrengthScale(baseTime);
		if (strengthScale > 1.95F && !player.isCrouching()) chargedBonus = getChargedReach(player.getItemInHand(InteractionHand.MAIN_HAND));
		return getCurrentAttackReachWithoutChargedReach(player) + chargedBonus;
	}
	public static double getCurrentAttackReachWithoutChargedReach(Player player) {
		@Nullable final var attackRange = player.getAttribute(ForgeMod.ENTITY_REACH.get());
		double baseAttackRange = Combatify.CONFIG.attackReach.get() ? 2.5 : 3;
		return (attackRange != null) ? attackRange.getValue() : baseAttackRange;
	}
	public static double getSquaredCurrentAttackReach(Player player, float baseTime) {
		final var attackRange = getCurrentAttackReach(player,baseTime);
		return attackRange * attackRange;
	}
	public static HitResult pickResult(Player player, Entity camera) {
		double d = getCurrentAttackReach(player, 0.0F) + 2;
		HitResult hitResult = camera.pick(d, 1, false);
		Vec3 eyePosition = camera.getEyePosition(1.0F);
		Vec3 viewVector = camera.getViewVector(1.0F);
		boolean bl = false;
		double e = d;
		if (d > getCurrentAttackReach(player, 0.0F)) {
			bl = true;
		}

		e *= e;
		if (hitResult != null) {
			e = hitResult.getLocation().distanceToSqr(eyePosition);
		}
		Vec3 vec32 = eyePosition.add(viewVector.x * d, viewVector.y * d, viewVector.z * d);
		AABB aABB = camera.getBoundingBox().expandTowards(viewVector.scale(d)).inflate(1.0, 1.0, 1.0);
		EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(camera, eyePosition, vec32, aABB, (entityx) ->
			!entityx.isSpectator() && entityx.isPickable(), e);
		if (entityHitResult != null) {
			Vec3 vec33 = entityHitResult.getLocation();
			double h = eyePosition.distanceToSqr(vec33);
			if (bl && h > getSquaredCurrentAttackReach(player, 0.0F)) {
				hitResult = BlockHitResult.miss(vec33, Direction.getNearest(viewVector.x, viewVector.y, viewVector.z), BlockPos.containing(vec33));
			} else if (h < e || hitResult == null) {
				hitResult = entityHitResult;
			}
		}
		hitResult = redirectResult(player, hitResult);
		return hitResult;
	}
	public static void voidReturnLogic(ThrownTrident trident, EntityDataAccessor<Byte> ID_LOYALTY) {
		int j = trident.getEntityData().get(ID_LOYALTY);
		if(trident.getY() <= -65 && j > 0) {
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
	public static float getNewDamageAfterMagicAbsorb(LivingEntity entity, DamageSource source, float amount, double piercingLevel) {
		if (!source.is(DamageTypeTags.BYPASSES_EFFECTS) && piercingLevel < 1) {
			if (entity.hasEffect(MobEffects.DAMAGE_RESISTANCE) && !source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
				int i = entity.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1;
				int j = 5 - i;
				float f = (float) (amount * j * (1 + piercingLevel));
				float g = amount;
				amount = Math.max(f / 5.0F, (float) (amount * piercingLevel));
				float h = g - amount;
				if (h > 0.0F && h < 3.4028235E37F) {
					if (entity instanceof ServerPlayer serverPlayer)
						serverPlayer.awardStat(Stats.DAMAGE_RESISTED, Math.round(h * 10.0F));
					if (source.getEntity() instanceof ServerPlayer serverPlayer)
						serverPlayer.awardStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(h * 10.0F));
				}
			}

			if (amount <= 0.0F)
				return 0.0F;
			else if (!source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
				int i = EnchantmentHelper.getDamageProtection(entity.getArmorSlots(), source);
				if (i > 0)
					amount = CombatRules.getDamageAfterMagicAbsorb(amount, i - Math.round(i * piercingLevel));
			}
		}
		return amount;
	}
}
