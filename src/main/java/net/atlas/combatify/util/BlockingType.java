package net.atlas.combatify.util;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.enchantment.DefendingEnchantment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

import static net.atlas.combatify.Combatify.EMPTY;

public abstract class BlockingType {
	private final String name;
	private boolean canBeDisabled = true;
	private boolean canCrouchBlock = true;
	private boolean isToolBlocker = false;
	private boolean canBlockHit = false;
	private boolean requiresSwordBlocking = false;
	private boolean requireFullCharge = true;
	private boolean defaultKbMechanics = true;
	public int getDefenderBonus(ItemStack stack) {
		return EnchantmentHelper.getItemEnchantmentLevel(DefendingEnchantment.DEFENDER.get(), stack);
	}
	public Float getShieldBlockBaseConfig(ItemStack stack) {
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(stack.getItem())) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(stack.getItem());
			if (configurableItemData.blockBase != null) {
				int extra = 0;
				if (Combatify.CONFIG.defender.get()) extra += getDefenderBonus(stack);
				return configurableItemData.blockBase.floatValue() + extra;
			}
		}
		return null;
	}
	public Float getShieldBlockFactorConfig(ItemStack stack) {
		if (Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(stack.getItem())) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(stack.getItem());
			if (configurableItemData.blockFactor != null) {
				return configurableItemData.blockFactor.floatValue() / 100;
			}
		}
		return null;
	}
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
	public boolean requiresSwordBlocking() {
		return requiresSwordBlocking;
	}
	public BlockingType setSwordBlocking(boolean requiresSwordBlocking) {
		this.requiresSwordBlocking = requiresSwordBlocking;
		return this;
	}

	public BlockingType(String name) {
		this.name = name;
	}
	public boolean isEmpty() {
		return this == EMPTY;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BlockingType that)) return false;
		return canBeDisabled == that.canBeDisabled && canCrouchBlock == that.canCrouchBlock && isToolBlocker() == that.isToolBlocker() && canBlockHit == that.canBlockHit && requiresSwordBlocking == that.requiresSwordBlocking && requireFullCharge == that.requireFullCharge && defaultKbMechanics == that.defaultKbMechanics && Objects.equals(getName(), that.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName(), canBeDisabled, canCrouchBlock, isToolBlocker(), canBlockHit, requiresSwordBlocking, requireFullCharge, defaultKbMechanics);
	}

	public abstract void block(LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl);
	public abstract float getShieldBlockBase(ItemStack stack);
	public abstract float getShieldBlockFactor(ItemStack stack);
	public abstract double getShieldKnockbackResistanceValue(ItemStack stack);
	public abstract @NotNull InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand);
	public abstract boolean canUse(Level world, Player user, InteractionHand hand);
	public void appendTooltips(ItemStack itemStack, Consumer<Component> appender) {
		float base = getShieldBlockBase(itemStack);
		float factor = getShieldBlockFactor(itemStack);
		double knockback = getShieldKnockbackResistanceValue(itemStack);
		if (base > 0.0) appender.accept((Component.literal("")).append(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADDITION.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(base), getProtectionComponent())).withStyle(ChatFormatting.DARK_GREEN));
		if (factor > 0.0) appender.accept((Component.literal("")).append(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.MULTIPLY_TOTAL.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format((double) factor * 100), getReductionComponent())).withStyle(ChatFormatting.DARK_GREEN));
		if (knockback > 0.0) appender.accept((Component.literal("")).append(Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADDITION.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(knockback * 10.0), Component.translatable("attribute.name.generic.knockback_resistance"))).withStyle(ChatFormatting.DARK_GREEN));
	}

	public Component getProtectionComponent() {
		return Component.translatable("attribute.name.generic.shield_strength");
	}

	public Component getReductionComponent() {
		return Component.translatable("attribute.name.generic.shield_reduction");
	}
}
