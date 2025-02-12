package net.atlas.combatify.enchantment;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.component.CustomEnchantmentEffectComponents;
import net.atlas.combatify.util.blocking.ComponentModifier.DataSet;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
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
	public static DataSet modifyShieldEffectiveness(ItemStack itemStack, RandomSource randomSource, DataSet value) {
		MutableFloat base = new MutableFloat(value.addValue());
		MutableFloat factor = new MutableFloat(value.multiplyValue());
		EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> holder.value().getEffects(CustomEnchantmentEffectComponents.SHIELD_EFFECTIVENESS).forEach(protectionBaseFactor -> {
			base.setValue(protectionBaseFactor.base().process(i, randomSource, base.floatValue()));
			factor.setValue(protectionBaseFactor.factor().process(i, randomSource, factor.floatValue()));
		}));
		return new DataSet(base.floatValue(), factor.floatValue());
	}
	public static void applyPostBlockedEffects(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker, DamageSource damageSource) {
		EnchantmentHelper.runIterationOnEquipment(attacker, (holder, enchantmentLevel, enchantedItemInUse) -> holder.value().getEffects(CustomEnchantmentEffectComponents.POST_BLOCK_EFFECTS)
			.forEach(targetedConditionalEffect -> {
				if (targetedConditionalEffect.matches(Enchantment.damageContext(serverLevel, enchantmentLevel, target, damageSource)) && (targetedConditionalEffect.enchanted() == EnchantmentTarget.ATTACKER || targetedConditionalEffect.enchanted() == EnchantmentTarget.DAMAGING_ENTITY)) {
					LivingEntity applicable = switch (targetedConditionalEffect.affected()) {
						case ATTACKER, DAMAGING_ENTITY -> attacker;
						case VICTIM -> target;
					};
					targetedConditionalEffect.effect().doEffect(serverLevel, enchantedItemInUse, target, damageSource, enchantmentLevel, applicable, applicable.position());
				}
			}));
		EnchantmentHelper.runIterationOnEquipment(target, (holder, enchantmentLevel, enchantedItemInUse) -> holder.value().getEffects(CustomEnchantmentEffectComponents.POST_BLOCK_EFFECTS)
			.forEach(targetedConditionalEffect -> {
				if (targetedConditionalEffect.matches(Enchantment.damageContext(serverLevel, enchantmentLevel, target, damageSource)) && targetedConditionalEffect.enchanted() == EnchantmentTarget.VICTIM) {
					LivingEntity applicable = switch (targetedConditionalEffect.affected()) {
						case ATTACKER, DAMAGING_ENTITY -> attacker;
						case VICTIM -> target;
					};
					targetedConditionalEffect.effect().doEffect(serverLevel, enchantedItemInUse, attacker, damageSource, enchantmentLevel, applicable, applicable.position());
				}
			}));
	}
	public static float modifyDamage(ServerLevel serverLevel, ItemStack itemStack, Entity entity, DamageSource damageSource, float baseDamage, Operation<Float> original) {
		label1: {
			if (Combatify.CONFIG.bedrockImpaling() && entity.isInWaterOrRain()) {
				Optional<HolderSet.Named<EntityType<?>>> sensitiveToImpaling = BuiltInRegistries.ENTITY_TYPE.get(EntityTypeTags.SENSITIVE_TO_IMPALING);
				if (sensitiveToImpaling.isEmpty() || sensitiveToImpaling.get().size() <= 0)
					break label1;
				int i = 0;
				Entity applicable;
				do {
					applicable = sensitiveToImpaling.get().get(i).value().create(serverLevel, EntitySpawnReason.COMMAND);
					i++;
				} while (applicable == null && sensitiveToImpaling.get().size() > i);
				float result = Math.max(original.call(serverLevel, itemStack, entity, damageSource, baseDamage), original.call(serverLevel, itemStack, applicable, damageSource, baseDamage));
                assert applicable != null;
                applicable.discard();
				return result;
			}
		}
		return original.call(serverLevel, itemStack, entity, damageSource, baseDamage);
	}
	public static float modifyShieldDisable(ServerLevel serverLevel, ItemStack itemStack, Entity target, Entity sourceEntity, DamageSource damageSource, float timeSeconds) {
		MutableFloat disableTime = new MutableFloat(timeSeconds);
		if (itemStack != null) EnchantmentHelper.runIterationOnItem(itemStack, (holder, enchantmentLevel) -> modifyDisableTime(EnchantmentTarget.ATTACKER, holder, serverLevel, enchantmentLevel, target, damageSource, disableTime));
		else if (sourceEntity instanceof LivingEntity livingEntity) EnchantmentHelper.runIterationOnEquipment(livingEntity, (holder, enchantmentLevel, enchantedItemInUse) -> modifyDisableTime(EnchantmentTarget.ATTACKER, holder, serverLevel, enchantmentLevel, target, damageSource, disableTime));
		if (target instanceof LivingEntity livingEntity) EnchantmentHelper.runIterationOnEquipment(livingEntity, (holder, enchantmentLevel, enchantedItemInUse) -> modifyDisableTime(EnchantmentTarget.VICTIM, holder, serverLevel, enchantmentLevel, target, damageSource, disableTime));
		return disableTime.floatValue();
	}
	public static void modifyDisableTime(EnchantmentTarget enchantmentTarget, Holder<Enchantment> holder, ServerLevel serverLevel, int enchantmentLevel, Entity target, DamageSource damageSource, MutableFloat disableTime) {
		for (TargetedConditionalEffect<EnchantmentValueEffect> effect : holder.value().getEffects(CustomEnchantmentEffectComponents.SHIELD_DISABLE)) {
			if (effect.enchanted() == enchantmentTarget && effect.matches(Enchantment.damageContext(serverLevel, enchantmentLevel, target, damageSource))) disableTime.setValue(effect.effect().process(enchantmentLevel, target.getRandom(), disableTime.floatValue()));
		}
	}
}
