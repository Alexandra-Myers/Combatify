package net.alexandra.atlas.atlas_combat.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.alexandra.atlas.atlas_combat.extensions.PlayerExtensions;
import net.alexandra.atlas.atlas_combat.item.WeaponType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin implements ResourceManagerReloadListener/*, AutoCloseable*/ {
    @Shadow
	@Final
	Minecraft minecraft;

    @ModifyExpressionValue(method = "pick(F)V", at = @At(value = "CONSTANT", args = "doubleValue=3.0"))
    private double getActualAttackRange0(double original) {
        if (this.minecraft.player != null) {
            return ((PlayerExtensions)minecraft.player).getAttackRange(this.minecraft.player, 2.5);
        }
        return 2.5;
    }

	@ModifyExpressionValue(method = "pick(F)V", at = @At(value = "CONSTANT", args = "doubleValue=9.0"))
    private double getActualAttackRange1(double original) {
        if (this.minecraft.player != null) {
            return ((PlayerExtensions)minecraft.player).getSquaredAttackRange(this.minecraft.player, 6.25);
        }
        return 6.25;
    }
}
