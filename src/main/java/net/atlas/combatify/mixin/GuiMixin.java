package net.atlas.combatify.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ShieldIndicatorStatus;
import net.atlas.combatify.extensions.*;
import net.atlas.combatify.util.ClientMethodHandler;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
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
	@Unique
	private static final ResourceLocation CROSSHAIR_SHIELD_INDICATOR_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/crosshair_shield_indicator_full");
	@Unique
	private static final ResourceLocation CROSSHAIR_SHIELD_INDICATOR_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("hud/crosshair_shield_indicator_disabled");
	@Unique
	private static final ResourceLocation HOTBAR_ATTACK_INDICATOR_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_attack_indicator_full");
	@Unique
	private static final ResourceLocation HOTBAR_SHIELD_INDICATOR_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_shield_indicator_full");
	@Unique
	private static final ResourceLocation HOTBAR_SHIELD_INDICATOR_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_shield_indicator_disabled");
	@Final
	@Shadow
	private static ResourceLocation CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE;
	@Final
	@Shadow
	private static ResourceLocation CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE;
	@Final
	@Shadow
	private static ResourceLocation CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE;
	@Final
	@Shadow
	private static ResourceLocation HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE;
	@Final
	@Shadow
	private static ResourceLocation HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE;
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	protected abstract boolean canRenderCrosshairForSpectator(HitResult hitResult);
	@Shadow
	@Final
	private DebugScreenOverlay debugOverlay;

	@Inject(method = "renderCrosshair", at = @At(value = "HEAD"))
	private void renderCrosshair(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		Options options = this.minecraft.options;
		ClientMethodHandler.redirectResult(minecraft.hitResult);
		if (options.getCameraType().isFirstPerson()) {
			assert minecraft.gameMode != null;
			assert minecraft.player != null;
			if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR || this.canRenderCrosshairForSpectator(this.minecraft.hitResult)) {
				boolean bl = this.debugOverlay.showDebugScreen() && !options.hideGui && !this.minecraft.player.isReducedDebugInfo() && !(Boolean)options.reducedDebugInfo().get();
				if (!bl) {
					int j = guiGraphics.guiHeight() / 2 - 7 + 16;
					int k = guiGraphics.guiWidth() / 2 - 8;
					boolean isShieldCooldown = isShieldOnCooldown();
					boolean var7 = ((IOptions) this.minecraft.options).shieldIndicator().get() == ShieldIndicatorStatus.CROSSHAIR && !isShieldDelayed();
					if (var7 && isShieldCooldown)
						guiGraphics.blitSprite(CROSSHAIR_SHIELD_INDICATOR_DISABLED_SPRITE, k, j, 16, 16);
					else if (var7 && this.minecraft.player.isBlocking())
						guiGraphics.blitSprite(CROSSHAIR_SHIELD_INDICATOR_FULL_SPRITE, k, j, 16, 16);
				}
			}
		}
	}
	@Inject(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"), cancellable = true)
	public void renderCrosshair1(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		boolean isShieldCooldown = isShieldOnCooldown();
		boolean var7 = ((IOptions)this.minecraft.options).shieldIndicator().get() == ShieldIndicatorStatus.CROSSHAIR && !isShieldDelayed();
		assert minecraft.player != null;
		if(var7 && isShieldCooldown) {
			RenderSystem.defaultBlendFunc();
			RenderSystem.disableBlend();
			ci.cancel();
			return;
		} else if(var7 && this.minecraft.player.isBlocking()) {
			RenderSystem.defaultBlendFunc();
			RenderSystem.disableBlend();
			ci.cancel();
			return;
		}
        float maxIndicator = Math.min(((IOptions)minecraft.options).attackIndicatorMaxValue().get().floatValue(), Combatify.CONFIG.chargedAttacks() ? 2 : 1);
		float minIndicator = Math.min(((IOptions)minecraft.options).attackIndicatorMinValue().get().floatValue(), Combatify.CONFIG.chargedAttacks() ? 2 : 1);
		float attackStrengthScale = this.minecraft.player.getAttackStrengthScale(0.0F);
		boolean bl = false;
		EntityHitResult hitResult = minecraft.hitResult instanceof EntityHitResult ? (EntityHitResult) minecraft.hitResult : null;
		minecraft.crosshairPickEntity = hitResult != null ? hitResult.getEntity() : minecraft.crosshairPickEntity;
		if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && attackStrengthScale >= maxIndicator)
			bl = this.minecraft.crosshairPickEntity.isAlive();
		int j = guiGraphics.guiHeight() / 2 - 7 + 16;
		int k = guiGraphics.guiWidth() / 2 - 8;
		if (bl)
			guiGraphics.blitSprite(CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE, k, j, 16, 16);
		else if (attackStrengthScale > minIndicator && attackStrengthScale < maxIndicator) {
			int height = (int)((attackStrengthScale - minIndicator) / (maxIndicator - minIndicator + 0.00000005F) * 17.0F);
			guiGraphics.blitSprite(CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE, k, j, 16, 4);
			guiGraphics.blitSprite(CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE, 16, 4, 0, 0, k, j, height, 4);
		}
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableBlend();
		ci.cancel();
	}
	@Inject(method = "renderItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
	private void renderHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci, Player player, ItemStack itemStack, HumanoidArm humanoidArm, int i) {
		boolean isShieldCooldown = isShieldOnCooldown();
		boolean var7 = ((IOptions)this.minecraft.options).shieldIndicator().get() == ShieldIndicatorStatus.HOTBAR && !isShieldDelayed();
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
		ClientMethodHandler.redirectResult(minecraft.hitResult);
		int n = guiGraphics.guiHeight() - 20;
		int o = i + 91 + 6;
		if (humanoidArm == HumanoidArm.RIGHT)
			o = i - 91 - 22;
		float maxIndicator = Math.min(((IOptions)minecraft.options).attackIndicatorMaxValue().get().floatValue(), Combatify.CONFIG.chargedAttacks() ? 2 : 1);
		float minIndicator = Math.min(((IOptions)minecraft.options).attackIndicatorMinValue().get().floatValue(), Combatify.CONFIG.chargedAttacks() ? 2 : 1);
		float attackStrengthScale = this.minecraft.player.getAttackStrengthScale(0.0F);
		boolean bl = false;
		EntityHitResult hitResult = minecraft.hitResult instanceof EntityHitResult ? (EntityHitResult) minecraft.hitResult : null;
		minecraft.crosshairPickEntity = hitResult != null ? hitResult.getEntity() : minecraft.crosshairPickEntity;
		if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && attackStrengthScale >= maxIndicator)
			bl = this.minecraft.crosshairPickEntity.isAlive();
		if (bl)
			guiGraphics.blitSprite(HOTBAR_ATTACK_INDICATOR_FULL_SPRITE, o, n, 18, 18);
		else if (attackStrengthScale > minIndicator && attackStrengthScale < maxIndicator) {
			int height = (int)((attackStrengthScale - minIndicator) / (maxIndicator - minIndicator + 0.00000005F) * 19.0F);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			guiGraphics.blitSprite(HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE, o, n, 18, 18);
			guiGraphics.blitSprite(HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE, 18, 18, 0, 18 - height, o, n + 18 - height, 18, height);
		}

		RenderSystem.disableBlend();
		ci.cancel();
	}
	@Inject(method = "renderItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void renderHotbar1(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci, Player player, ItemStack itemStack, HumanoidArm humanoidArm, int i) {
		int n = guiGraphics.guiHeight() - 20;
		int o = i + 91 + 6;
		assert minecraft.player != null;
		if (humanoidArm == HumanoidArm.RIGHT)
			o = i - 91 - 22;
		boolean var7 = ((IOptions)this.minecraft.options).shieldIndicator().get() == ShieldIndicatorStatus.HOTBAR && !isShieldDelayed();
		boolean isShieldCooldown = isShieldOnCooldown();
		if (var7 && isShieldCooldown)
			guiGraphics.blitSprite(HOTBAR_SHIELD_INDICATOR_DISABLED_SPRITE, o, n, 18, 18);
		else if (var7 && this.minecraft.player.isBlocking())
			guiGraphics.blitSprite(HOTBAR_SHIELD_INDICATOR_FULL_SPRITE, o, n, 18, 18);
	}
	@Unique
	public boolean isShieldOnCooldown() {
		assert minecraft.player != null;
		ItemStack offHandStack = this.minecraft.player.getItemInHand(InteractionHand.OFF_HAND);
		ItemStack mainHandStack = this.minecraft.player.getItemInHand(InteractionHand.MAIN_HAND);
		boolean offHandShieldCooldown = this.minecraft.player.getCooldowns().isOnCooldown(offHandStack.getItem()) && !((ItemExtensions)offHandStack.getItem()).getBlockingType().isEmpty();
		boolean mainHandShieldCooldown = this.minecraft.player.getCooldowns().isOnCooldown(mainHandStack.getItem()) && !((ItemExtensions)mainHandStack.getItem()).getBlockingType().isEmpty();
		return offHandShieldCooldown || mainHandShieldCooldown;
	}
	@Unique
	public boolean isShieldDelayed() {
		if (this.minecraft.player == null)
			return false;
		ItemStack itemStack = MethodHandler.getBlockingItem(this.minecraft.player);
		ItemExtensions shieldItem = (ItemExtensions) itemStack.getItem();
		return shieldItem.getBlockingType().hasDelay() && Combatify.CONFIG.shieldDelay() > 0 && itemStack.getUseDuration(this.minecraft.player) - this.minecraft.player.getUseItemRemainingTicks() < Combatify.CONFIG.shieldDelay();
	}
}
