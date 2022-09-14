package net.alexandra.atlas.atlas_combat.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.config.ShieldIndicatorStatus;
import net.alexandra.atlas.atlas_combat.extensions.IMinecraft;
import net.alexandra.atlas.atlas_combat.extensions.IOptions;
import net.alexandra.atlas.atlas_combat.extensions.PlayerExtensions;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin extends GuiComponent {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	protected abstract boolean canRenderCrosshairForSpectator(HitResult hitResult);

	@Shadow
	private int screenWidth;

	@Shadow
	private int screenHeight;

	@Shadow
	protected abstract void renderSlot(int par1, int par2, float par3, Player par4, ItemStack par5, int par6);

	@Shadow
	protected abstract Player getCameraPlayer();

	@Shadow
	@Final
	private static ResourceLocation WIDGETS_LOCATION;

	/**
	 * @author
	 * @reason
	 */
	@Inject(method = "renderCrosshair", at = @At(value = "HEAD"), cancellable = true)
	private void renderCrosshair(PoseStack matrices, CallbackInfo ci) {
		Options options = this.minecraft.options;
		if (options.getCameraType().isFirstPerson()) {
			if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR || this.canRenderCrosshairForSpectator(this.minecraft.hitResult)) {
				if (options.renderDebug && !options.hideGui && !this.minecraft.player.isReducedDebugInfo() && !options.reducedDebugInfo().get()) {
					Camera camera = this.minecraft.gameRenderer.getMainCamera();
					PoseStack poseStack = RenderSystem.getModelViewStack();
					poseStack.pushPose();
					poseStack.translate((double)(screenWidth / 2), (double)(screenHeight / 2), (double)getBlitOffset());
					poseStack.mulPose(Vector3f.XN.rotationDegrees(camera.getXRot()));
					poseStack.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot()));
					poseStack.scale(-1.0F, -1.0F, -1.0F);
					RenderSystem.applyModelViewMatrix();
					RenderSystem.renderCrosshair(10);
					poseStack.popPose();
					RenderSystem.applyModelViewMatrix();
				} else {
					RenderSystem.blendFuncSeparate(
							GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
							GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
							GlStateManager.SourceFactor.ONE,
							GlStateManager.DestFactor.ZERO
					);
					int i = 15;
					this.blit(matrices, (this.screenWidth - 15) / 2, (this.screenHeight - 15) / 2, 0, 0, 15, 15);

					int j = this.screenHeight / 2 - 7 + 16;
					int k = this.screenWidth / 2 - 8;
					ItemStack offHandStack = this.minecraft.player.getItemInHand(InteractionHand.OFF_HAND);
					boolean var7 = ((IOptions)this.minecraft.options).shieldIndicator().get() == ShieldIndicatorStatus.CROSSHAIR;
					if (var7 && offHandStack.getItem() == Items.SHIELD && this.minecraft.player.getCooldowns().isOnCooldown(offHandStack.getItem())) {
						this.blit(matrices, k, j, 52, 112, 16, 16);
					} else if (var7 && this.minecraft.player.isBlocking()) {
						this.blit(matrices, k, j, 36, 112, 16, 16);
					}else if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.CROSSHAIR) {
						float maxIndicator =  ((IOptions)options).attackIndicatorValue().get().floatValue();
						float f = this.minecraft.player.getAttackStrengthScale(0.0F);
						boolean bl = false;
						EntityHitResult hitResult = ((IMinecraft)minecraft).rayTraceEntity(minecraft.player, 1.0F, ((PlayerExtensions)minecraft.player).getAttackRange(minecraft.player, 2.5));
						minecraft.crosshairPickEntity = hitResult != null ? hitResult.getEntity() : minecraft.crosshairPickEntity;
						if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && f >= maxIndicator) {
							bl = (minecraft.hitResult).distanceTo(this.minecraft.crosshairPickEntity) <= ((PlayerExtensions)minecraft.player).getAttackRange(minecraft.player, 2.5);
							bl &= this.minecraft.crosshairPickEntity.isAlive();
						}
						if (bl) {
							this.blit(matrices, k, j, 68, 94, 16, 16);
						} else if (f > maxIndicator - 0.7 && f < maxIndicator) {
							int l = (int)((f - (maxIndicator - 0.7F)) / 0.70000005F * 17.0F);
							this.blit(matrices, k, j, 36, 94, 16, 4);
							this.blit(matrices, k, j, 52, 94, l, 4);
						}
					}

					RenderSystem.disableBlend();

				}

			}
		}
		ci.cancel();
	}
	@Inject(method = "renderHotbar", at = @At(value = "HEAD"), cancellable = true)
	private void renderHotbar(float tickDelta, PoseStack matrices, CallbackInfo ci) {
		Player player = getCameraPlayer();
		if (player != null) {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
			ItemStack itemStack = player.getOffhandItem();
			HumanoidArm humanoidArm = player.getMainArm().getOpposite();
			int i = this.screenWidth / 2;
			int j = this.getBlitOffset();
			int k = 182;
			int l = 91;
			this.setBlitOffset(-90);
			this.blit(matrices, i - 91, this.screenHeight - 22, 0, 0, 182, 22);
			this.blit(matrices, i - 91 - 1 + player.getInventory().selected * 20, this.screenHeight - 22 - 1, 0, 22, 24, 22);
			if (!itemStack.isEmpty()) {
				if (humanoidArm == HumanoidArm.LEFT) {
					this.blit(matrices, i - 91 - 29, this.screenHeight - 23, 24, 22, 29, 24);
				} else {
					this.blit(matrices, i + 91, this.screenHeight - 23, 53, 22, 29, 24);
				}
			}

			this.setBlitOffset(j);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			int m = 1;

			int n;
			int o;
			for(n = 0; n < 9; ++n) {
				o = i - 90 + n * 20 + 2;
				int p = this.screenHeight - 16 - 3;
				renderSlot(o, p, tickDelta, player, player.getInventory().items.get(n), m++);
			}

			if (!itemStack.isEmpty()) {
				n = this.screenHeight - 16 - 3;
				if (humanoidArm == HumanoidArm.LEFT) {
					this.renderSlot(i - 91 - 26, n, tickDelta, player, itemStack, m++);
				} else {
					this.renderSlot(i + 91 + 10, n, tickDelta, player, itemStack, m++);
				}
			}

			RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
			n = this.screenHeight - 20;
			o = i + 91 + 6;
			if (humanoidArm == HumanoidArm.RIGHT) {
				o = i - 91 - 22;
			}
			boolean var7 = ((IOptions)this.minecraft.options).shieldIndicator().get() == ShieldIndicatorStatus.HOTBAR;
			if (var7 && itemStack.getItem() == Items.SHIELD && this.minecraft.player.getCooldowns().isOnCooldown(itemStack.getItem())) {
				this.blit(matrices, o, n, 18, 112, 18, 18);
			} else if (var7 && this.minecraft.player.isBlocking()) {
				this.blit(matrices, o, n, 0, 112, 18, 18);
			} else if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) {
				float maxIndicator =  ((IOptions)minecraft.options).attackIndicatorValue().get().floatValue();
				float f = this.minecraft.player.getAttackStrengthScale(0.0F);
				boolean bl = false;
				EntityHitResult hitResult = ((IMinecraft)minecraft).rayTraceEntity(minecraft.player, 1.0F, ((PlayerExtensions)minecraft.player).getAttackRange(minecraft.player, 2.5));
				minecraft.crosshairPickEntity = hitResult != null ? hitResult.getEntity() : minecraft.crosshairPickEntity;
				if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && f >= maxIndicator) {
					bl = (minecraft.hitResult).distanceTo(this.minecraft.crosshairPickEntity) <= ((PlayerExtensions)minecraft.player).getAttackRange(minecraft.player, 2.5);
					bl &= this.minecraft.crosshairPickEntity.isAlive();
				}
				if (bl) {
					this.blit(matrices, o, n, 0, 130, 18, 18);
				} else if (f > maxIndicator - 0.7F && f < maxIndicator) {

					int var16 = (int) ((f - (maxIndicator - 0.7F)) / 0.70000005F * 19.0F);
					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
					this.blit(matrices, o, n, 0, 94, 18, 18);
					this.blit(matrices, o, n + 18 - var16, 18, 112 - var16, 18, var16);
				}
			}
			RenderSystem.disableBlend();
		}
		ci.cancel();
	}
}
