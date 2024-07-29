package net.atlas.combatify.util;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.attributes.CustomAttributes;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.extensions.LivingEntityExtensions;
import net.atlas.combatify.extensions.PlayerExtensions;
import net.atlas.combatify.item.LongSwordItem;
import net.atlas.combatify.item.WeaponType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
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
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static net.atlas.combatify.Combatify.CONFIG;

public class MethodHandler {
	public static Vec3 getNearestPointTo(AABB box, Vec3 vec3) {
		double x = Mth.clamp(vec3.x, box.minX, box.maxX);
		double y = Mth.clamp(vec3.y, box.minY, box.maxY);
		double z = Mth.clamp(vec3.z, box.minZ, box.maxZ);

		return new Vec3(x, y, z);
	}

	@SuppressWarnings("ALL")
	public static void updateModifiers(DataComponentMap.Builder builder, Item item) {
		ItemAttributeModifiers modifier = item.components().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
		ItemAttributeModifiers def = item.getDefaultAttributeModifiers();
		if (modifier == ItemAttributeModifiers.EMPTY && def != ItemAttributeModifiers.EMPTY)
			modifier = def;
		modifier = ((ItemExtensions)item).modifyAttributeModifiers(modifier);
		if (modifier != null && Combatify.ITEMS != null) {
			if (Combatify.ITEMS.configuredItems.containsKey(item)) {
				ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(item);
				if (configurableItemData.type != null) {
					ItemAttributeModifiers.Builder itemAttributeBuilder = ItemAttributeModifiers.builder();
					configurableItemData.type.addCombatAttributes(((ItemExtensions)item).getConfigTier(), itemAttributeBuilder);
					modifier.modifiers().forEach(entry -> {
						boolean bl = entry.attribute().is(Attributes.ATTACK_DAMAGE)
							|| entry.attribute().is(Attributes.ATTACK_SPEED)
							|| entry.attribute().is(Attributes.ENTITY_INTERACTION_RANGE);
						if (!bl)
							itemAttributeBuilder.add(entry.attribute(), entry.modifier(), entry.slot());
					});
					modifier = itemAttributeBuilder.build();
				}
				ItemAttributeModifiers.Builder itemAttributeBuilder = ItemAttributeModifiers.builder();
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
				def.modifiers().forEach(entry -> {
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
						itemAttributeBuilder.add(entry.attribute(), entry.modifier(), entry.slot());
				});
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
						itemAttributeBuilder.add(entry.attribute(), entry.modifier(), entry.slot());
				});
				if (configurableItemData.damage != null) {
					modDamage = true;
					itemAttributeBuilder.add(Attributes.ATTACK_DAMAGE,
						new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, configurableItemData.damage - (CONFIG.fistDamage() ? 1 : 2), AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND);
				}
				if (!modDamage && damage.get() != null)
					itemAttributeBuilder.add(damage.get().attribute(), damage.get().modifier(), damage.get().slot());
				if (configurableItemData.speed != null) {
					modSpeed = true;
					itemAttributeBuilder.add(Attributes.ATTACK_SPEED,
						new AttributeModifier(WeaponType.BASE_ATTACK_SPEED_CTS_ID, configurableItemData.speed - CONFIG.baseHandAttackSpeed(), AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND);
				}
				if (!modSpeed && speed.get() != null)
					itemAttributeBuilder.add(speed.get().attribute(), speed.get().modifier(), speed.get().slot());
				if (configurableItemData.reach != null) {
					modReach = true;
					itemAttributeBuilder.add(Attributes.ENTITY_INTERACTION_RANGE,
						new AttributeModifier(WeaponType.BASE_ATTACK_REACH_ID, configurableItemData.reach - 2.5, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND);
				}
				if (!modReach && reach.get() != null)
					itemAttributeBuilder.add(reach.get().attribute(), reach.get().modifier(), reach.get().slot());
				ResourceLocation resourceLocation = ResourceLocation.withDefaultNamespace("armor.any");
				EquipmentSlotGroup slotGroup = EquipmentSlotGroup.ARMOR;
				if (item instanceof Equipable equipable)
					slotGroup = EquipmentSlotGroup.bySlot(equipable.getEquipmentSlot());
				resourceLocation = switch (slotGroup) {
					case HEAD -> ResourceLocation.withDefaultNamespace("armor.helmet");
					case CHEST -> ResourceLocation.withDefaultNamespace("armor.chestplate");
					case LEGS -> ResourceLocation.withDefaultNamespace("armor.leggings");
					case FEET -> ResourceLocation.withDefaultNamespace("armor.boots");
					case BODY -> ResourceLocation.withDefaultNamespace("armor.body");
					default -> resourceLocation;
				};
				if (configurableItemData.defense != null) {
					modDefense = true;
					itemAttributeBuilder.add(Attributes.ARMOR,
						new AttributeModifier(resourceLocation, configurableItemData.defense, AttributeModifier.Operation.ADD_VALUE),
						slotGroup);
				}
				if (!modDefense && defense.get() != null)
					itemAttributeBuilder.add(defense.get().attribute(), defense.get().modifier(), defense.get().slot());
				if (configurableItemData.toughness != null) {
					modToughness = true;
					itemAttributeBuilder.add(Attributes.ARMOR_TOUGHNESS,
						new AttributeModifier(resourceLocation, configurableItemData.toughness, AttributeModifier.Operation.ADD_VALUE),
						slotGroup);
				}
				if (!modToughness && toughness.get() != null)
					itemAttributeBuilder.add(toughness.get().attribute(), toughness.get().modifier(), toughness.get().slot());
				if (configurableItemData.armourKbRes != null) {
					modKnockbackResistance = true;
					if (configurableItemData.armourKbRes > 0)
						itemAttributeBuilder.add(Attributes.KNOCKBACK_RESISTANCE,
							new AttributeModifier(resourceLocation, configurableItemData.armourKbRes, AttributeModifier.Operation.ADD_VALUE),
							slotGroup);
				}
				if (!modKnockbackResistance && knockbackResistance.get() != null)
					itemAttributeBuilder.add(knockbackResistance.get().attribute(), knockbackResistance.get().modifier(), knockbackResistance.get().slot());
				if (modDamage || modSpeed || modReach || modDefense || modToughness || modKnockbackResistance)
					modifier = itemAttributeBuilder.build();
			}
		}
		builder.set(DataComponents.ATTRIBUTE_MODIFIERS, modifier);
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
	public static void disableShield(LivingEntity attacker, LivingEntity target, ItemStack blockingItem) {
		double piercingLevel = ((ItemExtensions)attacker.getMainHandItem().getItem()).getPiercingLevel();
		piercingLevel += CustomEnchantmentHelper.getBreach(attacker);
		if (!(Combatify.CONFIG.armorPiercingDisablesShields() || attacker.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof LongSwordItem))
			piercingLevel = 0;
		boolean canDisable = attacker.canDisableShield() || piercingLevel > 0;
		ItemExtensions shieldItem = (ItemExtensions) blockingItem.getItem();
		if (canDisable && shieldItem.getBlockingType().canBeDisabled()) {
			if (piercingLevel > 0)
				((LivingEntityExtensions) attacker).setPiercingNegation(piercingLevel);
			float damage = (float) (Combatify.CONFIG.shieldDisableTime() + (float) attacker.getAttributeValue(CustomAttributes.SHIELD_DISABLE_TIME));
			damage -= (float) (target.getAttributeValue(CustomAttributes.SHIELD_DISABLE_REDUCTION));
			if (target instanceof PlayerExtensions player)
				player.ctsShieldDisable(damage, blockingItem.getItem());
		}
	}
	public static void arrowDisable(LivingEntity target, ItemStack blockingItem) {
		float damage = Combatify.CONFIG.shieldDisableTime().floatValue();
		damage -= (float) (target.getAttributeValue(CustomAttributes.SHIELD_DISABLE_REDUCTION));
		if (target instanceof PlayerExtensions player)
			player.ctsShieldDisable(damage, blockingItem.getItem());
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
}
