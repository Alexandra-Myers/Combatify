package net.atlas.combatify.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlayerHeadItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerHeadItem.class)
public class PlayerHeadItemMixin extends ItemMixin {
	@Inject(method = "verifyComponentsAfterLoad", at = @At(value = "HEAD"))
	public void editModifiers1(ItemStack itemStack, CallbackInfo ci) {
		editModifiers(itemStack);
	}
}
