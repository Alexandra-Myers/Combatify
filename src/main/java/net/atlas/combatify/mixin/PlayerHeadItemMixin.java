package net.atlas.combatify.mixin;

import net.atlas.combatify.util.MethodHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlayerHeadItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerHeadItem.class)
public class PlayerHeadItemMixin {
	@Inject(method = "verifyComponentsAfterLoad", at = @At(value = "HEAD"))
	public void editModifiers(ItemStack itemStack, CallbackInfo ci) {
		MethodHandler.updateModifiers(itemStack);
	}
}
