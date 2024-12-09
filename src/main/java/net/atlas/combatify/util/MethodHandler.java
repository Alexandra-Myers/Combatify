package net.atlas.combatify.util;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.extensions.LivingEntityExtensions;
import net.atlas.combatify.item.LongSwordItem;
import net.atlas.combatify.item.TieredShieldItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class MethodHandler {
	public static float getAttackStrengthScale(LivingEntity entity, float baseTime) {
		if (entity instanceof Player player)
			return player.getAttackStrengthScale(baseTime);
		return 2.0f;
	}
	public static Vec3 getNearestPointTo(AABB box, Vec3 vec3) {
		double x = Mth.clamp(vec3.x, box.minX, box.maxX);
		double y = Mth.clamp(vec3.y, box.minY, box.maxY);
		double z = Mth.clamp(vec3.z, box.minZ, box.maxZ);

		return new Vec3(x, y, z);
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
		if (f < 60 || !Combatify.CONFIG.bowFatigue())
			return 0.5F;
		else
			return f >= 200 ? 10.5F : 0.5F + 10.0F * (float)(f - 60) / 140.0F;
	}
	public static void knockback(LivingEntity entity, double strength, double x, double z) {
		if (!Combatify.CONFIG.ctsKB()) {
			entity.knockback(strength, x, z);
			return;
		}
		double knockbackRes = entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
		ItemStack blockingItem = getBlockingItem(entity).stack();
		boolean delay = ((ItemExtensions) blockingItem.getItem()).getBlockingType().hasDelay() && Combatify.CONFIG.shieldDelay() > 0 && blockingItem.getUseDuration(entity) - entity.getUseItemRemainingTicks() < Combatify.CONFIG.shieldDelay();
		if (!blockingItem.isEmpty() && !delay) {
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
		ItemStack blockingItem = getBlockingItem(entity).stack();
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
	private static List<BlockPos> clip(Level level, ClipContext clipContext) {
		List<BlockPos> blockPosList = new ArrayList<>();
		traverseBlocks(clipContext.getFrom(), clipContext.getTo(), clipContext, (clipContextx, blockPos) -> {
			BlockState blockState = level.getBlockState(blockPos);
            if (blockState.canOcclude() && blockState.getBlock().hasCollision)
				blockPosList.add(blockPos);
		});
		return blockPosList;
	}
	private static <C> void traverseBlocks(Vec3 vec3, Vec3 vec32, C object, BiConsumer<C, BlockPos> biConsumer) {
		if (!vec3.equals(vec32)) {
			double d = Mth.lerp(-1.0E-7, vec3.x, vec32.x);
			double e = Mth.lerp(-1.0E-7, vec3.y, vec32.y);
			double f = Mth.lerp(-1.0E-7, vec3.z, vec32.z);
			int g = Mth.floor(d);
			int h = Mth.floor(e);
			int i = Mth.floor(f);
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(g, h, i);
			biConsumer.accept(object, mutableBlockPos);
			double j = Mth.lerp(-1.0E-7, vec32.x, vec3.x) - d;
			double k = Mth.lerp(-1.0E-7, vec32.y, vec3.y) - e;
			double l = Mth.lerp(-1.0E-7, vec32.z, vec3.z) - f;
			int m = Mth.sign(j);
			int n = Mth.sign(k);
			int o = Mth.sign(l);
			double p = m == 0 ? Double.MAX_VALUE : (double)m / j;
			double q = n == 0 ? Double.MAX_VALUE : (double)n / k;
			double r = o == 0 ? Double.MAX_VALUE : (double)o / l;
			double s = p * (m > 0 ? 1.0 - Mth.frac(d) : Mth.frac(d));
			double t = q * (n > 0 ? 1.0 - Mth.frac(e) : Mth.frac(e));
			double u = r * (o > 0 ? 1.0 - Mth.frac(f) : Mth.frac(f));

			do {
				if (s < t) {
					if (s < u) {
						g += m;
						s += p;
					} else {
						i += o;
						u += r;
					}
				} else if (t < u) {
					h += n;
					t += q;
				} else {
					i += o;
					u += r;
				}

				biConsumer.accept(object, mutableBlockPos.set(g, h, i));
			} while(s <= 1.0 || t <= 1.0 || u <= 1.0);
		}
	}
	public static List<BlockPos> pickFromPos(Entity entity, double reach) {
		Vec3 viewVector = entity.getViewVector(1);
		Vec3 pos = entity.getEyePosition(1);
		Vec3 endPos = pos.add(viewVector.scale(reach));
		return clip(entity.level(), new ClipContext(pos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity));
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
				List<BlockPos> blockPosList = pickFromPos(player, distanceTo);
				if (!blockPosList.isEmpty())
					return instance;
				return rayTraceResult;
			} else {
				return instance;
			}
		}
		return instance;
	}
	public static void disableShield(LivingEntity attacker, LivingEntity target, DamageSource damageSource, ItemStack blockingItem) {
		ItemStack attackingItem = attacker.getMainHandItem();
		double piercingLevel = ((ItemExtensions)attackingItem.getItem()).getPiercingLevel();
		piercingLevel += CustomEnchantmentHelper.getBreach(attacker);
		if (!(Combatify.CONFIG.armorPiercingDisablesShields() || attacker.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof LongSwordItem))
			piercingLevel = 0;
		boolean canDisable = attacker.canDisableShield() || piercingLevel > 0;
		ItemExtensions shieldItem = (ItemExtensions) blockingItem.getItem();
		if (canDisable && shieldItem.getBlockingType().canBeDisabled()) {
			if (piercingLevel > 0)
				((LivingEntityExtensions) attacker).setPiercingNegation(piercingLevel);
			float damage = Combatify.CONFIG.shieldDisableTime().floatValue();
			if (attacker.level() instanceof ServerLevel serverLevel) {
				damage = CustomEnchantmentHelper.modifyShieldDisable(serverLevel, attackingItem, target, damageSource, damage);
				damage = CustomEnchantmentHelper.modifyShieldDisable(serverLevel, blockingItem, target, damageSource, damage);
			}
			if (target instanceof Player player)
				disableShield(player, damage, blockingItem.getItem());
		}
	}
	public static void arrowDisable(LivingEntity target, DamageSource damageSource, ItemStack blockingItem) {
		float damage = Combatify.CONFIG.shieldDisableTime().floatValue();
		if (target.level() instanceof ServerLevel serverLevel) {
			damage = CustomEnchantmentHelper.modifyShieldDisable(serverLevel, blockingItem, target, damageSource, damage);
		}
		if (target instanceof Player player)
			disableShield(player, damage, blockingItem.getItem());
	}
	public static void disableShield(Player player, float damage, Item item) {
		player.getCooldowns().addCooldown(item, (int)(damage * 20.0F));
		if (item instanceof TieredShieldItem)
			for (TieredShieldItem tieredShieldItem : Combatify.shields)
				if (item != tieredShieldItem)
					player.getCooldowns().addCooldown(tieredShieldItem, (int)(damage * 20.0F));
		player.stopUsingItem();
		player.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + player.level().random.nextFloat() * 0.4F);
		player.level().broadcastEntityEvent(player, (byte)30);
	}
	public static FakeUseItem getBlockingItem(LivingEntity entity) {
		if (entity.isUsingItem() && !entity.getUseItem().isEmpty()) {
			if (entity.getUseItem().getUseAnimation() == UseAnim.BLOCK) {
				return new FakeUseItem(entity.getUseItem(), entity.getUsedItemHand());
			}
		} else if (((entity.onGround() && entity.isCrouching()) || entity.isPassenger()) && ((LivingEntityExtensions) entity).hasEnabledShieldOnCrouch()) {
			for (InteractionHand hand : InteractionHand.values()) {
				ItemStack stack = entity.getItemInHand(hand);
				Item blockingItem = stack.getItem();
				boolean bl = Combatify.CONFIG.shieldOnlyWhenCharged() && entity instanceof Player player && player.getAttackStrengthScale(1.0F) < Combatify.CONFIG.shieldChargePercentage() / 100F && ((ItemExtensions) blockingItem).getBlockingType().requireFullCharge();
				if (!bl && !stack.isEmpty() && stack.getUseAnimation() == UseAnim.BLOCK && !isItemOnCooldown(entity, stack) && ((ItemExtensions)stack.getItem()).getBlockingType().canCrouchBlock()) {
					return new FakeUseItem(stack, hand);
				}
			}
		}

		return new FakeUseItem(ItemStack.EMPTY, null);
	}
	public static boolean isItemOnCooldown(LivingEntity entity, ItemStack var1) {
		return entity instanceof Player player && player.getCooldowns().isOnCooldown(var1.getItem());
	}
	public static double getCurrentAttackReach(Player player, float baseTime) {
		@Nullable final var attackRange = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
		double chargedBonus = 0;
		double baseAttackRange = Combatify.CONFIG.attackReach() ? 2.5 : 3;
		float strengthScale = player.getAttackStrengthScale(baseTime);
		float charge = Combatify.CONFIG.chargedAttacks() ? 1.95F : 0.9F;
		if (attackRange != null) {
			Item item = player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
			chargedBonus = ((ItemExtensions) item).getChargedAttackBonus();
			AttributeModifier modifier = new AttributeModifier(Combatify.CHARGED_REACH_ID, chargedBonus, AttributeModifier.Operation.ADD_VALUE);
			if (strengthScale > charge && !player.isCrouching() && Combatify.CONFIG.chargedReach())
				attackRange.addOrUpdateTransientModifier(modifier);
			else
				attackRange.removeModifier(modifier);
		}
		if (strengthScale < charge || player.isCrouching() || !Combatify.CONFIG.chargedReach()) {
			chargedBonus = 0;
		}
		return (attackRange != null) ? attackRange.getValue() : baseAttackRange + chargedBonus;
	}
	public static void voidReturnLogic(ThrownTrident trident, EntityDataAccessor<Byte> ID_LOYALTY) {
		int j = trident.getEntityData().get(ID_LOYALTY);
		if (Combatify.CONFIG.tridentVoidReturn() && trident.getY() <= -65 && j > 0) {
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

	public static void blockedByShield(LivingEntity target, LivingEntity attacker, DamageSource damageSource) {
		double x = target.getX() - attacker.getX();
		double z = target.getZ() - attacker.getZ();
		double x2 = attacker.getX() - target.getX();
		double z2 = attacker.getZ() - target.getZ();
		ItemStack blockingItem = MethodHandler.getBlockingItem(target).stack();
		if (((ItemExtensions)blockingItem.getItem()).getBlockingType().isToolBlocker()) {
			MethodHandler.disableShield(attacker, target, damageSource, blockingItem);
			return;
		}
		MethodHandler.knockback(target, 0.5, x2, z2);
		MethodHandler.knockback(attacker, 0.5, x, z);
		MethodHandler.disableShield(attacker, target, damageSource, blockingItem);
	}
}
