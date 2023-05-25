package net.alexandra.atlas.atlas_combat.mixin;

import net.minecraft.world.item.enchantment.SweepingEdgeEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SweepingEdgeEnchantment.class)
public class SweepingEdgeEnchantmentMixin {

	@Inject(method = "getSweepingDamageRatio", at = @At(value = "RETURN"), cancellable = true)
	private static void getSweepingDamageRatio(int lvl, CallbackInfoReturnable<Float> cir) {
		cir.setReturnValue(0.5F - 0.5F / (float)(lvl + 1));
	}
}
