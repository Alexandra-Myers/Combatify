package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.extensions.PlayerExtensions;
import net.alexandra.atlas.atlas_combat.item.WeaponType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin implements ResourceManagerReloadListener/*, AutoCloseable*/ {
    @Shadow
	@Final
	private Minecraft minecraft;

    @ModifyConstant(
        method = "pick(F)V",
        require = 1, allow = 1, constant = @Constant(doubleValue = 6.0))
    private double getActualReachDistance(final double reachDistance) {
        if (this.minecraft.player != null) {
            return ((PlayerExtensions)minecraft.player).getReach(this.minecraft.player, 2.5);
        }
        return 2.5;
    }

    @ModifyConstant(method = "pick(F)V", constant = @Constant(doubleValue = 3.0))
    private double getActualAttackRange0(final double attackRange) {
        if (this.minecraft.player != null) {
            return ((PlayerExtensions)minecraft.player).getAttackRange(this.minecraft.player, 2.5);
        }
        return 2.5;
    }

    @ModifyConstant(method = "pick(F)V", constant = @Constant(doubleValue = 9.0))
    private double getActualAttackRange1(final double attackRange) {
        if (this.minecraft.player != null) {
            return ((PlayerExtensions)minecraft.player).getSquaredAttackRange(this.minecraft.player, 6.25);
        }
        return 6.25;
    }
}
