package net.atlas.combatify.util;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.atlas.combatify.util.MethodHandler.arrowDisable;

public class ShieldBlockingType extends BlockingType {

	public ShieldBlockingType(String name, boolean crouchable, boolean blockHit, boolean canDisable, boolean needsFullCharge, boolean defaultKbMechanics, boolean hasDelay) {
		super(name, crouchable, blockHit, canDisable, needsFullCharge, defaultKbMechanics, hasDelay);
	}

	@Override
	public Factory<? extends BlockingType> factory() {
		return Combatify.SHIELD_BLOCKING_TYPE_FACTORY;
	}

	@Override
	public void block(LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl) {
		if (MethodHandler.getCooldowns(instance).isOnCooldown(blockingItem.getItem()))
			return;
		float blockStrength = this.getShieldBlockDamageValue(blockingItem, instance.getRandom());
		boolean hurt = false;
		g.set(Math.min(blockStrength, amount.get()));
		if (!source.is(DamageTypeTags.IS_PROJECTILE) && !source.is(DamageTypeTags.IS_EXPLOSION)) {
			entity = source.getDirectEntity();
			if (entity instanceof LivingEntity livingEntity) {
				instance.hurtCurrentlyUsedShield(g.get());
				hurt = true;
				MethodHandler.blockedByShield(instance, livingEntity, source);
			}
		} else {
			g.set(amount.get());
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

		if (!hurt)
			instance.hurtCurrentlyUsedShield(g.get());
		amount.set(amount.get() - g.get());
		bl.set(true);
	}

	@Override
	public float getShieldBlockDamageValue(ItemStack stack, RandomSource random) {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(stack.getItem());
		if (configurableItemData != null) {
			if (configurableItemData.blocker().blockStrength() != null) {
				return CustomEnchantmentHelper.modifyShieldEffectiveness(stack, random, configurableItemData.blocker().blockStrength().floatValue());
			}
		}
		BannerPatternLayers bannerPatternLayers = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
		DyeColor dyeColor = stack.get(DataComponents.BASE_COLOR);
        return CustomEnchantmentHelper.modifyShieldEffectiveness(stack, random, !bannerPatternLayers.layers().isEmpty() || dyeColor != null ? 10.0F : 5.0F);
	}

	@Override
	public double getShieldKnockbackResistanceValue(ItemStack stack) {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(stack.getItem());
		if (configurableItemData != null) {
			if (configurableItemData.blocker().blockKbRes() != null) {
				return configurableItemData.blocker().blockKbRes();
			}
		}
		BannerPatternLayers bannerPatternLayers = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
		DyeColor dyeColor = stack.get(DataComponents.BASE_COLOR);
		return !bannerPatternLayers.layers().isEmpty() || dyeColor != null ? 0.8 : 0.5;
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		player.startUsingItem(interactionHand);
		return InteractionResultHolder.consume(itemStack);
	}

	@Override
	public boolean canUse(Level world, Player user, InteractionHand hand) {
		return true;
	}
}
