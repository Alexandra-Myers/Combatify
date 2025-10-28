package net.atlas.combatify.mixin.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.config.ShieldIndicatorStatus;
import net.atlas.combatify.extensions.*;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Gui.class)
public abstract class GuiMixin {
	@Shadow
	@Final
	protected Minecraft minecraft;

	@Shadow
	protected abstract boolean canRenderCrosshairForSpectator(HitResult hitResult);

	@Shadow
	protected int screenWidth;

	@Shadow
	protected int screenHeight;

	@Shadow
	@Final
	protected static ResourceLocation GUI_ICONS_LOCATION;

	@Inject(method = "renderCrosshair", at = @At(value = "HEAD"))
	private void renderCrosshair(GuiGraphics guiGraphics, CallbackInfo ci) {
		Options options = this.minecraft.options;
		if (options.getCameraType().isFirstPerson()) {
			assert minecraft.gameMode != null;
			assert minecraft.player != null;
			if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR || this.canRenderCrosshairForSpectator(this.minecraft.hitResult)) {
				boolean bl = options.renderDebug && !options.hideGui && !this.minecraft.player.isReducedDebugInfo() && !(Boolean)options.reducedDebugInfo().get();
				if (!bl) {
					RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
					int j = this.screenHeight / 2 - 7 + 16;
					int k = this.screenWidth / 2 - 8;
					boolean isShieldCooldown = isShieldOnCooldown();
					boolean var7 = CombatifyClient.shieldIndicator.get() == ShieldIndicatorStatus.CROSSHAIR;
					if (var7 && isShieldCooldown) {
						guiGraphics.blit(GUI_ICONS_LOCATION, k, j, 52, 112, 16, 16);
					} else if (var7 && this.minecraft.player.isBlocking()) {
						guiGraphics.blit(GUI_ICONS_LOCATION, k, j, 36, 112, 16, 16);
					}
					RenderSystem.defaultBlendFunc();
				}
			}
		}
	}
	@Inject(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"), cancellable = true)
	public void renderCrosshair1(GuiGraphics guiGraphics, CallbackInfo ci) {
		boolean isShieldCooldown = isShieldOnCooldown();
		boolean var7 = CombatifyClient.shieldIndicator.get() == ShieldIndicatorStatus.CROSSHAIR;
		assert minecraft.player != null;
		if(var7 && isShieldCooldown) {
			RenderSystem.defaultBlendFunc();
			ci.cancel();
			return;
		} else if(var7 && this.minecraft.player.isBlocking()) {
			RenderSystem.defaultBlendFunc();
			ci.cancel();
			return;
		}
		int j = this.screenHeight / 2 - 7 + 16;
		int k = this.screenWidth / 2 - 8;
		float minIndicator = CombatifyClient.attackIndicatorMinValue.get().floatValue();
		float maxIndicator = CombatifyClient.attackIndicatorMaxValue.get().floatValue();
		float attackStrengthScale = this.minecraft.player.getAttackStrengthScale(0.0F);
		boolean shouldPick = false;
		EntityHitResult hitResult = minecraft.hitResult instanceof EntityHitResult ? (EntityHitResult) minecraft.hitResult : null;
		minecraft.crosshairPickEntity = hitResult != null ? hitResult.getEntity() : minecraft.crosshairPickEntity;
		if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && attackStrengthScale >= maxIndicator) {
			shouldPick = this.minecraft.player.getEyePosition().distanceTo(MethodHandler.getNearestPointTo(this.minecraft.crosshairPickEntity.getBoundingBox(), this.minecraft.player.getEyePosition())) <= MethodHandler.getCurrentAttackReach(this.minecraft.player, 0.0F);
			shouldPick &= this.minecraft.crosshairPickEntity.isAlive();
		}
		if (shouldPick) {
			guiGraphics.blit(GUI_ICONS_LOCATION, k, j, 68, 94, 16, 16);
		} else if (attackStrengthScale > minIndicator && attackStrengthScale < maxIndicator) {
			int l = (int)((attackStrengthScale - minIndicator) / (maxIndicator - minIndicator + 0.00000005F) * 17.0F);
			guiGraphics.blit(GUI_ICONS_LOCATION, k, j, 36, 94, 16, 4);
			guiGraphics.blit(GUI_ICONS_LOCATION, k, j, 52, 94, l, 4);
		}
		RenderSystem.defaultBlendFunc();
		ci.cancel();
	}
	@Inject(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
	private void renderHotbar(float f, GuiGraphics guiGraphics, CallbackInfo ci, Player player, ItemStack itemStack, HumanoidArm humanoidArm, int i) {
		boolean isShieldCooldown = isShieldOnCooldown();
		boolean var7 = CombatifyClient.shieldIndicator.get() == ShieldIndicatorStatus.HOTBAR;
		assert minecraft.player != null;
		if(var7 && isShieldCooldown) {
			RenderSystem.disableBlend();
			ci.cancel();
			return;
		} else if(var7 && this.minecraft.player.isBlocking()) {
			RenderSystem.disableBlend();
			ci.cancel();
			return;
		}
		int n = this.screenHeight - 20;
		int o = i + 91 + 6;
		if (humanoidArm == HumanoidArm.RIGHT) {
			o = i - 91 - 22;
		}
		float minIndicator = CombatifyClient.attackIndicatorMinValue.get().floatValue();
		float maxIndicator = CombatifyClient.attackIndicatorMaxValue.get().floatValue();
		float attackStrengthScale = this.minecraft.player.getAttackStrengthScale(0.0F);
		boolean shouldPick = false;
		EntityHitResult hitResult = minecraft.hitResult instanceof EntityHitResult ? (EntityHitResult) minecraft.hitResult : null;
		minecraft.crosshairPickEntity = hitResult != null ? hitResult.getEntity() : minecraft.crosshairPickEntity;
		if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && attackStrengthScale >= maxIndicator) {
			shouldPick = this.minecraft.player.getEyePosition().distanceTo(MethodHandler.getNearestPointTo(this.minecraft.crosshairPickEntity.getBoundingBox(), this.minecraft.player.getEyePosition())) <= MethodHandler.getCurrentAttackReach(this.minecraft.player, 0.0F);
			shouldPick &= this.minecraft.crosshairPickEntity.isAlive();
		}
		if (shouldPick) {
			guiGraphics.blit(GUI_ICONS_LOCATION, o, n, 0, 130, 18, 18);
		} else if (attackStrengthScale > minIndicator && attackStrengthScale < maxIndicator) {
			int var16 = (int) ((attackStrengthScale - minIndicator) / (maxIndicator - minIndicator + 0.00000005F) * 19.0F);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			guiGraphics.blit(GUI_ICONS_LOCATION, o, n, 0, 94, 18, 18);
			guiGraphics.blit(GUI_ICONS_LOCATION, o, n + 18 - var16, 18, 112 - var16, 18, var16);
		}

		RenderSystem.disableBlend();
		ci.cancel();
	}
	@Inject(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void renderHotbar1(float f, GuiGraphics guiGraphics, CallbackInfo ci, Player player, ItemStack itemStack, HumanoidArm humanoidArm, int i) {
		RenderSystem.enableBlend();
		int n = this.screenHeight - 20;
		int o = i + 91 + 6;
		assert minecraft.player != null;
		if (humanoidArm == HumanoidArm.RIGHT) {
			o = i - 91 - 22;
		}
		boolean var7 = CombatifyClient.shieldIndicator.get() == ShieldIndicatorStatus.HOTBAR;
		boolean isShieldCooldown = isShieldOnCooldown();
		if (var7 && isShieldCooldown) {
			guiGraphics.blit(GUI_ICONS_LOCATION, o, n, 18, 112, 18, 18);
		} else if (var7 && this.minecraft.player.isBlocking()) {
			guiGraphics.blit(GUI_ICONS_LOCATION, o, n, 0, 112, 18, 18);
		}
		RenderSystem.disableBlend();
	}
	public boolean isShieldOnCooldown() {
		assert minecraft.player != null;
		ItemStack offHandStack = this.minecraft.player.getItemInHand(InteractionHand.OFF_HAND);
		ItemStack mainHandStack = this.minecraft.player.getItemInHand(InteractionHand.MAIN_HAND);
		boolean offHandShieldCooldown = this.minecraft.player.getCooldowns().isOnCooldown(offHandStack.getItem()) && !((ItemExtensions)offHandStack.getItem()).getBlockingType().isEmpty();
		boolean mainHandShieldCooldown = this.minecraft.player.getCooldowns().isOnCooldown(mainHandStack.getItem()) && !((ItemExtensions)mainHandStack.getItem()).getBlockingType().isEmpty();
		return offHandShieldCooldown || mainHandShieldCooldown;
	}
}
