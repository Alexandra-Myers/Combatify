package net.atlas.combatify.util.blocking;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.extensions.Tier;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.item.ItemStack;

import static net.atlas.combatify.util.MethodHandler.arrowDisable;

public class TestBlockingType extends SwordBlockingType {

	public TestBlockingType(ResourceLocation name, BlockingTypeData data) {
		super(name, data);
	}

	@Override
	public boolean fulfilBlock(ServerLevel serverLevel, LivingEntity instance, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef protectedDamage, LocalBooleanRef blocked, float actualStrength) {
		boolean hurt = false;
		if (source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.IS_PROJECTILE)) {
			protectedDamage.set(amount.get());
			switch (source.getDirectEntity()) {
				case Arrow arrow when Combatify.CONFIG.arrowDisableMode().satisfiesConditions(arrow) ->
					arrowDisable(instance, source, arrow, blockingItem);
				case SpectralArrow arrow when Combatify.CONFIG.arrowDisableMode().satisfiesConditions(arrow) ->
					arrowDisable(instance, source, arrow, blockingItem);
				case null, default -> {
					// Do nothing
				}
			}
		} else {
			if (source.getDirectEntity() instanceof LivingEntity livingEntity) {
				MethodHandler.hurtCurrentlyUsedShield(instance, protectedDamage.get());
				hurt = true;
				MethodHandler.blockedByShield(serverLevel, instance, livingEntity, source);
			}
		}

		if (!hurt)
			MethodHandler.hurtCurrentlyUsedShield(instance, protectedDamage.get());
		return true;
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

	@Override
	public Factory<? extends BlockingType> factory() {
		return Combatify.TEST_BLOCKING_TYPE_FACTORY;
	}
}
