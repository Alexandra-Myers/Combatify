package net.atlas.combat_enhanced.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.enchantment.SweepingEdgeEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SweepingEdgeEnchantment.class)
public class SweepingEdgeEnchantmentMixin {

	@ModifyReturnValue(method = "getSweepingDamageRatio", at = @At(value = "RETURN"))
	private static float getSweepingDamageRatio(float original, @Local(ordinal = 0) int lvl) {
		return 0.5F - 0.5F / (float)(lvl + 1);
	}
}
