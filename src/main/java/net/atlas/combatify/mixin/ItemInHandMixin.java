package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandMixin {
	@Shadow @Final private Minecraft minecraft;
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
	@WrapOperation(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", ordinal = 5))
	private void modifyBowCode(PoseStack instance, float f, float g, float h, Operation<Void> original) {
		instance.translate(f, 0.18344387412071228, 0.15731531381607056);
	}
	@WrapOperation(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", ordinal = 6))
	private void modifyBowCode1(PoseStack instance, float x, float y, float z, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) float f, @Local(ordinal = 0, argsOnly = true) ItemStack itemStack) {
		assert minecraft.player != null;
		float r = (float)itemStack.getUseDuration(minecraft.player) - ((float)this.minecraft.player.getUseItemRemainingTicks() - f + 1.0F);
		float m = Mth.sin((r - 0.1F) * 1.3F);
		float n = MethodHandler.getFatigueForTime((int) r) - 0.1F;
		float o = m * n;
		original.call(instance, o * 0.0F, o * 0.004F, o * 0.0F);
	}
}
