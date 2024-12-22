package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.CookeyMod;
import net.atlas.combatify.extensions.IItemInHandRenderer;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandMixin implements IItemInHandRenderer {
	@Shadow
	protected abstract void applyItemArmAttackTransform(PoseStack poseStack, HumanoidArm humanoidArm, float f);

	@Shadow
	protected abstract void applyItemArmTransform(PoseStack poseStack, HumanoidArm humanoidArm, float f);

	@Shadow
	public abstract void renderItem(LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext displayContext, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource, int i);

	@Shadow @Final private Minecraft minecraft;

	@Shadow
	protected abstract void swingArm(float f, float g, PoseStack poseStack, int i, HumanoidArm humanoidArm);

	@Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
	public void onRenderArmWithItem(AbstractClientPlayer abstractClientPlayer, float f, float g, InteractionHand interactionHand, float h, ItemStack itemStack, float i, PoseStack poseStack, MultiBufferSource multiBufferSource, int j, CallbackInfo ci) {
		HumanoidArm humanoidArm = interactionHand == InteractionHand.MAIN_HAND
			? abstractClientPlayer.getMainArm()
			: abstractClientPlayer.getMainArm().getOpposite();

		ItemStack blockingItem = MethodHandler.getBlockingItem(abstractClientPlayer).stack();
		if ((CookeyMod.getConfig().hudRendering().onlyShowShieldWhenBlocking().get() || CookeyMod.getConfig().animations().enableToolBlocking().get())
			&& (itemStack.getItem() instanceof ShieldItem && !(!blockingItem.isEmpty() && blockingItem.getItem() instanceof ShieldItem))) {
			ci.cancel();
		}
		if (CookeyMod.getConfig().animations().enableToolBlocking().get()) {
            if (itemStack.getItem() instanceof ShieldItem && !blockingItem.isEmpty() && blockingItem.getItem() instanceof ShieldItem) {
				ci.cancel();
			}

			if (abstractClientPlayer.getUsedItemHand() != interactionHand && !blockingItem.isEmpty() && blockingItem.getItem() instanceof ShieldItem) {
				poseStack.pushPose();
				this.applyItemArmTransform(poseStack, humanoidArm, i);
				int reverse = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
				poseStack.translate(reverse * -0.14142136F, 0.08F, 0.14142136F);
				poseStack.mulPose(Axis.XP.rotationDegrees(-102.25F));
				poseStack.mulPose(Axis.YP.rotationDegrees(reverse * 13.365F));
				poseStack.mulPose(Axis.ZP.rotationDegrees(reverse * 78.05F));
				if (CookeyMod.getConfig().animations().swingAndUseItem().get()) {
					this.applyItemArmAttackTransform(poseStack, humanoidArm, h);
				}
				boolean isRightHand = humanoidArm == HumanoidArm.RIGHT;
				this.renderItem(abstractClientPlayer, itemStack, isRightHand ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, !isRightHand, poseStack, multiBufferSource, j);

				poseStack.popPose();
				ci.cancel();
			}
		}
	}

	@ModifyExpressionValue(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isInvisible()Z", ordinal = 0))
	public boolean makeArmAppear(boolean original) {
		return !CookeyMod.getConfig().hudRendering().showHandWhenInvisible().get() && original;
	}

	@WrapOperation(method = "renderArmWithItem",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;swingArm(FFLcom/mojang/blaze3d/vertex/PoseStack;ILnet/minecraft/world/entity/HumanoidArm;)V",
			ordinal = 1))
	public void cancelAttackTransform(ItemInHandRenderer instance, float f, float g, PoseStack poseStack, int i, HumanoidArm humanoidArm, Operation<Void> original) {
		if (!CookeyMod.getConfig().animations().swingAndUseItem().get())
			original.call(instance, f, g, poseStack, i, humanoidArm);
	}

	@WrapOperation(method = "renderArmWithItem",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;swingArm(FFLcom/mojang/blaze3d/vertex/PoseStack;ILnet/minecraft/world/entity/HumanoidArm;)V",
			ordinal = 2))
	public void cancelAttackTransform0(ItemInHandRenderer instance, float f, float g, PoseStack poseStack, int i, HumanoidArm humanoidArm, Operation<Void> original) {
		if (!CookeyMod.getConfig().animations().swingAndUseItem().get())
			original.call(instance, f, g, poseStack, i, humanoidArm);
	}

	@Inject(method = "renderArmWithItem",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			ordinal = 1, shift = At.Shift.BEFORE))
	public void injectAttackTransform(AbstractClientPlayer abstractClientPlayer, float f, float g, InteractionHand interactionHand, float h, ItemStack itemStack, float i, PoseStack poseStack, MultiBufferSource multiBufferSource, int j, CallbackInfo ci) {
		HumanoidArm humanoidArm = interactionHand == InteractionHand.MAIN_HAND
			? abstractClientPlayer.getMainArm()
			: abstractClientPlayer.getMainArm().getOpposite();
		if (CookeyMod.getConfig().animations().swingAndUseItem().get() && !abstractClientPlayer.isAutoSpinAttack()) {
			this.swingArm(h, i, poseStack, humanoidArm == HumanoidArm.RIGHT ? 1 : -1, humanoidArm);
		}
	}
	@ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"))
	public float modifyArmHeight(float strengthScale) {
		if (Combatify.CONFIG.chargedAttacks())
			strengthScale *= 0.5f;
		if (CombatifyClient.augmentedArmHeight.get())
			strengthScale = strengthScale * strengthScale * strengthScale * 0.25F + 0.75F;
		double offset = CookeyMod.getConfig().hudRendering().attackCooldownHandOffset().get();
		return (float) (strengthScale * (1 - offset) + offset);
	}
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
	@Inject(method = "applyItemArmTransform", at = @At(value = "HEAD"), cancellable = true)
	public void injectSwordBlocking(PoseStack matrices, HumanoidArm arm, float equipProgress, CallbackInfo ci) {
		assert minecraft.player != null;
		if(MethodHandler.getBlockingItem(minecraft.player).getItem() instanceof ItemExtensions blocker && blocker.combatify$getBlockingType().isToolBlocker() && !blocker.combatify$getBlockingType().isEmpty()) {
			int i = arm == HumanoidArm.RIGHT ? 1 : -1;
			matrices.translate(((float)i * 0.56F), (-0.52F + 0.0 * -0.6F), -0.72F);
			ci.cancel();
		}
	}
}
