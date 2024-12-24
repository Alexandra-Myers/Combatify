package net.atlas.combatify.util.blocking;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.component.CustomDataComponents;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.atlas.combatify.extensions.Tier;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import static net.atlas.combatify.util.MethodHandler.arrowDisable;

public class NewShieldBlockingType extends PercentageBlockingType {

	public NewShieldBlockingType(ResourceLocation name, boolean crouchable, boolean blockHit, boolean canDisable, boolean needsFullCharge, boolean defaultKbMechanics, boolean hasDelay) {
		super(name, crouchable, blockHit, canDisable, needsFullCharge, defaultKbMechanics, hasDelay);
	}

	@Override
	public boolean fulfilBlock(ServerLevel serverLevel, LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl, float actualStrength) {
		boolean hurt = false;
		if (source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.IS_PROJECTILE)) {
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
		} else {
			entity = source.getDirectEntity();
			if (entity instanceof LivingEntity livingEntity) {
				MethodHandler.hurtCurrentlyUsedShield(instance, g.get());
				hurt = true;
				MethodHandler.blockedByShield(serverLevel, instance, livingEntity, source);
			}
		}

		if (!hurt)
			MethodHandler.hurtCurrentlyUsedShield(instance, g.get());
		bl.set(true);
		return true;
	}

	@Override
	public Factory<? extends BlockingType> factory() {
		return Combatify.NEW_SHIELD_BLOCKING_TYPE_FACTORY;
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
		float strengthIncrease = stack.getOrDefault(CustomDataComponents.BLOCKING_LEVEL, 0F);
		strengthIncrease = CustomEnchantmentHelper.modifyShieldEffectiveness(stack, random, strengthIncrease, true);

		return CustomEnchantmentHelper.modifyShieldEffectiveness(stack, random, Math.min(0.5F + (strengthIncrease * 0.1F), 1), false);
	}

	@Override
	public double getShieldKnockbackResistanceValue(ItemStack stack) {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(stack.getItem());
		if (configurableItemData != null) {
			if (configurableItemData.blocker().blockKbRes() != null)
				return configurableItemData.blocker().blockKbRes();
		}
		Tier tier = stack.getItem().getConfigTier();
		if (tier.combatify$weaponLevel() >= 4)
			return 0.5;
		return 0.25;
	}
}
