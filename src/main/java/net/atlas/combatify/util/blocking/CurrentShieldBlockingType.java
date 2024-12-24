package net.atlas.combatify.util.blocking;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static net.atlas.combatify.util.MethodHandler.arrowDisable;

public class CurrentShieldBlockingType extends ShieldBlockingType {

	public CurrentShieldBlockingType(ResourceLocation name, boolean crouchable, boolean blockHit, boolean canDisable, boolean needsFullCharge, boolean defaultKbMechanics, boolean hasDelay) {
		super(name, crouchable, blockHit, canDisable, needsFullCharge, defaultKbMechanics, hasDelay);
	}

	@Override
	public Factory<? extends BlockingType> factory() {
		return Combatify.CURRENT_SHIELD_BLOCKING_TYPE_FACTORY;
	}

	@Override
	public void block(ServerLevel serverLevel, LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl) {
		MethodHandler.hurtCurrentlyUsedShield(instance, amount.get());
		g.set(amount.get());
		amount.set(0.0f);
		if (!source.is(DamageTypeTags.IS_PROJECTILE) && source.getDirectEntity() instanceof LivingEntity livingEntity) {
			MethodHandler.blockedByShield(serverLevel, instance, livingEntity, source);
		} else if (source.is(DamageTypeTags.IS_PROJECTILE)) {
			switch (source.getDirectEntity()) {
				case Arrow arrow when Combatify.CONFIG.arrowDisableMode().satisfiesConditions(arrow) ->
					arrowDisable(instance, source, blockingItem);
				case SpectralArrow arrow when Combatify.CONFIG.arrowDisableMode().satisfiesConditions(arrow) ->
					arrowDisable(instance, source, blockingItem);
				case null, default -> {
					// Do nothing
				}
			}
		}

		bl.set(true);
	}

	@Override
	public float getShieldBlockDamageValue(ItemStack stack, RandomSource random) {
		return CustomEnchantmentHelper.modifyShieldEffectiveness(stack, random, 1);
	}

	@Override
	public double getShieldKnockbackResistanceValue(ItemStack stack) {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(stack.getItem());
		if (configurableItemData != null) {
			if (configurableItemData.blocker().blockKbRes() != null)
				return configurableItemData.blocker().blockKbRes();
		}
		return 0;
	}

    @Override
	public void appendTooltipInfo(Consumer<Component> consumer, Player player, ItemStack stack) {
		float f = getShieldBlockDamageValue(stack, player.getRandom());
		double g = getShieldKnockbackResistanceValue(stack);
		if (f < 1.0) {
			consumer.accept(CommonComponents.space().append(
				Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_VALUE.id(),
					ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(f),
					getStrengthTranslationKey())).withStyle(ChatFormatting.DARK_GREEN));
		}
		if (g > 0.0) {
			consumer.accept(CommonComponents.EMPTY);
			consumer.accept(Component.translatable("item.modifiers.use").withStyle(ChatFormatting.GRAY));
			consumer.accept(CommonComponents.space().append(
				Component.translatable("attribute.modifier.equals." + AttributeModifier.Operation.ADD_VALUE.id(),
					ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(g * 10.0),
					Component.translatable("attribute.name.knockback_resistance"))).withStyle(ChatFormatting.DARK_GREEN));
		}
	}
}
