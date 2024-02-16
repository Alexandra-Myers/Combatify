package net.atlas.combatify.util;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.extensions.LivingEntityExtensions;
import net.atlas.combatify.extensions.PlayerExtensions;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

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
			attributeInstanceBaseValue += attributeModifier.getAmount() + damageBonus;
		}

		double attributeInstanceFinalValue = attributeInstanceBaseValue;

		for(AttributeModifier attributeModifier2 : attributeInstance.getModifiersOrEmpty(AttributeModifier.Operation.MULTIPLY_BASE)) {
			attributeInstanceFinalValue += attributeInstanceBaseValue * attributeModifier2.getAmount();
		}

		for(AttributeModifier attributeModifier2 : attributeInstance.getModifiersOrEmpty(AttributeModifier.Operation.MULTIPLY_TOTAL)) {
			attributeInstanceFinalValue *= 1.0 + attributeModifier2.getAmount();
		}

		return attributeInstance.getAttribute().value().sanitizeValue(attributeInstanceFinalValue);
	}
	public static float getFatigueForTime(int f) {
		if (f < 60 || !Combatify.CONFIG.bowFatigue()) {
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
	public static HitResult pickFromPos(Entity entity, double reach, double mod) {
		reach = Math.max(reach - mod, 0);
		Vec3 viewVector = entity.getViewVector(1);
		Vec3 pos = entity.getEyePosition(1).add(viewVector.scale(mod));
		Vec3 endPos = pos.add(viewVector.x * reach, viewVector.y * reach,viewVector.z * reach);
		return entity.level().clip(new ClipContext(pos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity));
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
			Level level = player.level();
			boolean bl = !level.getBlockState(blockPos).canOcclude() && !level.getBlockState(blockPos).getBlock().hasCollision;
			EntityHitResult rayTraceResult = MethodHandler.rayTraceEntity(player, 1.0F, getCurrentAttackReach(player, 0.0F));
			if (rayTraceResult != null && bl) {
				double reach = player.distanceTo(rayTraceResult.getEntity());
				double d = 0;
				HitResult check;
				while (d <= Math.ceil(reach)) {
					check = pickFromPos(player, reach, d);
					if (check.getType() == HitResult.Type.BLOCK) {
						BlockState state = level.getBlockState(((BlockHitResult) check).getBlockPos());
						bl = !state.canOcclude() && !state.getBlock().hasCollision;
						if (!bl)
							return instance;
					}
					d += 0.0002;
				}
				return rayTraceResult;
			} else {
				return instance;
			}
		}
		return instance;
	}
	public static void disableShield(LivingEntity attacker, LivingEntity target, ItemStack blockingItem) {
		double piercingLevel = 0;
		Item item = attacker.getMainHandItem().getItem();
		piercingLevel += ((ItemExtensions)item).getPiercingLevel();
		if (Combatify.CONFIG.piercer())
			piercingLevel += net.atlas.combatify.enchantment.CustomEnchantmentHelper.getPierce(attacker) * 0.1;
		boolean canDisable = item instanceof AxeItem || piercingLevel > 0;
		ItemExtensions shieldItem = (ItemExtensions) blockingItem.getItem();
		if (canDisable && shieldItem.getBlockingType().canBeDisabled()) {
			if (piercingLevel > 0)
				((LivingEntityExtensions) target).setPiercingNegation(piercingLevel);
			float damage = (float) (Combatify.CONFIG.shieldDisableTime() + (float) net.atlas.combatify.enchantment.CustomEnchantmentHelper.getChopping(attacker) * Combatify.CONFIG.cleavingDisableTime());
			if(Combatify.CONFIG.defender())
				damage -= (float) (CustomEnchantmentHelper.getDefense(target) * Combatify.CONFIG.defenderDisableReduction());
			if(target instanceof PlayerExtensions player)
				player.ctsShieldDisable(damage, blockingItem.getItem());
		}
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
				boolean bl = Combatify.CONFIG.shieldOnlyWhenCharged() && entity instanceof Player player && player.getAttackStrengthScale(1.0F) < Combatify.CONFIG.shieldChargePercentage() / 100F && ((ItemExtensions) blockingItem).getBlockingType().requireFullCharge();
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
		@Nullable final var attackRange = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
		double chargedBonus = 0;
		double baseAttackRange = Combatify.CONFIG.attackReach() ? 2.5 : 3;
		float strengthScale = player.getAttackStrengthScale(baseTime);
		float charge = Combatify.CONFIG.chargedAttacks() ? 1.95F : 0.95F;
		if (attackRange != null) {
			Item item = player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
			chargedBonus = ((ItemExtensions) item).getChargedAttackBonus();
			AttributeModifier modifier = new AttributeModifier(UUID.fromString("98491ef6-97b1-4584-ae82-71a8cc85cf74"), "Charged reach bonus", chargedBonus, AttributeModifier.Operation.ADDITION);
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
