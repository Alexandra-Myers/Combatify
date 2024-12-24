package net.atlas.combatify.util.blocking;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class PercentageBlockingType extends BlockingType {
	public PercentageBlockingType(ResourceLocation name, BlockingTypeData data) {
		super(name, data);
	}

	@Override
	public void block(ServerLevel serverLevel, LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl) {
		float actualStrength = this.getShieldBlockDamageValue(blockingItem, instance.getRandom());
		g.set(amount.get() * actualStrength);
		if (!fulfilBlock(serverLevel, instance, entity, blockingItem, source, amount, f, g, bl, actualStrength)) return;

		amount.set(amount.get() - g.get());
	}

	public abstract boolean fulfilBlock(ServerLevel serverLevel, LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl, float actualStrength);

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
					Component.translatable("attribute.name.knockback_resistance"))).withStyle(ChatFormatting.DARK_GREEN));
	}

	@Override
	public Component getStrengthTranslationKey() {
		return Component.translatable("attribute.name.shield_reduction");
	}
}
