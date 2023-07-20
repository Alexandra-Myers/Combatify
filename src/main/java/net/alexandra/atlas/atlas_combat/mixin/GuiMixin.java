package net.alexandra.atlas.atlas_combat.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.alexandra.atlas.atlas_combat.config.ShieldIndicatorStatus;
import net.alexandra.atlas.atlas_combat.extensions.*;
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
	private Minecraft minecraft;

	@Shadow
	protected abstract boolean canRenderCrosshairForSpectator(HitResult hitResult);

	@Shadow
	private int screenWidth;

	@Shadow
	private int screenHeight;

	@Shadow
	@Final
	private static ResourceLocation GUI_ICONS_LOCATION;

	@Inject(method = "renderCrosshair", at = @At(value = "HEAD"))
	private void renderCrosshair(GuiGraphics guiGraphics, CallbackInfo ci) {
		Options options = this.minecraft.options;
		((IMinecraft)minecraft).redirectResult(minecraft.hitResult);
		if (options.getCameraType().isFirstPerson()) {
			assert minecraft.gameMode != null;
			assert minecraft.player != null;
			if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR || this.canRenderCrosshairForSpectator(this.minecraft.hitResult)) {
				int j = this.screenHeight / 2 - 7 + 16;
				int k = this.screenWidth / 2 - 8;
				boolean isShieldCooldown = isShieldOnCooldown();
				boolean var7 = ((IOptions)this.minecraft.options).shieldIndicator().get() == ShieldIndicatorStatus.CROSSHAIR;
				if (var7 && isShieldCooldown) {
					guiGraphics.blit(GUI_ICONS_LOCATION, k, j, 52, 112, 16, 16);
				} else if (var7 && this.minecraft.player.isBlocking()) {
					guiGraphics.blit(GUI_ICONS_LOCATION, k, j, 36, 112, 16, 16);
				}
			}
		}
	}
	@Inject(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"), cancellable = true)
	public void renderCrosshair1(GuiGraphics guiGraphics, CallbackInfo ci) {
		boolean isShieldCooldown = isShieldOnCooldown();
		boolean var7 = ((IOptions)this.minecraft.options).shieldIndicator().get() == ShieldIndicatorStatus.CROSSHAIR;
		assert minecraft.player != null;
		if(var7 && isShieldCooldown) {
			ci.cancel();
			return;
		} else if(var7 && this.minecraft.player.isBlocking()) {
			ci.cancel();
			return;
		}
		int j = this.screenHeight / 2 - 7 + 16;
		int k = this.screenWidth / 2 - 8;
		Options options = this.minecraft.options;
		float maxIndicator =  ((IOptions)options).attackIndicatorValue().get().floatValue();
		float f = this.minecraft.player.getAttackStrengthScale(0.0F);
		boolean bl = false;
		EntityHitResult hitResult = minecraft.hitResult instanceof EntityHitResult ? (EntityHitResult) minecraft.hitResult : null;
		minecraft.crosshairPickEntity = hitResult != null ? hitResult.getEntity() : minecraft.crosshairPickEntity;
		if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && f >= maxIndicator) {
			bl = minecraft.crosshairPickEntity.distanceTo(minecraft.player) <= ((PlayerExtensions)minecraft.player).getAttackRange(0.0F);
			bl &= this.minecraft.crosshairPickEntity.isAlive();
		}
		if (bl) {
			guiGraphics.blit(GUI_ICONS_LOCATION, k, j, 68, 94, 16, 16);
		} else if (f > maxIndicator - 0.7 && f < maxIndicator) {
			int l = (int)((f - (maxIndicator - 0.7F)) / 0.70000005F * 17.0F);
			guiGraphics.blit(GUI_ICONS_LOCATION, k, j, 36, 94, 16, 4);
			guiGraphics.blit(GUI_ICONS_LOCATION, k, j, 52, 94, l, 4);
		}
		ci.cancel();
	}
	@Inject(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
	private void renderHotbar(float f, GuiGraphics guiGraphics, CallbackInfo ci, Player player, ItemStack itemStack, HumanoidArm humanoidArm, int i) {
		boolean isShieldCooldown = isShieldOnCooldown();
		boolean var7 = ((IOptions)this.minecraft.options).shieldIndicator().get() == ShieldIndicatorStatus.HOTBAR;
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
		((IMinecraft)minecraft).redirectResult(minecraft.hitResult);
		int n = this.screenHeight - 20;
		int o = i + 91 + 6;
		if (humanoidArm == HumanoidArm.RIGHT) {
			o = i - 91 - 22;
		}
		float maxIndicator =  ((IOptions)minecraft.options).attackIndicatorValue().get().floatValue();
		float g = this.minecraft.player.getAttackStrengthScale(0.0F);
		boolean bl = false;
		EntityHitResult hitResult = minecraft.hitResult instanceof EntityHitResult ? (EntityHitResult) minecraft.hitResult : null;
		minecraft.crosshairPickEntity = hitResult != null ? hitResult.getEntity() : minecraft.crosshairPickEntity;
		if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && g >= maxIndicator) {
			bl = minecraft.crosshairPickEntity.distanceTo(minecraft.player) <= ((PlayerExtensions)minecraft.player).getAttackRange(0.0F);
			bl &= this.minecraft.crosshairPickEntity.isAlive();
		}
		if (bl) {
			guiGraphics.blit(GUI_ICONS_LOCATION, o, n, 0, 130, 18, 18);
		} else if (g > maxIndicator - 0.7F && g < maxIndicator) {
			int var16 = (int) ((g - (maxIndicator - 0.7F)) / 0.70000005F * 19.0F);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			guiGraphics.blit(GUI_ICONS_LOCATION, o, n, 0, 94, 18, 18);
			guiGraphics.blit(GUI_ICONS_LOCATION, o, n + 18 - var16, 18, 112 - var16, 18, var16);
		}

		RenderSystem.disableBlend();
		ci.cancel();
	}
	@Inject(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void renderHotbar1(float f, GuiGraphics guiGraphics, CallbackInfo ci, Player player, ItemStack itemStack, HumanoidArm humanoidArm, int i) {
		int n = this.screenHeight - 20;
		int o = i + 91 + 6;
		assert minecraft.player != null;
		if (humanoidArm == HumanoidArm.RIGHT) {
			o = i - 91 - 22;
		}
		boolean var7 = ((IOptions)this.minecraft.options).shieldIndicator().get() == ShieldIndicatorStatus.HOTBAR;
		boolean isShieldCooldown = isShieldOnCooldown();
		if (var7 && isShieldCooldown) {
			guiGraphics.blit(GUI_ICONS_LOCATION, o, n, 18, 112, 18, 18);
		} else if (var7 && this.minecraft.player.isBlocking()) {
			guiGraphics.blit(GUI_ICONS_LOCATION, o, n, 0, 112, 18, 18);
		}
	}
	public boolean isShieldOnCooldown() {
		assert minecraft.player != null;
		ItemStack offHandStack = this.minecraft.player.getItemInHand(InteractionHand.OFF_HAND);
		ItemStack mainHandStack = this.minecraft.player.getItemInHand(InteractionHand.MAIN_HAND);
		boolean offHandShieldCooldown = offHandStack.getItem() instanceof IShieldItem && this.minecraft.player.getCooldowns().isOnCooldown(offHandStack.getItem());
		boolean mainHandShieldCooldown = mainHandStack.getItem() instanceof IShieldItem && this.minecraft.player.getCooldowns().isOnCooldown(mainHandStack.getItem());
		return offHandShieldCooldown || mainHandShieldCooldown;
	}
}
