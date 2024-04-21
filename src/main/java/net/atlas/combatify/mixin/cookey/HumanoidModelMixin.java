package net.atlas.combatify.mixin.cookey;

import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.UseAnim;
import net.atlas.combatify.config.cookey.option.BooleanOption;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> extends AgeableListModel<T> implements ArmedModel, HeadedModel {
    @Final
    @Shadow
    public ModelPart rightArm;

    @Final
    @Shadow
    public ModelPart leftArm;

    @Shadow
    protected abstract void poseLeftArm(T livingEntity);

    @Shadow
    protected abstract void poseRightArm(T livingEntity);


    BooleanOption showEatingInThirdPerson = CombatifyClient.getInstance().getConfig().animations().showEatingInThirdPerson();

    private static final BooleanOption enableToolBlocking = CombatifyClient.getInstance().getConfig().animations().enableToolBlocking();

    @Inject(method = "poseRightArm", at = @At("HEAD"), cancellable = true)
    public void addRightArmAnimations(T livingEntity, CallbackInfo ci) {
        HumanoidArm usedHand = livingEntity.getUsedItemHand() == InteractionHand.MAIN_HAND
                ? livingEntity.getMainArm()
                : livingEntity.getMainArm().getOpposite();
        boolean poseLeftArmAfterwards = false;
        if (enableToolBlocking.get()) {
            ItemStack itemInRightArm = livingEntity.getItemInHand(livingEntity.getMainArm() == HumanoidArm.RIGHT ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
            if (itemInRightArm.getItem() instanceof ShieldItem && MethodHandler.getBlockingItem(livingEntity).equals(itemInRightArm))
                poseLeftArmAfterwards = true;
        }
        if (this.showEatingInThirdPerson.get()
                && livingEntity.isUsingItem() && usedHand == HumanoidArm.RIGHT && (livingEntity.getUseItem().getUseAnimation() == UseAnim.EAT || livingEntity.getUseItem().getUseAnimation() == UseAnim.DRINK)) {
            boolean run = this.applyEatingAnimation(livingEntity, usedHand, ((MinecraftAccessor) Minecraft.getInstance()).getTimer().partialTick);
            if (run) ci.cancel();
            return;
        }
        if (poseLeftArmAfterwards) this.poseLeftArm(livingEntity);
    }

    @Inject(method = "poseLeftArm", at = @At("HEAD"), cancellable = true)
    public void addLeftArmAnimations(T livingEntity, CallbackInfo ci) {
        HumanoidArm usedHand = livingEntity.getUsedItemHand() == InteractionHand.MAIN_HAND
                ? livingEntity.getMainArm()
                : livingEntity.getMainArm().getOpposite();
        boolean poseRightArmAfterwards = false;
        if (enableToolBlocking.get()) {
            ItemStack itemInLeftArm = livingEntity.getItemInHand(livingEntity.getMainArm() == HumanoidArm.RIGHT ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            if (itemInLeftArm.getItem() instanceof ShieldItem && MethodHandler.getBlockingItem(livingEntity).equals(itemInLeftArm))
                poseRightArmAfterwards = true;
        }
        if (this.showEatingInThirdPerson.get()
                && livingEntity.isUsingItem() && usedHand == HumanoidArm.LEFT && (livingEntity.getUseItem().getUseAnimation() == UseAnim.EAT || livingEntity.getUseItem().getUseAnimation() == UseAnim.DRINK)) {
            boolean run = this.applyEatingAnimation(livingEntity, usedHand, ((MinecraftAccessor) Minecraft.getInstance()).getTimer().partialTick);
            if (run) ci.cancel();
            return;
        }
        if (poseRightArmAfterwards) this.poseRightArm(livingEntity);
    }

    // Animation values and "formula" from ItemInHandRenderer's applyEatAnimation

    public boolean applyEatingAnimation(LivingEntity livingEntity, HumanoidArm humanoidArm, float f) {
        int side = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
        float xRot = humanoidArm == HumanoidArm.RIGHT ? this.rightArm.xRot : this.leftArm.xRot;
        float yRot;

        float g = livingEntity.getUseItemRemainingTicks() - f + 1.0F;
        float h = g / livingEntity.getUseItem().getUseDuration();
        if (h < -1.0F) return false; // Stop animation from going wild if eating won't process
        float j;
        float k = Math.min(1.0F - (float) Math.pow(h, 27.0D), 1.0F);
        if (h < 0.8F) {
            j = Mth.abs(Mth.cos(g / 4.0F * 3.1415927F) * 0.25F);
            xRot = xRot * 0.5F - 1.57079633F + j;
        } else {
            xRot = k * (xRot * 0.5F - 1.32079633F);
        }

        yRot = side * k * -0.5235988F;

        if (humanoidArm == HumanoidArm.RIGHT) {
            this.rightArm.xRot = xRot;
            this.rightArm.yRot = yRot;
        } else {
            this.leftArm.xRot = xRot;
            this.leftArm.yRot = yRot;
        }

        return true;
    }
}
