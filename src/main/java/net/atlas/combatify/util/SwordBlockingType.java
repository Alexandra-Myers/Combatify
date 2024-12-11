package net.atlas.combatify.util;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.extensions.ExtendedTier;
import net.atlas.combatify.extensions.ItemExtensions;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SwordBlockingType extends PercentageBlockingType {
	public SwordBlockingType(String name) {
		super(name);
	}

	@Override
	public boolean canBlock(LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl) {
		return instance.getItemInHand(InteractionHand.OFF_HAND).isEmpty();
	}

	@Override
	public boolean fulfilBlock(LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl, float actualStrength) {
		boolean blocked = !source.is(DamageTypeTags.IS_EXPLOSION) && !source.is(DamageTypeTags.IS_PROJECTILE);
		entity = source.getDirectEntity();
		if (blocked && entity instanceof LivingEntity livingEntity)
			MethodHandler.blockedByShield(instance, livingEntity, source);
		return true;
	}

	@Override
	public float getShieldBlockDamageValue(ItemStack stack) {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(stack.getItem());
		if (configurableItemData != null) {
			if (configurableItemData.blockStrength != null)
				return (float) (configurableItemData.blockStrength / 100.0);
		}
		Tier tier = ((ItemExtensions) stack.getItem()).getConfigTier();
		float strengthIncrease = ExtendedTier.getLevel(tier) / 2F - 2F;
		strengthIncrease = Math.max(strengthIncrease, -3);
		return Math.min(0.3F + (strengthIncrease * 0.1F), 1);
	}
	@Override
	public double getShieldKnockbackResistanceValue(ItemStack stack) {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(stack.getItem());
		if (configurableItemData != null) {
			if (configurableItemData.blockKbRes != null)
				return configurableItemData.blockKbRes;
		}
		return 0.0;
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack itemStack = player.getItemInHand(hand);
		if (player.isSprinting())
			player.setSprinting(false);
		player.startUsingItem(hand);
		return InteractionResultHolder.consume(itemStack);
	}

	@Override
	public boolean canUse(Level world, Player user, InteractionHand hand) {
		ItemStack oppositeStack = user.getItemInHand(InteractionHand.OFF_HAND);
		return hand != InteractionHand.OFF_HAND && oppositeStack.isEmpty() && Combatify.CONFIG.swordBlocking();
	}

	@Override
	public Component getStrengthTranslationKey() {
		return Component.translatable("attribute.name.generic.damage_reduction");
	}
}
