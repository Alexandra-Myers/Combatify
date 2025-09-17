package net.atlas.combatify.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {
	@ModifyExpressionValue(method = "getArmPose(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Avatar;getUseItemRemainingTicks()I"))
	private static int modifyUseItemRemainingCheck(int original, @Share("isFakingUsingItem") LocalBooleanRef fakeUsingItem) {
		if (fakeUsingItem.get())
			return Integer.MAX_VALUE;
		return original;
	}
	@ModifyExpressionValue(method = "getArmPose(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Avatar;getUsedItemHand()Lnet/minecraft/world/InteractionHand;"))
	private static InteractionHand modifyUseHandCheck(InteractionHand original, @Local(ordinal = 0, argsOnly = true) Avatar avatar, @Local(ordinal = 0, argsOnly = true) InteractionHand interactionHand, @Share("isFakingUsingItem") LocalBooleanRef fakeUsingItem) {
		boolean isReallyUsingItem = original == interactionHand && avatar.getUseItemRemainingTicks() > 0;
		fakeUsingItem.set(!isReallyUsingItem && MethodHandler.getBlockingItem(avatar).useHand() == interactionHand && avatar.isBlocking());
		if (fakeUsingItem.get())
			return interactionHand;
		return original;
	}
}
