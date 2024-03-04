package net.atlas.combatify.mixin;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.Properties.class)
public class PropertiesMixin {
	@Inject(method = "buildComponents", at = @At(value = "HEAD"))
	public void updateAttributeModifiers(CallbackInfoReturnable<DataComponentMap> cir) {

	}
}
