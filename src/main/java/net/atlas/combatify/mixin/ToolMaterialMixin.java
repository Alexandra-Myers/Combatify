package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.minecraft.world.item.ToolMaterial;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ToolMaterial.class)
public abstract class ToolMaterialMixin {
	@ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ToolMaterial;<init>(Lnet/minecraft/tags/TagKey;IFFILnet/minecraft/tags/TagKey;)V"), index = 3)
	private static float modifyDamage(float f) {
		if (f > 0 && Combatify.CONFIG.tierDamageNerf()) {
			f -= 1;
		}
		return f;
	}
}
