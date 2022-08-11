package net.alexandra.atlas.atlas_combat.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandMixin {
	@Redirect(method = "tick", at = @At(value = "INVOKE",target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"))
	public float modifyArmPos4(LocalPlayer instance, float v) {
		return (float) instance.getAttackStrengthScale(v)/2;
	}
}
