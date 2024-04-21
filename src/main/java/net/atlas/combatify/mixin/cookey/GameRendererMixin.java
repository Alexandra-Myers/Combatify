package net.atlas.combatify.mixin.cookey;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.atlas.combatify.CombatifyClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.atlas.combatify.config.cookey.option.BooleanOption;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
	Minecraft minecraft;

    BooleanOption disableCameraBobbing = CombatifyClient.getInstance().getConfig().animations().disableCameraBobbing();

    BooleanOption alternativeBobbing = CombatifyClient.getInstance().getConfig().hudRendering().alternativeBobbing();

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
    public void cancelCameraShake(GameRenderer instance, PoseStack poseStack, float f, Operation<Void> original) {
        if (!this.disableCameraBobbing.get()) {
            original.call(instance, poseStack, f);
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    public void changeToAlternativeBob(PoseStack poseStack, float f, CallbackInfo ci) {
        if (alternativeBobbing.get()) {
            this.alternativeBobView(poseStack, f);
            ci.cancel();
        }
    }


    private void alternativeBobView(PoseStack poseStack, float f) {
        if (this.minecraft.getCameraEntity() instanceof Player player) {
            float g = player.walkDist - player.walkDistO;
            float h = -(player.walkDist + g * f);
            float i = Mth.lerp(f, player.oBob, player.bob);
            poseStack.translate(Mth.sin(h * 3.1415927F) * i * 0.5F, -Math.abs(Mth.cos(h * 3.1415927F) * i), 0.0D);
            poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.cos(h * 3.1415927F) * i * 3.0F));
        }
    }
}
