package net.atlas.combatify.util.blocking;

import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.component.CustomDataComponents;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class SwordBlockingType extends PercentageBlockingType {

	public SwordBlockingType(ResourceLocation name, BlockingTypeData data) {
		super(name, data);
	}

	@Override
	public Factory<? extends BlockingType> factory() {
		return Combatify.SWORD_BLOCKING_TYPE_FACTORY;
	}

	@Override
	public boolean fulfilBlock(ServerLevel serverLevel, LivingEntity instance, @Nullable Entity entity, ItemStack blockingItem, DamageSource source, LocalFloatRef amount, LocalFloatRef f, LocalFloatRef g, LocalBooleanRef bl, float actualStrength) {
		boolean blocked = !source.is(DamageTypeTags.IS_EXPLOSION) && !source.is(DamageTypeTags.IS_PROJECTILE);
		entity = source.getDirectEntity();
		if (blocked && entity instanceof LivingEntity livingEntity)
			MethodHandler.blockedByShield(serverLevel, instance, livingEntity, source);
		return true;
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

		return Mth.clamp(CustomEnchantmentHelper.modifyShieldEffectiveness(stack, random, 0.3F + (strengthIncrease * 0.1F), false), 0, 1);
	}
	@Override
	public double getShieldKnockbackResistanceValue(ItemStack stack) {
		ConfigurableItemData configurableItemData = MethodHandler.forItem(stack.getItem());
		if (configurableItemData != null) {
			if (configurableItemData.blocker().blockStrength() != null)
				return configurableItemData.blocker().blockKbRes();
		}
		return 0.0;
	}

	@Override
	public Component getStrengthTranslationKey() {
		return Component.translatable("attribute.name.damage_reduction");
	}

}
