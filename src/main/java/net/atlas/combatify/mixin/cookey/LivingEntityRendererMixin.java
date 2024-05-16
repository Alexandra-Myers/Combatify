package net.atlas.combatify.mixin.cookey;

import com.mojang.blaze3d.vertex.PoseStack;
import net.atlas.combatify.CombatifyClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.atlas.combatify.config.cookey.option.BooleanOption;
import net.atlas.combatify.extensions.OverlayRendered;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {
    protected LivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Shadow
    public static int getOverlayCoords(LivingEntity livingEntity, float f) {
        return 0;
    }

    @Shadow
    protected abstract float getWhiteOverlayProgress(T livingEntity, float f);

	@Unique
	private BooleanOption showOwnNameInThirdPerson;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void injectOptions(EntityRendererProvider.Context context, M entityModel, float f, CallbackInfo ci) {
		showOwnNameInThirdPerson = CombatifyClient.getInstance().getConfig().misc().showOwnNameInThirdPerson();
	}

    @Inject(method = "shouldShowName*", at = @At("HEAD"), cancellable = true)
    public void showOwnName(T livingEntity, CallbackInfoReturnable<Boolean> cir) {
        if (livingEntity == Minecraft.getInstance().cameraEntity
                && this.showOwnNameInThirdPerson.get()) cir.setReturnValue(true);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(method = "render*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;FFFFFF)V"))
    public void renderWithOverlay(RenderLayer renderLayer, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Entity livingEntity, float f, float g, float h, float j, float k, float l) {
        if (renderLayer instanceof OverlayRendered) {
            int overlayCoords = getOverlayCoords((T) livingEntity, this.getWhiteOverlayProgress((T) livingEntity, g));
            ((OverlayRendered<T>) renderLayer).combatify$renderWithOverlay(poseStack, multiBufferSource, i, (T) livingEntity, f, g, h, j, k, l, overlayCoords);
        } else {
            renderLayer.render(poseStack, multiBufferSource, i, livingEntity, f, g, h, j, k, l);
        }
    }
}
