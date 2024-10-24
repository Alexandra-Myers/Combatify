package net.atlas.combatify.util;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.extensions.ExtendedTier;
import net.atlas.combatify.extensions.ItemExtensions;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import org.jetbrains.annotations.Nullable;

import static net.atlas.combatify.util.MethodHandler.arrowDisable;

public class NewShieldBlockingType extends PercentageBlockingType {
	public NewShieldBlockingType(String name) {
		super(name);
	}

	@Override
	public boolean canBlock(LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl) {
		return !(instance instanceof Player player) || !player.getCooldowns().isOnCooldown(blockingItem.getItem());
	}

	@Override
	public boolean fulfilBlock(LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl, float actualStrength) {
		boolean hurt = false;
		if (source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.IS_PROJECTILE)) {
			g.set(amount.get());
			switch (source.getDirectEntity()) {
				case Arrow arrow when Combatify.CONFIG.arrowDisableMode().satisfiesConditions(arrow) ->
					arrowDisable(instance, blockingItem);
				case SpectralArrow arrow when Combatify.CONFIG.arrowDisableMode().satisfiesConditions(arrow) ->
					arrowDisable(instance, blockingItem);
				case null, default -> {
					// Do nothing
				}
			}
		} else {
			entity = source.getDirectEntity();
			if (entity instanceof LivingEntity livingEntity) {
				instance.hurtCurrentlyUsedShield(g.get());
				hurt = true;
				instance.blockUsingShield(livingEntity);
			}
		}

		if (!hurt)
			instance.hurtCurrentlyUsedShield(g.get());
		bl.set(true);
		return true;
	}

	@Override
	public float getShieldBlockDamageValue(ItemStack stack) {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(stack.getItem())) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(stack.getItem());
			if (configurableItemData.blockStrength != null)
				return (float) (configurableItemData.blockStrength / 100.0);
		}
		Tier tier = ((ItemExtensions) stack.getItem()).getConfigTier();
		float strengthIncrease = ExtendedTier.getLevel(tier) / 2F - 2F;
		strengthIncrease = Mth.ceil(strengthIncrease);

		return Math.min(0.5F + (strengthIncrease * 0.1F), 1);
	}

	@Override
	public double getShieldKnockbackResistanceValue(ItemStack stack) {
		if(Combatify.ITEMS != null && Combatify.ITEMS.configuredItems.containsKey(stack.getItem())) {
			ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(stack.getItem());
			if (configurableItemData.blockKbRes != null)
				return configurableItemData.blockKbRes;
		}
		Tier tier = ((ItemExtensions) stack.getItem()).getConfigTier();
		if (ExtendedTier.getLevel(tier) >= 4)
			return 0.5;
		return 0.25;
	}
}
