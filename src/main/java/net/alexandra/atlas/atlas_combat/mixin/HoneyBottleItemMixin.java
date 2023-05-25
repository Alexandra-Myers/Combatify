package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.minecraft.world.item.HoneyBottleItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoneyBottleItem.class)
public class HoneyBottleItemMixin {

	@Unique
	public final int maxUseDuration = AtlasCombat.CONFIG.honeyBottleUseDuration();

	@Inject(method = "getUseDuration", at = @At(value = "RETURN"), cancellable = true)
	public void getUseDuration(ItemStack itemStack, CallbackInfoReturnable<Integer> cir) {
		cir.setReturnValue(maxUseDuration);
	}

}
