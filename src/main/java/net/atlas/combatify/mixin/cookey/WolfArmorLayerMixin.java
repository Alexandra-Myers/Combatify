package net.atlas.combatify.mixin.cookey;

import com.mojang.blaze3d.vertex.PoseStack;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.config.cookey.option.BooleanOption;
import net.atlas.combatify.extensions.OverlayRendered;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.WolfArmorLayer;
import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WolfArmorLayer.class)
public abstract class WolfArmorLayerMixin implements OverlayRendered<Wolf> {
	@Shadow
	public abstract void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Wolf wolf, float f, float g, float h, float j, float k, float l);

	@Unique
	private int overlayCoords;

	@Unique
	private BooleanOption showDamageTintOnArmor;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void injectOptions(RenderLayerParent<Wolf, WolfModel<Wolf>> renderLayerParent, EntityModelSet entityModelSet, CallbackInfo ci) {
		showDamageTintOnArmor = CombatifyClient.getInstance().getConfig().hudRendering().showDamageTintOnArmor();
	}

    @ModifyArg(method = "render*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/WolfModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"), index = 3)
    public int modifyOverlayCoords(int previousCoords) {
        boolean show = this.showDamageTintOnArmor.get();
        return show ? this.overlayCoords : previousCoords;
    }

    @Override
    public void combatify$renderWithOverlay(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Wolf entity, float f, float g, float h, float j, float k, float l, int overlayCoords) {
        this.overlayCoords = overlayCoords;
        this.render(poseStack, multiBufferSource, i, entity, f, g, h, j, k, l);
    }
}
