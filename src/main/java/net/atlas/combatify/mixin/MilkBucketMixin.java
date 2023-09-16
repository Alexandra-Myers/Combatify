package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MilkBucketItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MilkBucketItem.class)
public class MilkBucketMixin {

	@Inject(method = "getUseDuration", at = @At(value = "RETURN"), cancellable = true)
	public void getUseDuration(ItemStack itemStack, CallbackInfoReturnable<Integer> cir) {
		cir.setReturnValue(Combatify.CONFIG.milkBucketUseDuration.get());
	}
}
