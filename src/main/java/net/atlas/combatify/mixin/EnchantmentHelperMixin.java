package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ArmourPiercingMode;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings("unused")
@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
	@WrapMethod(method = "modifyArmorEffectiveness")
	private static float piercerMemories(ServerLevel serverLevel, ItemStack itemStack, Entity entity, DamageSource damageSource, float protection, Operation<Float> original) {
		return switch (entity) {
			case LivingEntity ignored when Combatify.CONFIG.armourPiercingMode() == ArmourPiercingMode.APPLY_BEFORE_PERCENTAGE -> protection;
			case LivingEntity livingEntity when Combatify.CONFIG.armourPiercingMode() == ArmourPiercingMode.APPLY_AFTER_PERCENTAGE -> {
				double d = MethodHandler.getPiercingLevel(livingEntity.getItemInHand(InteractionHand.MAIN_HAND));
				d += CustomEnchantmentHelper.getArmorModifier(serverLevel, itemStack, entity, damageSource);
				d -= livingEntity.combatify$getPiercingNegation();
				livingEntity.combatify$setPiercingNegation(0);
				yield (float) Mth.clamp(protection * (1 - d), 0, 1);
			}
			case LivingEntity livingEntity when Combatify.CONFIG.armourPiercingMode() == ArmourPiercingMode.VANILLA -> {
				double d = MethodHandler.getPiercingLevel(livingEntity.getItemInHand(InteractionHand.MAIN_HAND));
				d -= livingEntity.combatify$getPiercingNegation();
				livingEntity.combatify$setPiercingNegation(0);
				yield (float) Mth.clamp(original.call(serverLevel, itemStack, entity, damageSource, protection) - d, 0, protection);
			}
			case null, default -> original.call(serverLevel, itemStack, entity, damageSource, protection);
		};
	}
}
