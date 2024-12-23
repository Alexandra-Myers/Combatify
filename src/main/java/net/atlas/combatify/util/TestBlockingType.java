package net.atlas.combatify.util;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.extensions.Tier;
import net.atlas.combatify.extensions.ToolMaterialWrapper;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class TestBlockingType extends SwordBlockingType {

	public TestBlockingType(String name, boolean crouchable, boolean blockHit, boolean canDisable, boolean needsFullCharge, boolean defaultKbMechanics, boolean hasDelay) {
		super(name, crouchable, blockHit, canDisable, needsFullCharge, defaultKbMechanics, hasDelay);
	}

	@Override
	public boolean fulfilBlock(LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl, float actualStrength) {
		boolean hurt = false;
		if (source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.IS_PROJECTILE)) {
			g.set(amount.get());
		} else {
			entity = source.getDirectEntity();
			if (entity instanceof LivingEntity livingEntity) {
				instance.hurtCurrentlyUsedShield(g.get());
				hurt = true;
				MethodHandler.blockedByShield(instance, livingEntity, source);
			}
		}

		if (!hurt)
			instance.hurtCurrentlyUsedShield(g.get());
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
		if (ToolMaterialWrapper.getLevel(tier) >= 4)
			return 0.5;
		return 0.25;
	}

	@Override
	public boolean canUse(Level world, Player user, InteractionHand hand) {
		ItemStack oppositeStack = user.getItemInHand(InteractionHand.OFF_HAND);
		return hand != InteractionHand.OFF_HAND && oppositeStack.isEmpty();
	}

	@Override
	public boolean requiresSwordBlocking() {
		return false;
	}

	@Override
	public Factory<? extends BlockingType> factory() {
		return Combatify.TEST_BLOCKING_TYPE_FACTORY;
	}
}
