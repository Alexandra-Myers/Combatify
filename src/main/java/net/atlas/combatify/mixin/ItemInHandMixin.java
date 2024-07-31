package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.config.cookey.ModConfig;
import net.atlas.combatify.config.cookey.category.AnimationsCategory;
import net.atlas.combatify.config.cookey.category.HudRenderingCategory;
import net.atlas.combatify.extensions.IItemInHandRenderer;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandMixin implements IItemInHandRenderer {
	@Shadow
	protected abstract void applyItemArmAttackTransform(PoseStack poseStack, HumanoidArm humanoidArm, float f);

	@Shadow
	protected abstract void applyItemArmTransform(PoseStack poseStack, HumanoidArm humanoidArm, float f);

	@Shadow
	public abstract void renderItem(LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext displayContext, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource, int i);

	@Shadow
	private ItemStack offHandItem;

	@Shadow
	private ItemStack mainHandItem;

	@Shadow @Final private Minecraft minecraft;

	@Unique
	private AnimationsCategory animationsCategory;

	@Unique
	private HudRenderingCategory hudRenderingCategory;

    @Inject(method = "<init>", at = @At("TAIL"))
	private void injectOptions(Minecraft minecraft, EntityRenderDispatcher entityRenderDispatcher, ItemRenderer itemRenderer, CallbackInfo ci) {
		ModConfig modConfig = CombatifyClient.getInstance().getConfig();
		animationsCategory = modConfig.animations();
		hudRenderingCategory = modConfig.hudRendering();
    }

	@Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
	public void onRenderArmWithItem(AbstractClientPlayer abstractClientPlayer, float f, float g, InteractionHand interactionHand, float h, ItemStack itemStack, float i, PoseStack poseStack, MultiBufferSource multiBufferSource, int j, CallbackInfo ci) {
		HumanoidArm humanoidArm = interactionHand == InteractionHand.MAIN_HAND
			? abstractClientPlayer.getMainArm()
			: abstractClientPlayer.getMainArm().getOpposite();

		ItemStack blockingItem = MethodHandler.getBlockingItem(abstractClientPlayer).stack();
		if ((hudRenderingCategory.onlyShowShieldWhenBlocking().get() || animationsCategory.enableToolBlocking().get())
			&& (itemStack.getItem() instanceof ShieldItem && !(!blockingItem.isEmpty() && blockingItem.getItem() instanceof ShieldItem))) {
			ci.cancel();

		}
		if (animationsCategory.enableToolBlocking().get()) {
			ItemStack otherHandItem = interactionHand == InteractionHand.MAIN_HAND ? this.offHandItem : this.mainHandItem;
			if (itemStack.getItem() instanceof ShieldItem && (otherHandItem.getItem() instanceof TieredItem && (!blockingItem.isEmpty() && blockingItem.getItem() instanceof ShieldItem))) {
				ci.cancel();
			}

			if (abstractClientPlayer.getUsedItemHand() != interactionHand && (!blockingItem.isEmpty() && blockingItem.getItem() instanceof ShieldItem) && itemStack.getItem() instanceof TieredItem) {
				poseStack.pushPose();
				this.applyItemArmTransform(poseStack, humanoidArm, i);
				this.applyItemBlockTransform(poseStack, humanoidArm);
				if (animationsCategory.swingAndUseItem().get()) {
					this.applyItemArmAttackTransform(poseStack, humanoidArm, h);
				}
				boolean isRightHand = humanoidArm == HumanoidArm.RIGHT;
				this.renderItem(abstractClientPlayer, itemStack, isRightHand ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, !isRightHand, poseStack, multiBufferSource, j);

				poseStack.popPose();
				ci.cancel();
			}
		}
		if (Combatify.CONFIG.swordBlocking()) {
			if (!blockingItem.isEmpty() && MethodHandler.getBlockingItem(abstractClientPlayer).useHand() == interactionHand && ((ItemExtensions) blockingItem.getItem()).getBlockingType().isToolBlocker()) {
				poseStack.pushPose();
				applyItemArmTransform(poseStack, humanoidArm, i);
				applyItemBlockTransform(poseStack, humanoidArm);
				if (animationsCategory.swingAndUseItem().get()) {
					this.applyItemArmAttackTransform(poseStack, humanoidArm, h);
				}
				boolean isRightHand = humanoidArm == HumanoidArm.RIGHT;
				renderItem(abstractClientPlayer, itemStack, isRightHand ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, !isRightHand, poseStack, multiBufferSource, j);

				poseStack.popPose();
				ci.cancel();
			}
		}
	}

	@ModifyExpressionValue(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isInvisible()Z", ordinal = 0))
	public boolean makeArmAppear(boolean original) {
		return !hudRenderingCategory.showHandWhenInvisible().get() && original;
	}

	@Redirect(method = "renderArmWithItem",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;applyItemArmAttackTransform(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V",
			ordinal = 1))
	public void cancelAttackTransform(ItemInHandRenderer itemInHandRenderer, PoseStack poseStack, HumanoidArm humanoidArm, float f) {
		if (!animationsCategory.swingAndUseItem().get())
			this.applyItemArmAttackTransform(poseStack, humanoidArm, f);
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
		if (animationsCategory.swingAndUseItem().get() && !abstractClientPlayer.isAutoSpinAttack()) {
			this.applyItemArmAttackTransform(poseStack, humanoidArm, h);
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

	//This works, trust us
	@ModifyVariable(method = "tick", slice = @Slice(
			from = @At(value = "JUMP", ordinal = 3)
	), at = @At(value = "FIELD", ordinal = 0))
	public float modifyArmHeight(float f) {
		if (Combatify.CONFIG.chargedAttacks())
			f *= 0.5f;
		f = f * f * f * 0.25F + 0.75F;
		double offset = hudRenderingCategory.attackCooldownHandOffset().get();
		return (float) (f * (1 - offset) + offset);
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
	@Redirect(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", ordinal = 5))
	private void modifyBowCode(PoseStack instance, float x, float y, float z, @Local(ordinal = 0) HumanoidArm humanoidArm) {
		int q = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
		instance.translate(q * -0.2785682, 0.18344387412071228, 0.15731531381607056);
	}
	@Redirect(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", ordinal = 6))
	private void modifyBowCode1(PoseStack instance, float x, float y, float z, @Local(ordinal = 0, argsOnly = true) float f, @Local(ordinal = 0, argsOnly = true) ItemStack itemStack) {
		assert minecraft.player != null;
		float r = (float)itemStack.getUseDuration(minecraft.player) - ((float)this.minecraft.player.getUseItemRemainingTicks() - f + 1.0F);
		float m = Mth.sin((r - 0.1F) * 1.3F);
		float n = MethodHandler.getFatigueForTime((int) r) - 0.1F;
		float o = m * n;
		instance.translate(o * 0.0F, o * 0.004F, o * 0.0F);
	}
	@Inject(method = "applyItemArmTransform", at = @At(value = "HEAD"), cancellable = true)
	public void injectSwordBlocking(PoseStack matrices, HumanoidArm arm, float equipProgress, CallbackInfo ci) {
		assert minecraft.player != null;
		if(MethodHandler.getBlockingItem(minecraft.player).getItem() instanceof ItemExtensions blocker && blocker.getBlockingType().isToolBlocker() && !blocker.getBlockingType().isEmpty()) {
			int i = arm == HumanoidArm.RIGHT ? 1 : -1;
			matrices.translate(((float)i * 0.56F), (-0.52F + 0.0 * -0.6F), -0.72F);
			ci.cancel();
		}
	}
}
