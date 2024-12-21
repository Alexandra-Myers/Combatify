package net.atlas.combatify.util;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class OldSwordBlockingType extends BlockingType {

	public OldSwordBlockingType(String name, boolean crouchable, boolean blockHit, boolean canDisable, boolean needsFullCharge, boolean defaultKbMechanics, boolean hasDelay) {
		super(name, crouchable, blockHit, canDisable, needsFullCharge, defaultKbMechanics, hasDelay);
	}

	@Override
	public BlockingType.Factory<? extends BlockingType> factory() {
		return Combatify.OLD_SWORD_BLOCKING_TYPE_FACTORY;
	}

	@Override
	public void block(LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl) {
		if (MethodHandler.getCooldowns(instance).isOnCooldown(blockingItem.getItem()))
			return;
		float actualStrength = this.getShieldBlockDamageValue(blockingItem, instance.getRandom());
		g.set(amount.get() - ((1 + amount.get()) * actualStrength));
		if (!source.is(DamageTypeTags.IS_PROJECTILE) && !source.is(DamageTypeTags.IS_EXPLOSION)) {
			entity = source.getDirectEntity();
			if (entity instanceof LivingEntity livingEntity) {
				instance.hurtCurrentlyUsedShield(g.get());
				MethodHandler.blockedByShield(instance, livingEntity, source);
			}
		}

		amount.set(amount.get() - g.get());
	}

	@Override
	public float getShieldBlockDamageValue(ItemStack stack, RandomSource random) {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(stack.getItem());
		if (configurableItemData != null) {
			if (configurableItemData.blocker().blockStrength() != null) {
				float strengthIncrease = CustomEnchantmentHelper.modifyShieldEffectiveness(stack, random, 0, true);
				return Math.min(CustomEnchantmentHelper.modifyShieldEffectiveness(stack, random, (float) (configurableItemData.blocker().blockStrength() / 100.0 + (strengthIncrease * 0.1)), false), 1);
			}
		}
		float strengthIncrease = CustomEnchantmentHelper.modifyShieldEffectiveness(stack, random, 0, true);
		return Math.min(CustomEnchantmentHelper.modifyShieldEffectiveness(stack, random, 0.5F + (strengthIncrease * 0.1F), false), 1);
	}

	@Override
	public double getShieldKnockbackResistanceValue(ItemStack stack) {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(stack.getItem());
		if (configurableItemData != null) {
			if (configurableItemData.blocker().blockStrength() != null)
				return configurableItemData.blocker().blockKbRes();
		}
		return 0.0;
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (player.isSprinting())
			player.setSprinting(false);
		player.startUsingItem(interactionHand);
		return InteractionResultHolder.consume(itemStack);
	}

	@Override
	public void appendTooltipInfo(Consumer<Component> consumer, Player player, ItemStack stack) {
		consumer.accept(CommonComponents.EMPTY);
		consumer.accept(Component.translatable("item.modifiers.use").withStyle(ChatFormatting.GRAY));
		float f = getShieldBlockDamageValue(stack, player.getRandom());
		double g = getShieldKnockbackResistanceValue(stack);
		consumer.accept(CommonComponents.space().append(
			Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL.id(),
				ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format((double) f * 100),
				getStrengthTranslationKey())).withStyle(ChatFormatting.DARK_GREEN));
		if (g > 0.0)
			consumer.accept(CommonComponents.space().append(
				Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_VALUE.id(),
					ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(g * 10.0),
					Component.translatable("attribute.name.generic.knockback_resistance"))).withStyle(ChatFormatting.DARK_GREEN));
	}

	@Override
	public Component getStrengthTranslationKey() {
		return Component.translatable("attribute.name.generic.damage_reduction");
	}

	@Override
	public boolean isToolBlocker() {
		return true;
	}

	@Override
	public boolean canUse(Level world, Player user, InteractionHand hand) {
		return true;
	}
}
