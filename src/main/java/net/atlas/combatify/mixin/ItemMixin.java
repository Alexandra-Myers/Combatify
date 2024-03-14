package net.atlas.combatify.mixin;

import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public abstract class ItemMixin implements ItemExtensions {
	@Inject(method = "verifyComponentsAfterLoad", at = @At(value = "HEAD"))
	public void editModifiers(ItemStack itemStack, CallbackInfo ci) {
		MethodHandler.updateModifiers(itemStack);
	}

	@Override
	public Item self() {
		return Item.class.cast(this);
	}
}
