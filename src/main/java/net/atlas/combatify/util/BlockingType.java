package net.atlas.combatify.util;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.enchantment.DefendingEnchantment;
import net.atlas.combatify.extensions.LivingEntityExtensions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BlockingType {
	private final ResourceLocation name;
	public static final BlockingType SWORD = new BlockingType("sword") {
		@Override
		public void block(LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl) {
			if(instance.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
				boolean blocked = !source.is(DamageTypeTags.IS_EXPLOSION) && !source.is(DamageTypeTags.IS_PROJECTILE);
				if (source.is(DamageTypeTags.IS_EXPLOSION)) {
					g.set(Math.min(amount.get(), 10));
				} else if (blocked) {
					((LivingEntityExtensions)instance).setIsParryTicker(0);
					((LivingEntityExtensions)instance).setIsParry(true);
					float actualStrength = this.getShieldBlockDamageValue(blockingItem);
					g.set(amount.get() * actualStrength);
					entity = source.getDirectEntity();
					if (entity instanceof LivingEntity) {
						instance.blockUsingShield((LivingEntity) entity);
					}
					bl.set(true);
				}

				amount.set(amount.get() - g.get());
			}
		}

		@Override
		public float getShieldBlockDamageValue(ItemStack itemStack) {
			Tier var2 = itemStack.getItem() instanceof TieredItem tieredItem ? tieredItem.getTier() : Tiers.NETHERITE;
			float strengthIncrease = var2.getAttackDamageBonus() <= 1.0F ? -1F : 0.0F;
			strengthIncrease += Combatify.CONFIG.swordProtectionEfficacy();
			strengthIncrease = Math.max(strengthIncrease, -3);
			if(Combatify.CONFIG.defender()) {
				strengthIncrease += EnchantmentHelper.getItemEnchantmentLevel(DefendingEnchantment.DEFENDER, itemStack);
			}
			return Math.min(0.5F + (strengthIncrease * 0.125F), 1);
		}
		@Override
		public double getShieldKnockbackResistanceValue(ItemStack itemStack) {
			return 0.0;
		}

		@Override
		public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
			ItemStack itemStack = user.getItemInHand(hand);
			if(Combatify.CONFIG.swordBlocking() && hand != InteractionHand.OFF_HAND) {
				ItemStack oppositeStack = user.getItemInHand(InteractionHand.OFF_HAND);
				if(oppositeStack.isEmpty()) {
					if(user.isSprinting()) {
						user.setSprinting(false);
					}
					user.startUsingItem(hand);
					return InteractionResultHolder.consume(itemStack);
				}
			}
			return InteractionResultHolder.pass(itemStack);
		}
	}.setToolBlocker(true).setDisablement(false).setCrouchable(false).setBlockHit(true).setRequireFullCharge(false).setPercentage(true);
	public static final BlockingType SHIELD = new BlockingType("shield") {
		@Override
		public void block(LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl) {
			if (instance instanceof Player player && player.getCooldowns().isOnCooldown(blockingItem.getItem()))
				return;
			float blockStrength = this.getShieldBlockDamageValue(blockingItem);
			g.set(Math.min(blockStrength, amount.get()));
			if (!source.is(DamageTypeTags.IS_PROJECTILE) && !source.is(DamageTypeTags.IS_EXPLOSION)) {
				entity = source.getDirectEntity();
				if (entity instanceof LivingEntity) {
					instance.blockUsingShield((LivingEntity) entity);
				}
			} else {
				g.set(amount.get());
			}

			instance.hurtCurrentlyUsedShield(g.get());
			amount.set(amount.get() - g.get());
			bl.set(true);
		}

		@Override
		public float getShieldBlockDamageValue(ItemStack itemStack) {
			float f = itemStack.getTagElement("BlockEntityTag") != null ? 10.0F : 5.0F;
			if(Combatify.CONFIG.defender()) {
				f += EnchantmentHelper.getItemEnchantmentLevel(DefendingEnchantment.DEFENDER, itemStack);
			}
			return f;
		}

		@Override
		public double getShieldKnockbackResistanceValue(ItemStack itemStack) {
			return itemStack.getTagElement("BlockEntityTag") != null ? 0.8 : 0.5;
		}

		@Override
		public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand interactionHand) {
			ItemStack itemStack = player.getItemInHand(interactionHand);
			player.startUsingItem(interactionHand);
			return InteractionResultHolder.consume(itemStack);
		}
	};
	public static final BlockingType NEW_SHIELD = new BlockingType("new_shield") {
		@Override
		public void block(LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl) {
			if (source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.IS_PROJECTILE)) {
				g.set(amount.get());
			} else {
				float actualStrength = this.getShieldBlockDamageValue(blockingItem);
				g.set(amount.get() * actualStrength);
				entity = source.getDirectEntity();
				if (entity instanceof LivingEntity) {
					instance.blockUsingShield((LivingEntity) entity);
				}
				bl.set(true);
			}

			amount.set(amount.get() - g.get());
		}

		@Override
		public float getShieldBlockDamageValue(ItemStack stack) {
			return 0.5F;
		}

		@Override
		public double getShieldKnockbackResistanceValue(ItemStack stack) {
			return 0.5;
		}

		@Override
		public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand interactionHand) {
			ItemStack itemStack = player.getItemInHand(interactionHand);
			player.startUsingItem(interactionHand);
			return InteractionResultHolder.consume(itemStack);
		}
	}.setKbMechanics(false).setPercentage(true);
	public static final BlockingType EMPTY = new BlockingType("empty") {
		@Override
		public void block(LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl) {

		}

		@Override
		public float getShieldBlockDamageValue(ItemStack stack) {
			return 0;
		}

		@Override
		public double getShieldKnockbackResistanceValue(ItemStack stack) {
			return 0;
		}

		@Override
		public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
			return InteractionResultHolder.pass(user.getItemInHand(hand));
		}
	}.setDisablement(false).setCrouchable(false).setRequireFullCharge(false).setKbMechanics(false);
	private boolean canBeDisabled = true;
	private boolean canCrouchBlock = true;
	private boolean isToolBlocker = false;
	private boolean percentage = false;
	private boolean canBlockHit = false;
	private boolean requireFullCharge = true;
	private boolean defaultKbMechanics = true;
	public boolean canCrouchBlock() {
		return canCrouchBlock;
	}
	public BlockingType setCrouchable(boolean crouchable) {
		canCrouchBlock = crouchable;
		return this;
	}

	public boolean canBlockHit() {
		return canBlockHit;
	}
	public BlockingType setBlockHit(boolean blockHit) {
		canBlockHit = blockHit;
		return this;
	}
	public boolean isPercentage() {
		return percentage;
	}
	public BlockingType setPercentage(boolean percentage) {
		this.percentage = percentage;
		return this;
	}
	public boolean isToolBlocker() {
		return isToolBlocker;
	}
	public BlockingType setToolBlocker(boolean isTool) {
		isToolBlocker = isTool;
		return this;
	}
	public boolean canBeDisabled() {
		return canBeDisabled;
	}
	public BlockingType setDisablement(boolean canDisable) {
		canBeDisabled = canDisable;
		return this;
	}
	public boolean requireFullCharge() {
		return requireFullCharge;
	}
	public BlockingType setRequireFullCharge(boolean needsFullCharge) {
		requireFullCharge = needsFullCharge;
		return this;
	}
	public boolean defaultKbMechanics() {
		return defaultKbMechanics;
	}
	public BlockingType setKbMechanics(boolean defaultKbMechanics) {
		this.defaultKbMechanics = defaultKbMechanics;
		return this;
	}

	public BlockingType(String name) {
		this.name = new ResourceLocation(name);
		Combatify.registeredTypes.put(this.name, this);
	}
	public boolean isEmpty() {
		return this == EMPTY;
	}
	public static void init() {

	}

	public ResourceLocation getName() {
		return name;
	}
	public abstract void block(LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl);
	public abstract float getShieldBlockDamageValue(ItemStack stack);
	public abstract double getShieldKnockbackResistanceValue(ItemStack stack);
	public abstract @NotNull InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand);
}
