package net.atlas.combatify.util.blocking;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.component.CustomDataComponents;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.Nullable;

import static net.atlas.combatify.util.MethodHandler.arrowDisable;

public class ShieldBlockingType extends BlockingType {

	public ShieldBlockingType(ResourceLocation name, BlockingTypeData data) {
		super(name, data);
	}

	@Override
	public Factory<? extends BlockingType> factory() {
		return Combatify.SHIELD_BLOCKING_TYPE_FACTORY;
	}

	@Override
	public void block(ServerLevel serverLevel, LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl) {
		float blockStrength = this.getShieldBlockDamageValue(blockingItem, instance.getRandom());
		boolean hurt = false;
		g.set(Math.min(blockStrength, amount.get()));
		if (!source.is(DamageTypeTags.IS_PROJECTILE) && !source.is(DamageTypeTags.IS_EXPLOSION)) {
			entity = source.getDirectEntity();
			if (entity instanceof LivingEntity livingEntity) {
				MethodHandler.hurtCurrentlyUsedShield(instance, g.get());
				hurt = true;
				MethodHandler.blockedByShield(serverLevel, instance, livingEntity, source);
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
			MethodHandler.hurtCurrentlyUsedShield(instance, g.get());
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
		float strengthIncrease = stack.getOrDefault(CustomDataComponents.BLOCKING_LEVEL, 0F);
		BannerPatternLayers bannerPatternLayers = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
		DyeColor dyeColor = stack.get(DataComponents.BASE_COLOR);
        return CustomEnchantmentHelper.modifyShieldEffectiveness(stack, random, !bannerPatternLayers.layers().isEmpty() || dyeColor != null ? 10.0F + strengthIncrease : 5.0F + strengthIncrease);
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

}
