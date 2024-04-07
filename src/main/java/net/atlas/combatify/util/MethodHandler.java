package net.atlas.combatify.util;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.extensions.LivingEntityExtensions;
import net.atlas.combatify.extensions.PlayerExtensions;
import net.atlas.combatify.item.WeaponType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static net.atlas.combatify.Combatify.CONFIG;

public class MethodHandler {
	public static Vec3 getNearestPointTo(AABB box, Vec3 vec3) {
		double x = Mth.clamp(vec3.x, box.minX, box.maxX);
		double y = Mth.clamp(vec3.y, box.minY, box.maxY);
		double z = Mth.clamp(vec3.z, box.minZ, box.maxZ);

		return new Vec3(x, y, z);
	}

	public static void updateModifiers(ItemStack itemStack) {
		ItemAttributeModifiers modifier = itemStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
		modifier = ((ItemExtensions)itemStack.getItem()).modifyAttributeModifiers(modifier);
		itemStack.set(DataComponents.ATTRIBUTE_MODIFIERS, modifier);
		if (modifier != null && Combatify.ITEMS != null) {
			Item item = itemStack.getItem();
			if (Combatify.ITEMS.configuredItems.containsKey(item)) {
				ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(item);
				if (configurableItemData.type != null) {
					ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
					configurableItemData.type.addCombatAttributes(((ItemExtensions)item).getConfigTier(), builder);
					modifier.modifiers().forEach(entry -> {
						boolean bl = entry.attribute().is(Attributes.ATTACK_DAMAGE)
							|| entry.attribute().is(Attributes.ATTACK_SPEED)
							|| entry.attribute().is(Attributes.ENTITY_INTERACTION_RANGE);
						if (!bl)
							builder.add(entry.attribute(), entry.modifier(), entry.slot());
					});
					itemStack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
					modifier = itemStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, modifier);
				}
				ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
				boolean modDamage = false;
				AtomicReference<ItemAttributeModifiers.Entry> damage = new AtomicReference<>();
				boolean modSpeed = false;
				AtomicReference<ItemAttributeModifiers.Entry> speed = new AtomicReference<>();
				boolean modReach = false;
				AtomicReference<ItemAttributeModifiers.Entry> reach = new AtomicReference<>();
				boolean modDefense = false;
				AtomicReference<ItemAttributeModifiers.Entry> defense = new AtomicReference<>();
				boolean modToughness = false;
				AtomicReference<ItemAttributeModifiers.Entry> toughness = new AtomicReference<>();
				boolean modKnockbackResistance = false;
				AtomicReference<ItemAttributeModifiers.Entry> knockbackResistance = new AtomicReference<>();
				modifier.modifiers().forEach(entry -> {
					if (entry.attribute().is(Attributes.ATTACK_DAMAGE))
						damage.set(entry);
					else if (entry.attribute().is(Attributes.ENTITY_INTERACTION_RANGE))
						reach.set(entry);
					else if (entry.attribute().is(Attributes.ATTACK_SPEED))
						speed.set(entry);
					else if (entry.attribute().is(Attributes.ARMOR))
						defense.set(entry);
					else if (entry.attribute().is(Attributes.ARMOR_TOUGHNESS))
						toughness.set(entry);
					else if (entry.attribute().is(Attributes.KNOCKBACK_RESISTANCE))
						knockbackResistance.set(entry);
					else
						builder.add(entry.attribute(), entry.modifier(), entry.slot());
				});
				if (configurableItemData.damage != null) {
					modDamage = true;
					builder.add(Attributes.ATTACK_DAMAGE,
						new AttributeModifier(Item.BASE_ATTACK_DAMAGE_UUID, "Config modifier", configurableItemData.damage - (CONFIG.fistDamage() ? 1 : 2), AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND);
				}
				if (configurableItemData.speed != null) {
					modSpeed = true;
					builder.add(Attributes.ATTACK_SPEED,
						new AttributeModifier(WeaponType.BASE_ATTACK_SPEED_UUID, "Config modifier", configurableItemData.speed - CONFIG.baseHandAttackSpeed(), AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND);
				}
				if (configurableItemData.reach != null) {
					modReach = true;
					builder.add(Attributes.ENTITY_INTERACTION_RANGE,
						new AttributeModifier(WeaponType.BASE_ATTACK_REACH_UUID, "Config modifier", configurableItemData.reach - 2.5, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND);
				}
				UUID uuid = UUID.fromString("C1D3F271-8B8E-BA4A-ACE0-6020A98928B2");
				EquipmentSlotGroup slotGroup = EquipmentSlotGroup.ARMOR;
				if (itemStack.getItem() instanceof Equipable equipable)
					slotGroup = EquipmentSlotGroup.bySlot(equipable.getEquipmentSlot());
				uuid = switch (slotGroup) {
					case HEAD -> UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150");
					case CHEST -> UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E");
					case LEGS -> UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D");
					case FEET -> UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B");
					case BODY -> UUID.fromString("C1C72771-8B8E-BA4A-ACE0-81A93C8928B2");
					default -> uuid;
				};
				if (configurableItemData.defense != null) {
					modDefense = true;
					builder.add(Attributes.ARMOR,
						new AttributeModifier(uuid, "Config modifier", configurableItemData.defense, AttributeModifier.Operation.ADD_VALUE),
						slotGroup);
				}
				if (configurableItemData.toughness != null) {
					modToughness = true;
					builder.add(Attributes.ARMOR_TOUGHNESS,
						new AttributeModifier(uuid, "Config modifier", configurableItemData.toughness, AttributeModifier.Operation.ADD_VALUE),
						slotGroup);
				}
				if (configurableItemData.armourKbRes != null) {
					modKnockbackResistance = true;
					if (configurableItemData.armourKbRes > 0)
						builder.add(Attributes.KNOCKBACK_RESISTANCE,
							new AttributeModifier(uuid, "Config modifier", configurableItemData.armourKbRes, AttributeModifier.Operation.ADD_VALUE),
							slotGroup);
				}
				if (!modDamage && damage.get() != null)
					builder.add(damage.get().attribute(), damage.get().modifier(), damage.get().slot());
				if (!modSpeed && speed.get() != null)
					builder.add(speed.get().attribute(), speed.get().modifier(), speed.get().slot());
				if (!modReach && reach.get() != null)
					builder.add(reach.get().attribute(), reach.get().modifier(), reach.get().slot());
				if (!modDefense && defense.get() != null)
					builder.add(defense.get().attribute(), defense.get().modifier(), defense.get().slot());
				if (!modToughness && toughness.get() != null)
					builder.add(toughness.get().attribute(), toughness.get().modifier(), toughness.get().slot());
				if (!modKnockbackResistance && knockbackResistance.get() != null)
					builder.add(knockbackResistance.get().attribute(), knockbackResistance.get().modifier(), knockbackResistance.get().slot());
				if (modDamage || modSpeed || modReach || modDefense || modToughness || modKnockbackResistance)
					itemStack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
			}
		}
	}
	public static double calculateValue(@Nullable AttributeInstance attributeInstance, float damageBonus) {
		if(attributeInstance == null)
			return damageBonus;
		double attributeInstanceBaseValue = attributeInstance.getBaseValue();

		for(AttributeModifier attributeModifier : attributeInstance.getModifiersOrEmpty(AttributeModifier.Operation.ADD_VALUE)) {
			attributeInstanceBaseValue += attributeModifier.amount() + damageBonus;
		}

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
		ItemStack blockingItem = getBlockingItem(entity);
		boolean delay = ((ItemExtensions) blockingItem.getItem()).getBlockingType().hasDelay() && Combatify.CONFIG.shieldDelay() > 0 && blockingItem.getUseDuration() - entity.getUseItemRemainingTicks() < Combatify.CONFIG.shieldDelay();
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
		if (Combatify.CONFIG.swingThroughGrass() && instance.getType() == HitResult.Type.BLOCK) {
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
		piercingLevel += ((ItemExtensions)attacker.getMainHandItem().getItem()).getPiercingLevel();
		if (Combatify.CONFIG.piercer())
			piercingLevel += CustomEnchantmentHelper.getPierce(attacker) * 0.1;
		boolean canDisable = attacker.canDisableShield() || piercingLevel > 0;
		ItemExtensions shieldItem = (ItemExtensions) blockingItem.getItem();
		if (canDisable && shieldItem.getBlockingType().canBeDisabled()) {
			if (piercingLevel > 0)
				((LivingEntityExtensions) target).setPiercingNegation(piercingLevel);
			float damage = (float) (Combatify.CONFIG.shieldDisableTime() + (float) CustomEnchantmentHelper.getChopping(attacker) * Combatify.CONFIG.cleavingDisableTime());
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
				if (!bl && !var1.isEmpty() && var1.getUseAnimation() == UseAnim.BLOCK && !isItemOnCooldown(entity, var1) && ((ItemExtensions)var1.getItem()).getBlockingType().canCrouchBlock()) {
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
		float charge = Combatify.CONFIG.chargedAttacks() ? 1.95F : 0.9F;
		if (attackRange != null) {
			Item item = player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
			chargedBonus = ((ItemExtensions) item).getChargedAttackBonus();
			AttributeModifier modifier = new AttributeModifier(UUID.fromString("98491ef6-97b1-4584-ae82-71a8cc85cf74"), "Charged reach bonus", chargedBonus, AttributeModifier.Operation.ADD_VALUE);
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
	public static Vec3 project(Vec3 originalVec, Vec3 newVec) {
	    Vec3 normalized = newVec.normalize();
	    double d = originalVec.dot(newVec);
	    return normalized.multiply(d, d, d);
	}
}
