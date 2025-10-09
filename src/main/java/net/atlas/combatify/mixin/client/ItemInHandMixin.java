package net.atlas.combatify.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandMixin {
	@Shadow @Final private Minecraft minecraft;
	@Shadow
	private float oMainHandHeight;

	@Shadow
	private float oOffHandHeight;

	@ModifyExpressionValue(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isUsingItem()Z", ordinal = 1))
	private boolean modifyUseItemCheck(boolean original, @Local(ordinal = 0, argsOnly = true) AbstractClientPlayer abstractClientPlayer, @Local(ordinal = 0, argsOnly = true) InteractionHand interactionHand, @Share("isFakingUsingItem") LocalBooleanRef fakeUsingItem) {
		boolean isReallyUsingItem = abstractClientPlayer.isUsingItem() && abstractClientPlayer.getUsedItemHand() == interactionHand;
		fakeUsingItem.set(!isReallyUsingItem && MethodHandler.getBlockingItem(abstractClientPlayer).useHand() == interactionHand && abstractClientPlayer.isBlocking());
		return original || fakeUsingItem.get();
	}
	@ModifyExpressionValue(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;getUseItemRemainingTicks()I", ordinal = 2))
	private int modifyUseItemRemainingCheck(int original, @Share("isFakingUsingItem") LocalBooleanRef fakeUsingItem) {
		if (fakeUsingItem.get())
			return Integer.MAX_VALUE;
		return original;
	}
	@ModifyExpressionValue(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;getUsedItemHand()Lnet/minecraft/world/InteractionHand;", ordinal = 1))
	private InteractionHand modifyUseHandCheck(InteractionHand original, @Local(ordinal = 0, argsOnly = true) InteractionHand interactionHand, @Share("isFakingUsingItem") LocalBooleanRef fakeUsingItem) {
		if (fakeUsingItem.get())
			return interactionHand;
		return original;
	}
	@WrapOperation(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", ordinal = 6))
	private void modifyBowCode1(PoseStack instance, float x, float y, float z, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) float f, @Local(ordinal = 0, argsOnly = true) ItemStack itemStack) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) {
			original.call(instance, x, y, z);
			return;
		}
		assert minecraft.player != null;
		float r = (float)itemStack.getUseDuration(minecraft.player) - ((float)this.minecraft.player.getUseItemRemainingTicks() - f + 1.0F);
		float m = Mth.sin((r - 0.1F) * 1.3F);
		float n = MethodHandler.getFatigueForTime((int) r) - 0.1F;
		float o = m * n;
		original.call(instance, o * 0.0F, o * 0.004F, o * 0.0F);
	}
	@WrapOperation(method = "renderArmWithItem", slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/UseAnim;"), to = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isAutoSpinAttack()Z")), at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;applyItemArmTransform(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V", ordinal = 2))
	private void addBlockingAnim(ItemInHandRenderer instance, PoseStack poseStack, HumanoidArm humanoidArm, float f, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) ItemStack stack) {
		original.call(instance, poseStack, humanoidArm, f);
		if (!(stack.getItem() instanceof ShieldItem)) applyItemBlockTransform(poseStack, humanoidArm);
	}
	@Inject(method = "itemUsed", at = @At("HEAD"))
	private void modifyOldStates(InteractionHand interactionHand, CallbackInfo ci) {
		if (interactionHand == InteractionHand.MAIN_HAND) {
			this.oMainHandHeight = 0.0F;
		} else {
			this.oOffHandHeight = 0.0F;
		}
	}
	/* Values from 15w33b, thanks to Fuzss for providing them
		https://github.com/Fuzss/swordblockingcombat/blob/1.15/src/main/java/com/fuzs/swordblockingcombat/client/handler/RenderBlockingHandler.java
	*/
	@Unique
	public void applyItemBlockTransform(PoseStack poseStack, HumanoidArm humanoidArm) {
		int reverse = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
		poseStack.translate(reverse * -0.14142136F, 0.08F, 0.14142136F);
		poseStack.mulPose(Axis.XP.rotationDegrees(-102.25F));
		poseStack.mulPose(Axis.YP.rotationDegrees(reverse * 13.365F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(reverse * 78.05F));
	}
}
