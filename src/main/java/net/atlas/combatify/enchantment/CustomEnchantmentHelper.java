package net.atlas.combatify.enchantment;

import com.google.common.util.concurrent.AtomicDouble;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.component.CustomEnchantmentEffectComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import org.apache.commons.lang3.mutable.MutableFloat;

import java.util.List;

import static net.minecraft.world.item.enchantment.Enchantment.damageContext;

public class CustomEnchantmentHelper {
	public static double getBreach(LivingEntity entity) {
		AtomicDouble atomicDouble = new AtomicDouble(0);
		EnchantmentHelper.runIterationOnEquipment(entity, (holder, i, enchantedItemInUse) -> {
			if (holder.is(Enchantments.BREACH) && holder.value().matchingSlot(enchantedItemInUse.inSlot())) atomicDouble.addAndGet(i * Combatify.CONFIG.breachArmorPiercing());
		});
		return atomicDouble.doubleValue();
	}
	public static double getBreach(ItemStack stack) {
		AtomicDouble atomicDouble = new AtomicDouble(0);
		EnchantmentHelper.runIterationOnItem(stack, (holder, i) -> {
			if (holder.is(Enchantments.BREACH)) atomicDouble.addAndGet(i * Combatify.CONFIG.breachArmorPiercing());
		});
		return atomicDouble.doubleValue();
	}
	public static float modifyDamage(ServerLevel serverLevel, ItemStack itemStack, Entity entity, DamageSource damageSource, float baseDamage) {
		if (Combatify.CONFIG.bedrockImpaling() && entity.isInWaterOrRain()) {
			ElderGuardian guardian = EntityType.ELDER_GUARDIAN.create(serverLevel);
			return Math.max(EnchantmentHelper.modifyDamage(serverLevel, itemStack, entity, damageSource, baseDamage), EnchantmentHelper.modifyDamage(serverLevel, itemStack, guardian, damageSource, baseDamage));
		}
		return EnchantmentHelper.modifyDamage(serverLevel, itemStack, entity, damageSource, baseDamage);
	}
	public static float modifyShieldDisable(ServerLevel serverLevel, ItemStack itemStack, Entity entity, DamageSource damageSource, float timeSeconds) {
		MutableFloat mutableFloat = new MutableFloat(timeSeconds);
		EnchantmentHelper.runIterationOnItem(itemStack, (holder, i) -> holder.value().modifyDamageFilteredValue(CustomEnchantmentEffectComponents.SHIELD_DISABLE, serverLevel, i, itemStack, entity, damageSource, mutableFloat));
		return mutableFloat.floatValue();
	}
}
