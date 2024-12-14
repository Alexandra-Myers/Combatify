package net.atlas.combatify.enchantment;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.component.CustomEnchantmentEffectComponents;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.apache.commons.lang3.mutable.MutableFloat;

import java.util.Optional;

public class CustomEnchantmentHelper {
	public static float getBreach(ItemStack stack, RandomSource randomSource) {
		MutableFloat mutableFloat = new MutableFloat(1);
		EnchantmentHelper.runIterationOnItem(stack, (holder, i) -> holder.value().getEffects(EnchantmentEffectComponents.ARMOR_EFFECTIVENESS).forEach(enchantmentValueEffectConditionalEffect -> mutableFloat.setValue(enchantmentValueEffectConditionalEffect.effect().process(i, randomSource, mutableFloat.floatValue()))));
		return -(mutableFloat.floatValue() - 1);
	}
	public static float getArmorModifier(ServerLevel serverLevel, ItemStack itemStack, Entity entity, DamageSource damageSource) {
		MutableFloat mutableFloat = new MutableFloat(1);
		EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> holder.value().modifyArmorEffectivness(serverLevel, i, itemStack, entity, damageSource, mutableFloat));
		return -(mutableFloat.floatValue() - 1);
	}
	public static float modifyDamage(ServerLevel serverLevel, ItemStack itemStack, Entity entity, DamageSource damageSource, float baseDamage, Operation<Float> original) {
		label1: {
			if (Combatify.CONFIG.bedrockImpaling() && entity.isInWaterOrRain()) {
				Optional<HolderSet.Named<EntityType<?>>> sensitiveToImpaling = BuiltInRegistries.ENTITY_TYPE.getTag(EntityTypeTags.SENSITIVE_TO_IMPALING);
				if (sensitiveToImpaling.isEmpty() || sensitiveToImpaling.get().size() <= 0)
					break label1;
				Entity applicable = sensitiveToImpaling.get().get(0).value().create(serverLevel);
				return Math.max(original.call(serverLevel, itemStack, entity, damageSource, baseDamage), original.call(serverLevel, itemStack, applicable, damageSource, baseDamage));
			}
		}
		return original.call(serverLevel, itemStack, entity, damageSource, baseDamage);
	}
	public static float modifyShieldDisable(ServerLevel serverLevel, ItemStack itemStack, Entity entity, DamageSource damageSource, float timeSeconds) {
		MutableFloat mutableFloat = new MutableFloat(timeSeconds);
		EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> holder.value().modifyDamageFilteredValue(CustomEnchantmentEffectComponents.SHIELD_DISABLE, serverLevel, i, itemStack, entity, damageSource, mutableFloat));
		return mutableFloat.floatValue();
	}
}
