package net.alexandra.atlas.atlas_combat.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.extensions.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.rizecookey.cookeymod.CookeyMod;
import net.rizecookey.cookeymod.config.category.AnimationsCategory;
import net.rizecookey.cookeymod.config.category.HudRenderingCategory;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandMixin implements IItemInHandRenderer {
	@Shadow
	@Final
	private Minecraft minecraft;
	@Shadow
	private ItemStack offHandItem;

	@Shadow
	private ItemStack mainHandItem;
	HudRenderingCategory hudRenderingCategory = (HudRenderingCategory)CookeyMod.getInstance().getConfig().getCategory(HudRenderingCategory.class);

	@Shadow
	protected abstract void applyItemArmAttackTransform(PoseStack matrices, HumanoidArm arm, float swingProgress);

	@Shadow
	protected abstract void applyItemArmTransform(PoseStack matrices, HumanoidArm arm, float equipProgress);

	AnimationsCategory animationsCategory = CookeyMod.getInstance().getConfig().getCategory(AnimationsCategory.class);

	@Shadow
	public abstract void renderItem(LivingEntity entity, ItemStack stack, ItemTransforms.TransformType renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource vertexConsumers, int light);

	@Inject(
			method = {"renderArmWithItem"},
			at = {@At("HEAD")},
			cancellable = true
	)
	public void onRenderArmWithItem(AbstractClientPlayer abstractClientPlayer, float f, float g, InteractionHand interactionHand, float h, ItemStack itemStack, float i, PoseStack poseStack, MultiBufferSource multiBufferSource, int j, CallbackInfo ci) {
		if (AtlasCombat.CONFIG.swordBlocking()) {
			if (abstractClientPlayer.getUsedItemHand() == interactionHand && !((LivingEntityExtensions)abstractClientPlayer).getBlockingItem().isEmpty() && ((LivingEntityExtensions)abstractClientPlayer).getBlockingItem().getItem() instanceof SwordItem) {
				poseStack.pushPose();
				HumanoidArm humanoidArm = interactionHand == InteractionHand.MAIN_HAND
						? abstractClientPlayer.getMainArm()
						: abstractClientPlayer.getMainArm().getOpposite();
				applyItemArmTransform(poseStack, humanoidArm, i);
				applyItemBlockTransform2(poseStack, humanoidArm);
				if (animationsCategory.swingAndUseItem.get()) {
					this.applyItemArmAttackTransform(poseStack, humanoidArm, h);
				}
				boolean isRightHand = humanoidArm == HumanoidArm.RIGHT;
				renderItem(abstractClientPlayer, itemStack, isRightHand ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !isRightHand, poseStack, multiBufferSource, j);

				poseStack.popPose();
				ci.cancel();
			}
		}
	}
	/*@ModifyVariable(method = "tick", slice = @Slice(
			from = @At(value = "JUMP", ordinal = 3)
	), at = @At(value = "FIELD", ordinal = 0))
	public float modifyArmHeight(float f) {
		f *= 0.5;
		f = f * f * f * 0.25F + 0.75F;
		double offset = (Double)this.hudRenderingCategory.attackCooldownHandOffset.get();
		return (float)((double)f * (1.0 - offset) + offset);
	}*/

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"))
	public float modifyArmHeight(LocalPlayer instance, float f) {
		f *= 0.5;
		f = f * f * f * 0.25F + 0.75F;
		double offset = this.hudRenderingCategory.attackCooldownHandOffset.get();
		return (float)((double)f * (1.0 - offset) + offset);
	}

	@Inject(method = "applyItemArmTransform", at = @At(value = "HEAD"), cancellable = true)
	public void injectSwordBlocking(PoseStack matrices, HumanoidArm arm, float equipProgress, CallbackInfo ci) {
		if(((LivingEntityExtensions)minecraft.player).getBlockingItem().getItem() instanceof SwordItem) {
			int i = arm == HumanoidArm.RIGHT ? 1 : -1;
			matrices.translate((double)((float)i * 0.56F), (double)(-0.52F + 0.0 * -0.6F), -0.72F);
			ci.cancel();
		}
	}
	@Override
	public void applyItemBlockTransform2(PoseStack poseStack, HumanoidArm humanoidArm) {
		int reverse = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
		poseStack.translate(reverse * -0.14142136F, 0.08F, 0.14142136F);
		/*poseStack.mulPose(Vector3f.XP.rotationDegrees(-102.25F));
		poseStack.mulPose(Vector3f.YP.rotationDegrees(reverse * 13.365F));
		poseStack.mulPose(Vector3f.ZP.rotationDegrees(reverse * 78.05F));*/
		poseStack.mulPose(new Quaternionf().rotateXYZ(-102.25F, reverse * 13.365F, reverse * 78.05F));
	}
}
