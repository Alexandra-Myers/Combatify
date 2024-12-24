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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class TestBlockingType extends SwordBlockingType {

	public TestBlockingType(ResourceLocation name, boolean crouchable, boolean blockHit, boolean canDisable, boolean needsFullCharge, boolean defaultKbMechanics, boolean hasDelay) {
		super(name, crouchable, blockHit, canDisable, needsFullCharge, defaultKbMechanics, hasDelay);
	}

	@Override
	public boolean fulfilBlock(ServerLevel serverLevel, LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl, float actualStrength) {
		boolean hurt = false;
		if (source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.IS_PROJECTILE)) {
			g.set(amount.get());
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
