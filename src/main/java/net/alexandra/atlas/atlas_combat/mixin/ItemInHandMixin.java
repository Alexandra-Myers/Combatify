package net.alexandra.atlas.atlas_combat.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.extensions.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.util.Mth;
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

	@Shadow
	protected abstract void applyEatTransform(PoseStack poseStack, float f, HumanoidArm humanoidArm, ItemStack itemStack);

	@Shadow
	protected abstract void renderTwoHandedMap(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f, float g, float h);

	@Shadow
	protected abstract void renderOneHandedMap(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f, HumanoidArm humanoidArm, float g, ItemStack itemStack);

	@Shadow
	protected abstract void renderPlayerArm(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f, float g, HumanoidArm humanoidArm);
	@ModifyVariable(method = "tick", slice = @Slice(
			from = @At(value = "JUMP", ordinal = 3)
	), at = @At(value = "FIELD", ordinal = 0))
	public float modifyArmHeight(float f) {
		f *= 0.5;
		f = f * f * f * 0.25F + 0.75F;
		double offset = this.hudRenderingCategory.attackCooldownHandOffset.get();
		return (float)((double)f * (1.0 - offset) + offset);
	}

	@Inject(method = "renderArmWithItem", at = @At(value = "HEAD"), cancellable = true)
	private void renderArmWithItem(AbstractClientPlayer abstractClientPlayer, float f, float g, InteractionHand interactionHand, float h, ItemStack itemStack, float i, PoseStack poseStack, MultiBufferSource multiBufferSource, int j, CallbackInfo ci) {
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
		if (!abstractClientPlayer.isScoping()) {
			boolean bl = interactionHand == InteractionHand.MAIN_HAND;
			HumanoidArm humanoidArm = bl ? abstractClientPlayer.getMainArm() : abstractClientPlayer.getMainArm().getOpposite();
			poseStack.pushPose();
			if (itemStack.isEmpty()) {
				if (bl && !abstractClientPlayer.isInvisible()) {
					this.renderPlayerArm(poseStack, multiBufferSource, j, i, h, humanoidArm);
				}
			} else if (itemStack.is(Items.FILLED_MAP)) {
				if (bl && this.offHandItem.isEmpty()) {
					this.renderTwoHandedMap(poseStack, multiBufferSource, j, g, i, h);
				} else {
					this.renderOneHandedMap(poseStack, multiBufferSource, j, i, humanoidArm, h, itemStack);
				}
			} else if (itemStack.is(Items.CROSSBOW)) {
				boolean bl2 = CrossbowItem.isCharged(itemStack);
				boolean bl3 = humanoidArm == HumanoidArm.RIGHT;
				int k = bl3 ? 1 : -1;
				if (abstractClientPlayer.isUsingItem() && abstractClientPlayer.getUseItemRemainingTicks() > 0 && abstractClientPlayer.getUsedItemHand() == interactionHand
				)
				{
					this.applyItemArmTransform(poseStack, humanoidArm, i);
					poseStack.translate((float)k * -0.4785682F, -0.094387F, 0.05731531F);
					poseStack.mulPose(Axis.XP.rotationDegrees(-11.935F));
					poseStack.mulPose(Axis.YP.rotationDegrees((float)k * 65.3F));
					poseStack.mulPose(Axis.ZP.rotationDegrees((float)k * -9.785F));
					float l = (float)itemStack.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - f + 1.0F);
					float m = l / (float)CrossbowItem.getChargeDuration(itemStack);
					if (m > 1.0F) {
						m = 1.0F;
					}

					if (m > 0.1F) {
						float n = Mth.sin((l - 0.1F) * 1.3F);
						float o = m - 0.1F;
						float p = n * o;
						poseStack.translate(p * 0.0F, p * 0.004F, p * 0.0F);
					}

					poseStack.translate(m * 0.0F, m * 0.0F, m * 0.04F);
					poseStack.scale(1.0F, 1.0F, 1.0F + m * 0.2F);
					poseStack.mulPose(Axis.YN.rotationDegrees((float)k * 45.0F));
				} else {
					float l = -0.4F * Mth.sin(Mth.sqrt(h) * (float) Math.PI);
					float m = 0.2F * Mth.sin(Mth.sqrt(h) * (float) (Math.PI * 2));
					float n = -0.2F * Mth.sin(h * (float) Math.PI);
					poseStack.translate((float)k * l, m, n);
					this.applyItemArmTransform(poseStack, humanoidArm, i);
					this.applyItemArmAttackTransform(poseStack, humanoidArm, h);
					if (bl2 && h < 0.001F && bl) {
						poseStack.translate((float)k * -0.641864F, 0.0F, 0.0F);
						poseStack.mulPose(Axis.YP.rotationDegrees((float)k * 10.0F));
					}
				}

				this.renderItem(
						abstractClientPlayer,
						itemStack,
						bl3 ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
						!bl3,
						poseStack,
						multiBufferSource,
						j
				);
			} else {
				boolean bl2 = humanoidArm == HumanoidArm.RIGHT;
				if (abstractClientPlayer.isUsingItem() && abstractClientPlayer.getUseItemRemainingTicks() > 0 && abstractClientPlayer.getUsedItemHand() == interactionHand
				)
				{
					int q = bl2 ? 1 : -1;
					switch (itemStack.getUseAnimation()) {
						case NONE -> this.applyItemArmTransform(poseStack, humanoidArm, i);
						case EAT, DRINK -> {
							this.applyEatTransform(poseStack, f, humanoidArm, itemStack);
							this.applyItemArmTransform(poseStack, humanoidArm, i);
						}
						case BLOCK -> this.applyItemArmTransform(poseStack, humanoidArm, i);
						case BOW -> {
							this.applyItemArmTransform(poseStack, humanoidArm, i);
							poseStack.translate((float) q * -0.2785682F, 0.18344387F, 0.15731531F);
							poseStack.mulPose(Axis.XP.rotationDegrees(-13.935F));
							poseStack.mulPose(Axis.YP.rotationDegrees((float) q * 35.3F));
							poseStack.mulPose(Axis.ZP.rotationDegrees((float) q * -9.785F));
							float r = (float) itemStack.getUseDuration() - ((float) this.minecraft.player.getUseItemRemainingTicks() - f + 1.0F);
							float l = r / 20.0F;
							l = (l * l + l * 2.0F) / 3.0F;
							if (l > 1.0F) {
								l = 1.0F;
							}
							if (l > 0.1F) {
								float m = Mth.sin((r - 0.1F) * 1.3F);
								float n = ((IBowItem)itemStack.getItem()).getFatigueForTime((int)r) - 0.1F;
								float o = m * n;
								poseStack.translate(o * 0.0F, o * 0.004F, o * 0.0F);
							}
							poseStack.translate(l * 0.0F, l * 0.0F, l * 0.04F);
							poseStack.scale(1.0F, 1.0F, 1.0F + l * 0.2F);
							poseStack.mulPose(Axis.YN.rotationDegrees((float) q * 45.0F));
						}
						case SPEAR -> {
							this.applyItemArmTransform(poseStack, humanoidArm, i);
							poseStack.translate((float) q * -0.5F, 0.7F, 0.1F);
							poseStack.mulPose(Axis.XP.rotationDegrees(-55.0F));
							poseStack.mulPose(Axis.YP.rotationDegrees((float) q * 35.3F));
							poseStack.mulPose(Axis.ZP.rotationDegrees((float) q * -9.785F));
							float r = (float) itemStack.getUseDuration() - ((float) this.minecraft.player.getUseItemRemainingTicks() - f + 1.0F);
							float l = r / 10.0F;
							if (l > 1.0F) {
								l = 1.0F;
							}
							if (l > 0.1F) {
								float m = Mth.sin((r - 0.1F) * 1.3F);
								float n = l - 0.1F;
								float o = m * n;
								poseStack.translate(o * 0.0F, o * 0.004F, o * 0.0F);
							}
							poseStack.translate(0.0F, 0.0F, l * 0.2F);
							poseStack.scale(1.0F, 1.0F, 1.0F + l * 0.2F);
							poseStack.mulPose(Axis.YN.rotationDegrees((float) q * 45.0F));
						}
					}
				} else if (abstractClientPlayer.isAutoSpinAttack()) {
					this.applyItemArmTransform(poseStack, humanoidArm, i);
					int q = bl2 ? 1 : -1;
					poseStack.translate((float)q * -0.4F, 0.8F, 0.3F);
					poseStack.mulPose(Axis.YP.rotationDegrees((float)q * 65.0F));
					poseStack.mulPose(Axis.ZP.rotationDegrees((float)q * -85.0F));
				} else {
					float s = -0.4F * Mth.sin(Mth.sqrt(h) * (float) Math.PI);
					float r = 0.2F * Mth.sin(Mth.sqrt(h) * (float) (Math.PI * 2));
					float l = -0.2F * Mth.sin(h * (float) Math.PI);
					int t = bl2 ? 1 : -1;
					poseStack.translate((float)t * s, r, l);
					this.applyItemArmTransform(poseStack, humanoidArm, i);
					this.applyItemArmAttackTransform(poseStack, humanoidArm, h);
				}

				this.renderItem(
						abstractClientPlayer,
						itemStack,
						bl2 ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
						!bl2,
						poseStack,
						multiBufferSource,
						j
				);
			}

			poseStack.popPose();
		}
		ci.cancel();
	}
	@Inject(method = "applyItemArmTransform", at = @At(value = "HEAD"), cancellable = true)
	public void injectSwordBlocking(PoseStack matrices, HumanoidArm arm, float equipProgress, CallbackInfo ci) {
		if(((LivingEntityExtensions)minecraft.player).getBlockingItem().getItem() instanceof SwordItem) {
			int i = arm == HumanoidArm.RIGHT ? 1 : -1;
			matrices.translate(((float)i * 0.56F), (-0.52F + 0.0 * -0.6F), -0.72F);
			ci.cancel();
		}
	}
	@Override
	public void applyItemBlockTransform2(PoseStack poseStack, HumanoidArm humanoidArm) {
		int reverse = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
		poseStack.translate(reverse * -0.14142136F, 0.08F, 0.14142136F);
		poseStack.mulPose(Axis.XP.rotationDegrees(-102.25F));
		poseStack.mulPose(Axis.YP.rotationDegrees((float)reverse * 13.365F));
		poseStack.mulPose(Axis.ZP.rotationDegrees((float)reverse * 78.05F));
	}
}
