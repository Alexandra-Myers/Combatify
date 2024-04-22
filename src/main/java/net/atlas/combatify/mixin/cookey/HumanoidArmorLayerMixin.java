package net.atlas.combatify.mixin.cookey;

import com.mojang.blaze3d.vertex.PoseStack;
import net.atlas.combatify.CombatifyClient;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.LivingEntity;
import net.atlas.combatify.annotation.mixin.Incompatible;
import net.atlas.combatify.config.cookey.option.BooleanOption;
import net.atlas.combatify.extensions.OverlayRendered;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(HumanoidArmorLayer.class)
@Incompatible("optifabric")
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> implements OverlayRendered<T> {
    @Shadow
    public abstract void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l);
    int overlayCoords;
    BooleanOption showDamageTintOnArmor = CombatifyClient.getInstance().getConfig().hudRendering().showDamageTintOnArmor();

    @ModifyArg(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/HumanoidModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"), index = 3)
    public int modifyOverlayCoords(int previousCoords) {
        boolean show = this.showDamageTintOnArmor.get();
        return show ? this.overlayCoords : previousCoords;
    }

    @Override
    public void renderWithOverlay(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T entity, float f, float g, float h, float j, float k, float l, int overlayCoords) {
        this.overlayCoords = overlayCoords;
        this.render(poseStack, multiBufferSource, i, entity, f, g, h, j, k, l);
    }
}