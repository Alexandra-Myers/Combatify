package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ArmourPiercingMode;
import net.atlas.combatify.config.JSImpl;
import net.atlas.combatify.enchantment.CustomEnchantmentHelper;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CombatRules.class)
public class CombatRulesMixin {
	@Inject(method = "getDamageAfterAbsorb", at = @At("HEAD"))
	private static void changeArmourAndProtection(LivingEntity attacked, float f, DamageSource damageSource, float g, float h, CallbackInfoReturnable<Float> cir, @Local(ordinal = 1, argsOnly = true) LocalFloatRef armour, @Local(ordinal = 2, argsOnly = true) LocalFloatRef toughness) {
		if (Combatify.CONFIG.armourPiercingMode() == ArmourPiercingMode.APPLY_BEFORE_PERCENTAGE && damageSource.getEntity() instanceof LivingEntity livingEntity) {
			ItemStack held = livingEntity.getItemInHand(InteractionHand.MAIN_HAND);
			double d = MethodHandler.getPiercingLevel(held);
			if (!(attacked.level() instanceof ServerLevel serverLevel)) d += CustomEnchantmentHelper.getBreach(held, livingEntity.getRandom());
			else d += CustomEnchantmentHelper.getArmorModifier(serverLevel, held, attacked, damageSource);
			d -= livingEntity.combatify$getPiercingNegation();
			livingEntity.combatify$setPiercingNegation(0);
			armour.set((float) (armour.get() * (1 - d)));
			toughness.set((float) (toughness.get() * (1 - d)));
		}
	}
	@ModifyReturnValue(method = "getDamageAfterAbsorb", at = @At("RETURN"))
	private static float changeArmourCalcs(float original, @Local(ordinal = 0, argsOnly = true) float amount, @Local(ordinal = 0, argsOnly = true) DamageSource damageSource, @Local(ordinal = 1, argsOnly = true) float armour, @Local(ordinal = 2, argsOnly = true) float toughness) {
		if (Combatify.ITEMS.armourCalcs.get().execFunc("shouldOverrideArmorProtection()")) {
			float result = (float) Combatify.ITEMS.armourCalcs.get().execGetterFunc(original, "armorProtection(damage, armor, armorToughness)", new JSImpl.Reference("damage", amount), new JSImpl.Reference("armor", armour), new JSImpl.Reference("armorToughness", toughness));
			ItemStack itemStack = damageSource.getWeaponItem();
			if (itemStack != null && damageSource.getEntity().level() instanceof ServerLevel serverLevel) original = 1 - Mth.clamp(EnchantmentHelper.modifyArmorEffectiveness(serverLevel, itemStack, damageSource.getEntity(), damageSource, result), 0.0F, 1.0F);
			else original = 1 - result;
			original *= amount;
		}
		if (Combatify.CONFIG.enableDebugLogging())
			Combatify.LOGGER.info("Damage: " + amount + " Result: " + original);
		return original;
	}
	@ModifyReturnValue(method = "getDamageAfterMagicAbsorb", at = @At("RETURN"))
	private static float changeEnchant(float original, @Local(ordinal = 0, argsOnly = true) float amount, @Local(ordinal = 1, argsOnly = true) float enchantLevel) {
		if (Combatify.ITEMS.armourCalcs.get().execFunc("shouldOverrideEnchantmentProtection()")) original = (float) Combatify.ITEMS.armourCalcs.get().execGetterFunc(original, "enchantmentProtection(damage, protectionLevel)", new JSImpl.Reference("damage", amount), new JSImpl.Reference("protectionLevel", enchantLevel));
		return original;
	}
}
