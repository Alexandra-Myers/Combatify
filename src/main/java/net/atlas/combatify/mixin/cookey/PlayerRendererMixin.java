package net.atlas.combatify.mixin.cookey;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.atlas.combatify.CookeyMod;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TieredItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

	private PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel<AbstractClientPlayer> entityModel, float f) {
		super(context, entityModel, f);
	}

    @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true)
    private static void addItemBlockPose(AbstractClientPlayer abstractClientPlayer, InteractionHand interactionHand, CallbackInfoReturnable<HumanoidModel.ArmPose> cir) {
        if (CookeyMod.getConfig().animations().enableToolBlocking().get()) {
            ItemStack currentHandStack = abstractClientPlayer.getItemInHand(interactionHand);
            ItemStack otherHandStack = interactionHand == InteractionHand.MAIN_HAND ? abstractClientPlayer.getItemInHand(InteractionHand.OFF_HAND) : abstractClientPlayer.getItemInHand(InteractionHand.MAIN_HAND);
            if (!MethodHandler.getBlockingItem(abstractClientPlayer).stack().isEmpty() && MethodHandler.getBlockingItem(abstractClientPlayer).getItem() instanceof ShieldItem) {
                if (currentHandStack.getItem() instanceof TieredItem && otherHandStack.getItem() instanceof ShieldItem) {
                    cir.setReturnValue(HumanoidModel.ArmPose.BLOCK);
                } else if (currentHandStack.getItem() instanceof ShieldItem && otherHandStack.getItem() instanceof TieredItem) {
                    cir.setReturnValue(HumanoidModel.ArmPose.EMPTY);
                }
            }
        }
    }

    @Redirect(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    public void transparentHandWhenInvisible(ModelPart instance, PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, PoseStack poseStack_, MultiBufferSource multiBufferSource, int i_, AbstractClientPlayer abstractClientPlayer, ModelPart modelPart, ModelPart modelPart2) {
        if (CookeyMod.getConfig().hudRendering().showHandWhenInvisible().get()) {
			var color = (int) (0xFFFFFFL + ((long) (CookeyMod.getConfig().hudRendering().invisibilityHandOpacity().get() * 0xFFL) << 24));
			instance.render(poseStack, multiBufferSource.getBuffer(RenderType.entityTranslucentCull(abstractClientPlayer.getSkin().texture())), i, j, abstractClientPlayer.isInvisible() ? color : 0xFFFFFFFF);
		} else instance.render(poseStack, vertexConsumer, i, j);
    }
}
