package net.atlas.combatify.mixin.client;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.config.ShieldIndicatorStatus;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static net.atlas.combatify.util.MethodHandler.getBlockingType;

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
		if (options.getCameraType().isFirstPerson()) {
			assert minecraft.gameMode != null;
			assert minecraft.player != null;
			if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR || this.canRenderCrosshairForSpectator(this.minecraft.hitResult)) {
				boolean bl = this.debugOverlay.showDebugScreen() && !this.minecraft.player.isReducedDebugInfo() && !(Boolean)options.reducedDebugInfo().get();
				if (!bl) {
					int j = guiGraphics.guiHeight() / 2 - 7 + 16;
					int k = guiGraphics.guiWidth() / 2 - 8;
					boolean isShieldCooldown = isShieldOnCooldown();
					boolean shieldIndicatorEnabled = CombatifyClient.shieldIndicator.get() == ShieldIndicatorStatus.CROSSHAIR && shieldNonDelayed();
					if (shieldIndicatorEnabled && isShieldCooldown)
						guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_SHIELD_INDICATOR_DISABLED_SPRITE, k, j, 16, 16);
					else if (shieldIndicatorEnabled && this.minecraft.player.isBlocking())
						guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_SHIELD_INDICATOR_FULL_SPRITE, k, j, 16, 16);
				}
			}
		}
	}
	@Inject(method = "renderCrosshair", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"), cancellable = true)
	public void renderCrosshair1(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		boolean isShieldCooldown = isShieldOnCooldown();
		boolean shieldIndicatorEnabled = CombatifyClient.shieldIndicator.get() == ShieldIndicatorStatus.CROSSHAIR && shieldNonDelayed();
		assert minecraft.player != null;
		if (shieldIndicatorEnabled && isShieldCooldown) {
			ci.cancel();
			return;
		} else if(shieldIndicatorEnabled && this.minecraft.player.isBlocking()) {
			ci.cancel();
			return;
		}
        float maxIndicator = Math.min(CombatifyClient.attackIndicatorMaxValue.get().floatValue(), (Combatify.CONFIG.chargedAttacks() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) ? 2 : 1);
		float minIndicator = Math.min(CombatifyClient.attackIndicatorMinValue.get().floatValue(), (Combatify.CONFIG.chargedAttacks() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) ? 2 : 1);
		if (minIndicator == maxIndicator) minIndicator = 0;
		float attackStrengthScale = this.minecraft.player.getAttackStrengthScale(0.0F);
		boolean bl = false;
		EntityHitResult hitResult = minecraft.hitResult instanceof EntityHitResult ? (EntityHitResult) minecraft.hitResult : null;
		minecraft.crosshairPickEntity = hitResult != null ? hitResult.getEntity() : minecraft.crosshairPickEntity;
		if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && attackStrengthScale >= maxIndicator)
			bl = this.minecraft.crosshairPickEntity.isAlive();
		int yPos = guiGraphics.guiHeight() / 2 - 7 + 16;
		int xPos = guiGraphics.guiWidth() / 2 - 8;
		if (bl)
			guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE, xPos, yPos, 16, 16);
		else if (CombatifyClient.dualAttackIndicator.get() && Combatify.CONFIG.chargedAttacks() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) {
			if (attackStrengthScale < 2) {
				int topHeight = (int)Math.min(attackStrengthScale * 17.0F, 17);
				int bottomHeight = (int)Math.min(Math.max(attackStrengthScale - 1.3F, 0) / (0.70000005F) * 17.0F, 17);
				int bottomYPos = yPos + 6;
				guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE, xPos, yPos, 16, 4);
				guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE, 16, 4, 0, 0, xPos, yPos, topHeight, 4);
				guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE, xPos, bottomYPos, 16, 4);
				guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE, 16, 4, 0, 0, xPos, bottomYPos, bottomHeight, 4);
			}
		} else if (attackStrengthScale > minIndicator && attackStrengthScale < maxIndicator) {
			int height = (int)((attackStrengthScale - minIndicator) / (maxIndicator - minIndicator + 0.00000005F) * 17.0F);
			guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE, xPos, yPos, 16, 4);
			guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE, 16, 4, 0, 0, xPos, yPos, height, 4);
		}
		ci.cancel();
	}
	@Inject(method = "renderItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAttackStrengthScale(F)F"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
	private void renderHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci, Player player, ItemStack itemStack, HumanoidArm humanoidArm, int i) {
		boolean isShieldCooldown = isShieldOnCooldown();
		boolean shieldIndicatorEnabled = CombatifyClient.shieldIndicator.get() == ShieldIndicatorStatus.HOTBAR && shieldNonDelayed();
		assert minecraft.player != null;
		if(shieldIndicatorEnabled && isShieldCooldown) {
			ci.cancel();
			return;
		} else if(shieldIndicatorEnabled && this.minecraft.player.isBlocking()) {
			ci.cancel();
			return;
		}
		int yPos = guiGraphics.guiHeight() - 20;
		int xPos = i + 91 + 6;
		if (humanoidArm == HumanoidArm.RIGHT)
			xPos = i - 91 - 22;
		float maxIndicator = Math.min(CombatifyClient.attackIndicatorMaxValue.get().floatValue(), (Combatify.CONFIG.chargedAttacks() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) ? 2 : 1);
		float minIndicator = Math.min(CombatifyClient.attackIndicatorMinValue.get().floatValue(), (Combatify.CONFIG.chargedAttacks() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) ? 2 : 1);
		if (minIndicator == maxIndicator) minIndicator = 0;
		float attackStrengthScale = this.minecraft.player.getAttackStrengthScale(0.0F);
		boolean bl = false;
		EntityHitResult hitResult = minecraft.hitResult instanceof EntityHitResult ? (EntityHitResult) minecraft.hitResult : null;
		minecraft.crosshairPickEntity = hitResult != null ? hitResult.getEntity() : minecraft.crosshairPickEntity;
		if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && attackStrengthScale >= maxIndicator)
			bl = this.minecraft.crosshairPickEntity.isAlive();
		if (bl)
			guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_ATTACK_INDICATOR_FULL_SPRITE, xPos, yPos, 18, 18);
		else if (CombatifyClient.dualAttackIndicator.get() && Combatify.CONFIG.chargedAttacks() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) {
			if (attackStrengthScale < 2) {
				int topHeight = (int)Math.min(attackStrengthScale * 19.0F, 18);
				int bottomHeight = (int)Math.min(Math.max(attackStrengthScale - 1.3F, 0) / 0.70000005F * 19.0F, 19);
				int bottomXPos = xPos + (humanoidArm == HumanoidArm.RIGHT ? -20 : 20);
				guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE, xPos, yPos, 18, 18);
				guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE, 18, 18, 0, 18 - topHeight, xPos, yPos + 18 - topHeight, 18, topHeight);
				guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE, bottomXPos, yPos, 18, 18);
				guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE, 18, 18, 0, 18 - bottomHeight, bottomXPos, yPos + 18 - bottomHeight, 18, bottomHeight);
			}
		} else if (attackStrengthScale > minIndicator && attackStrengthScale < maxIndicator) {
			int height = (int)((attackStrengthScale - minIndicator) / (maxIndicator - minIndicator + 0.00000005F) * 19.0F);
			guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE, xPos, yPos, 18, 18);
			guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE, 18, 18, 0, 18 - height, xPos, yPos + 18 - height, 18, height);
		}

		ci.cancel();
	}
	@Inject(method = "renderItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void renderHotbar1(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci, Player player, ItemStack itemStack, HumanoidArm humanoidArm, int i) {
		int n = guiGraphics.guiHeight() - 20;
		int o = i + 91 + 6;
		assert minecraft.player != null;
		if (humanoidArm == HumanoidArm.RIGHT)
			o = i - 91 - 22;
		boolean shieldIndicatorEnabled = CombatifyClient.shieldIndicator.get() == ShieldIndicatorStatus.HOTBAR && shieldNonDelayed();
		boolean isShieldCooldown = isShieldOnCooldown();
		if (shieldIndicatorEnabled && isShieldCooldown)
			guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_SHIELD_INDICATOR_DISABLED_SPRITE, o, n, 18, 18);
		else if (shieldIndicatorEnabled && this.minecraft.player.isBlocking())
			guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_SHIELD_INDICATOR_FULL_SPRITE, o, n, 18, 18);
	}
	@Unique
	public boolean isShieldOnCooldown() {
		assert minecraft.player != null;
		ItemStack offHandStack = this.minecraft.player.getItemInHand(InteractionHand.OFF_HAND);
		ItemStack mainHandStack = this.minecraft.player.getItemInHand(InteractionHand.MAIN_HAND);
		boolean offHandShieldCooldown = this.minecraft.player.getCooldowns().isOnCooldown(offHandStack) && !getBlockingType(offHandStack).isEmpty();
		boolean mainHandShieldCooldown = this.minecraft.player.getCooldowns().isOnCooldown(mainHandStack) && !getBlockingType(mainHandStack).isEmpty();
		return offHandShieldCooldown || mainHandShieldCooldown;
	}
	@Unique
	public boolean shieldNonDelayed() {
		if (this.minecraft.player == null)
			return true;
		ItemStack itemStack = MethodHandler.getBlockingItem(this.minecraft.player).stack();
		return !getBlockingType(itemStack).hasDelay() || Combatify.CONFIG.shieldDelay() <= 0 || itemStack.getUseDuration(this.minecraft.player) - this.minecraft.player.getUseItemRemainingTicks() >= Combatify.CONFIG.shieldDelay();
	}
}
