package net.atlas.combatify.mixin.cookey;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.Color;
import net.atlas.combatify.CookeyMod;
import net.atlas.combatify.event.OverlayReloadListener;
import net.atlas.combatify.extensions.OverlayTextureExtension;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OverlayTexture.class)
public abstract class OverlayTextureMixin implements OverlayTextureExtension, OverlayReloadListener {
    @Shadow
    @Final
    private DynamicTexture texture;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void modifyHitColor(CallbackInfo ci) {
        this.combatify$reloadOverlay();
        OverlayReloadListener.register(this);
    }

    public void combatify$onOverlayReload() {
        this.combatify$reloadOverlay();
    }


    @Unique
	private static int getColorInt(int red, int green, int blue, int alpha) {
        alpha = 255 - alpha;
        return (alpha << 24) + (blue << 16) + (green << 8) + red;
    }

    public void combatify$reloadOverlay() {
        NativeImage nativeImage = this.texture.getPixels();

        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                if (i < 8) {
                    Color color = CookeyMod.getConfig().hudRendering().damageColor().get();
                    assert nativeImage != null;
                    nativeImage.setPixelRGBA(j, i, getColorInt(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
                }
            }
        }

        RenderSystem.activeTexture(33985);
        this.texture.bind();
        nativeImage.upload(0, 0, 0, 0, 0, nativeImage.getWidth(), nativeImage.getHeight(), false, true, false, false);
        RenderSystem.activeTexture(33984);
    }
}
