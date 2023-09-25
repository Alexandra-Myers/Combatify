package net.atlas.combatify.util;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.extensions.LivingEntityExtensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Nullable;

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
	public static float getFatigueForTime(int f) {
		if (f < 60) {
			return 0.5F;
		} else {
			return f >= 200 ? 10.5F : 0.5F + 10.0F * (float)(f - 60) / 140.0F;
		}
	}
	public static void knockback(LivingEntity entity, double strength, double x, double z) {
		double knockbackRes = entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
		ItemStack blockingItem = getBlockingItem(entity);
		if (!blockingItem.isEmpty()) {
			BlockingType blockingType = ((ItemExtensions)blockingItem.getItem()).getBlockingType();
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
	public static void projectileKnockback(LivingEntity entity, double strength, double x, double z) {
		double knockbackRes = entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
		ItemStack blockingItem = getBlockingItem(entity);
		if (!blockingItem.isEmpty()) {
			BlockingType blockingType = ((ItemExtensions)blockingItem.getItem()).getBlockingType();
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
	public static EntityHitResult rayTraceEntity(Player player, float partialTicks, double blockReachDistance) {
		Vec3 from = player.getEyePosition(partialTicks);
		Vec3 look = player.getViewVector(partialTicks);
		Vec3 to = from.add(look.x * blockReachDistance, look.y * blockReachDistance, look.z * blockReachDistance);

		return ProjectileUtil.getEntityHitResult(
			player.level(),
			player,
			from,
			to,
			new AABB(from, to),
			EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != null
				&& e.isPickable()
				&& e instanceof LivingEntity)
		);
	}
	public static HitResult redirectResult(Player player, HitResult instance) {
		if(instance.getType() == HitResult.Type.BLOCK) {
			BlockHitResult blockHitResult = (BlockHitResult)instance;
			BlockPos blockPos = blockHitResult.getBlockPos();
			boolean bl = !player.level().getBlockState(blockPos).canOcclude() && !player.level().getBlockState(blockPos).getBlock().hasCollision;
			EntityHitResult rayTraceResult = MethodHandler.rayTraceEntity(player, 1.0F, getCurrentAttackReach(player, 0.0F));
			if (rayTraceResult != null && bl) {
				return rayTraceResult;
			} else {
				return instance;
			}
		}
		return instance;
	}
	public static ItemStack getBlockingItem(LivingEntity entity) {
		if (entity.isUsingItem() && !entity.getUseItem().isEmpty()) {
			if (entity.getUseItem().getUseAnimation() == UseAnim.BLOCK) {
				return entity.getUseItem();
			}
		} else if ((entity.onGround() && entity.isCrouching() && ((LivingEntityExtensions) entity).hasEnabledShieldOnCrouch() || entity.isPassenger()) && ((LivingEntityExtensions)entity).hasEnabledShieldOnCrouch()) {
			for(InteractionHand hand : InteractionHand.values()) {
				ItemStack var1 = entity.getItemInHand(hand);
				Item blockingItem = var1.getItem();
				boolean bl = Combatify.CONFIG.shieldOnlyWhenCharged.get() && entity instanceof Player player && player.getAttackStrengthScale(1.0F) < Combatify.CONFIG.shieldChargePercentage.get() / 100F && ((ItemExtensions) blockingItem).getBlockingType().requireFullCharge();
				if (!var1.isEmpty() && var1.getUseAnimation() == UseAnim.BLOCK && !isItemOnCooldown(entity, var1) && ((ItemExtensions)var1.getItem()).getBlockingType().canCrouchBlock() && !bl) {
					return var1;
				}
			}
		}

		return ItemStack.EMPTY;
	}
	public static boolean isItemOnCooldown(LivingEntity entity, ItemStack var1) {
		return entity instanceof Player player && player.getCooldowns().isOnCooldown(var1.getItem());
	}
	public static double getCurrentAttackReach(Player player, float baseTime) {
		@Nullable final var attackRange = player.getAttribute(ForgeMod.ENTITY_REACH.get());
		double chargedBonus = 0;
		double baseAttackRange = Combatify.CONFIG.attackReach.get() ? 0 : 0.5;
		float strengthScale = player.getAttackStrengthScale(baseTime);
		if (strengthScale > 1.95F && !player.isCrouching()) {
			Item item = player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
			chargedBonus = ((ItemExtensions) item).getChargedAttackBonus();
		}
		return (attackRange != null) ? (baseAttackRange + attackRange.getValue() + chargedBonus) : baseAttackRange + chargedBonus;
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
			}else {
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
}
