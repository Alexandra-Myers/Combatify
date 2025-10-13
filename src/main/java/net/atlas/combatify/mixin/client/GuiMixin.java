package net.atlas.combatify.mixin.client;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.config.DualAttackIndicatorStatus;
import net.atlas.combatify.config.ShieldIndicatorStatus;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.commons.lang3.mutable.MutableInt;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static net.atlas.combatify.util.MethodHandler.getBlockingType;
import static net.atlas.combatify.util.MethodHandler.getFatigueForTime;

@Mixin(Gui.class)
public abstract class GuiMixin {
	@Unique
	private static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_LEFT_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("hud/crosshair_left_indicator_background");
	@Unique
	private static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_LEFT_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("hud/crosshair_left_indicator_progress");
	@Unique
	private static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_RIGHT_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("hud/crosshair_right_indicator_background");
	@Unique
	private static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_RIGHT_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("hud/crosshair_right_indicator_progress");
	@Unique
	private static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_SIDE_FULL_PICK_SPRITE = ResourceLocation.withDefaultNamespace("hud/crosshair_side_indicator_full_pick");
	@Unique
	private static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_SIDE_CHARGED_PICK_SPRITE = ResourceLocation.withDefaultNamespace("hud/crosshair_side_indicator_charged_pick");
	@Unique
	private static final ResourceLocation CROSSHAIR_SHIELD_INDICATOR_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/crosshair_shield_indicator_full");
	@Unique
	private static final ResourceLocation CROSSHAIR_SHIELD_INDICATOR_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("hud/crosshair_shield_indicator_disabled");
	@Unique
	private static final ResourceLocation CROSSHAIR_SPECIAL_INDICATOR_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/crosshair_special_indicator_full");
	@Unique
	private static final ResourceLocation HOTBAR_SPECIAL_INDICATOR_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_special_indicator_full");
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
					int yPos = guiGraphics.guiHeight() / 2 - 7 + 16;
					int xPos = guiGraphics.guiWidth() / 2 - 8;
					boolean isShieldCooldown = isShieldOnCooldown();
					boolean shieldIndicatorEnabled = CombatifyClient.shieldIndicator.get() == ShieldIndicatorStatus.CROSSHAIR && shieldNonDelayed();
					if (shieldIndicatorEnabled && isShieldCooldown)
						guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_SHIELD_INDICATOR_DISABLED_SPRITE, xPos, yPos, 16, 16);
					else if (shieldIndicatorEnabled && this.minecraft.player.isBlocking())
						guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_SHIELD_INDICATOR_FULL_SPRITE, xPos, yPos, 16, 16);
					MutableInt mutableYPos = new MutableInt(yPos);
					if (!options.attackIndicator().get().equals(AttackIndicatorStatus.CROSSHAIR) && CombatifyClient.projectileChargeIndicator.get().equals(AttackIndicatorStatus.CROSSHAIR)) renderProjectileChargeOnCrosshair(guiGraphics, minecraft.player, xPos, mutableYPos);
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
		int yPos = guiGraphics.guiHeight() / 2 - 7 + 16;
		int xPos = guiGraphics.guiWidth() / 2 - 8;
		MutableInt mutableYPos = new MutableInt(yPos);
		float attackStrengthScale = this.minecraft.player.getAttackStrengthScale(0.0F);
		if (CombatifyClient.dualAttackIndicator.get().isOn() && Combatify.CONFIG.chargedAttacks() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) {
			boolean shouldPick = false;
			EntityHitResult hitResult = minecraft.hitResult instanceof EntityHitResult ? (EntityHitResult) minecraft.hitResult : null;
			minecraft.crosshairPickEntity = hitResult != null ? hitResult.getEntity() : minecraft.crosshairPickEntity;
			if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && (CombatifyClient.dualAttackIndicator.get().equals(DualAttackIndicatorStatus.SIDE) ? this.minecraft.player.combatify$isAttackAvailable(0) : attackStrengthScale >= 1.0)) {
				shouldPick = this.minecraft.player.getEyePosition().distanceTo(MethodHandler.getNearestPointTo(this.minecraft.crosshairPickEntity.getBoundingBox(), this.minecraft.player.getEyePosition())) <= MethodHandler.getCurrentAttackReach(this.minecraft.player, 0.0F);
				shouldPick &= this.minecraft.crosshairPickEntity.isAlive();
			}
			if (CombatifyClient.dualAttackIndicator.get() == DualAttackIndicatorStatus.BOTTOM) {
				if (attackStrengthScale < 2) {
					if (shouldPick) guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE, xPos, mutableYPos.getAndAdd(8), 16, 16);
					else renderCrosshairProgress(guiGraphics, xPos, mutableYPos.getAndAdd(8), attackStrengthScale);
					renderCrosshairProgress(guiGraphics, xPos, mutableYPos.getAndAdd(8), (attackStrengthScale - 1.3F) / (0.70000005F));
				} else if (shouldPick) {
					double reachLimited = MethodHandler.getCurrentAttackReachWithoutChargedReach(minecraft.player);
					if (minecraft.player.getEyePosition().distanceToSqr(MethodHandler.getNearestPointTo(minecraft.crosshairPickEntity.getBoundingBox(), minecraft.player.getEyePosition())) < reachLimited * reachLimited)
						guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE, xPos, mutableYPos.getAndAdd(8), 16, 16);
					else renderCrosshairProgress(guiGraphics, xPos, mutableYPos.getAndAdd(8), 1);
					guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE, xPos, mutableYPos.getAndAdd(8), 16, 16);
				} else if (CombatifyClient.projectileChargeIndicator.get().equals(AttackIndicatorStatus.CROSSHAIR)) renderProjectileChargeOnCrosshair(guiGraphics, minecraft.player, xPos, mutableYPos);
			} else {
				HumanoidArm humanoidArm = this.minecraft.player.getMainArm();
				yPos = (guiGraphics.guiHeight() - 11) / 2;
				int crosshairYPos = (guiGraphics.guiHeight() - 15) / 2;
				int crosshairXPos = (guiGraphics.guiWidth() - 15) / 2;
				xPos = crosshairXPos - 2;
				int sideXPos = crosshairXPos + 13;
				if (attackStrengthScale < 2) {
					float chargeRatio = (attackStrengthScale - 1.3F) / (0.70000005F);
					if (shouldPick) guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_ATTACK_INDICATOR_SIDE_FULL_PICK_SPRITE, crosshairXPos, crosshairYPos, 15, 15);
					renderSideCrosshairProgress(guiGraphics, humanoidArm, xPos, yPos, attackStrengthScale, chargeRatio, true);
					renderSideCrosshairProgress(guiGraphics, humanoidArm, sideXPos, yPos, attackStrengthScale, chargeRatio, false);
				} else if (shouldPick) {
					double reachLimited = MethodHandler.getCurrentAttackReachWithoutChargedReach(minecraft.player);
					if (minecraft.player.getEyePosition().distanceToSqr(MethodHandler.getNearestPointTo(minecraft.crosshairPickEntity.getBoundingBox(), minecraft.player.getEyePosition())) < reachLimited * reachLimited) guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_ATTACK_INDICATOR_SIDE_FULL_PICK_SPRITE, crosshairXPos, crosshairYPos, 15, 15);
					else guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_ATTACK_INDICATOR_SIDE_CHARGED_PICK_SPRITE, crosshairXPos, crosshairYPos, 15, 15);
					renderSideCrosshairProgress(guiGraphics, humanoidArm, xPos, yPos, 1, 1, true);
					renderSideCrosshairProgress(guiGraphics, humanoidArm, sideXPos, yPos, 1, 1, false);
				}
				xPos = guiGraphics.guiWidth() / 2 - 8;

				if (CombatifyClient.projectileChargeIndicator.get().equals(AttackIndicatorStatus.CROSSHAIR)) renderProjectileChargeOnCrosshair(guiGraphics, minecraft.player, xPos, mutableYPos);
			}

			ci.cancel();
			return;
		}
		float maxIndicator = Math.min(CombatifyClient.attackIndicatorMaxValue.get().floatValue(), (Combatify.CONFIG.chargedAttacks() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) ? 2 : 1);
		float minIndicator = Math.min(CombatifyClient.attackIndicatorMinValue.get().floatValue(), (Combatify.CONFIG.chargedAttacks() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) ? 2 : 1);
		if (minIndicator == maxIndicator) minIndicator = 0;
		boolean shouldPick = false;
		EntityHitResult hitResult = minecraft.hitResult instanceof EntityHitResult ? (EntityHitResult) minecraft.hitResult : null;
		minecraft.crosshairPickEntity = hitResult != null ? hitResult.getEntity() : minecraft.crosshairPickEntity;
		if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && attackStrengthScale >= maxIndicator) {
			shouldPick = this.minecraft.player.getEyePosition().distanceTo(MethodHandler.getNearestPointTo(this.minecraft.crosshairPickEntity.getBoundingBox(), this.minecraft.player.getEyePosition())) <= MethodHandler.getCurrentAttackReach(this.minecraft.player, 0.0F);
			shouldPick &= this.minecraft.crosshairPickEntity.isAlive();
		}
		if (shouldPick)
			guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE, xPos, mutableYPos.getAndAdd(8), 16, 16);
		else if (attackStrengthScale > minIndicator && attackStrengthScale < maxIndicator)
			renderCrosshairProgress(guiGraphics, xPos, mutableYPos.getAndAdd(8), (attackStrengthScale - minIndicator) / (maxIndicator - minIndicator + 0.00000005F));
		else if (CombatifyClient.projectileChargeIndicator.get().equals(AttackIndicatorStatus.CROSSHAIR)) renderProjectileChargeOnCrosshair(guiGraphics, minecraft.player, xPos, mutableYPos);

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
		MutableInt mutableXPos = new MutableInt(xPos);
		int offset = humanoidArm == HumanoidArm.RIGHT ? -20 : 20;
		float attackStrengthScale = this.minecraft.player.getAttackStrengthScale(0.0F);
		if (CombatifyClient.dualAttackIndicator.get().isOn() && Combatify.CONFIG.chargedAttacks() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) {
			boolean shouldPick = false;
			EntityHitResult hitResult = minecraft.hitResult instanceof EntityHitResult ? (EntityHitResult) minecraft.hitResult : null;
			minecraft.crosshairPickEntity = hitResult != null ? hitResult.getEntity() : minecraft.crosshairPickEntity;
			if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && attackStrengthScale >= 1.0) {
				shouldPick = player.getEyePosition().distanceTo(MethodHandler.getNearestPointTo(this.minecraft.crosshairPickEntity.getBoundingBox(), player.getEyePosition())) <= MethodHandler.getCurrentAttackReach(player, 0.0F);
				shouldPick &= this.minecraft.crosshairPickEntity.isAlive();
			}
			if (attackStrengthScale < 2) {
				if (shouldPick) guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_ATTACK_INDICATOR_FULL_SPRITE, mutableXPos.getAndAdd(offset), yPos, 18, 18);
				else renderHotbarProgress(guiGraphics, mutableXPos.getAndAdd(offset), yPos, attackStrengthScale);
				renderHotbarProgress(guiGraphics, mutableXPos.getAndAdd(offset), yPos, (attackStrengthScale - 1.3F) / 0.70000005F);
			} else if (shouldPick) {
				double reachLimited = MethodHandler.getCurrentAttackReachWithoutChargedReach(player);
				if (player.getEyePosition().distanceToSqr(MethodHandler.getNearestPointTo(minecraft.crosshairPickEntity.getBoundingBox(), player.getEyePosition())) <= reachLimited * reachLimited) guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_ATTACK_INDICATOR_FULL_SPRITE, mutableXPos.getAndAdd(offset), yPos, 18, 18);
				else renderHotbarProgress(guiGraphics, mutableXPos.getAndAdd(offset), yPos, 1);
				guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_ATTACK_INDICATOR_FULL_SPRITE, mutableXPos.getAndAdd(offset), yPos, 18, 18);
			} else if (CombatifyClient.projectileChargeIndicator.get().equals(AttackIndicatorStatus.HOTBAR)) renderProjectileChargeOnHotbar(guiGraphics, player, mutableXPos, yPos, humanoidArm == HumanoidArm.RIGHT);

			ci.cancel();
			return;
		}
		float maxIndicator = Math.min(CombatifyClient.attackIndicatorMaxValue.get().floatValue(), (Combatify.CONFIG.chargedAttacks() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) ? 2 : 1);
		float minIndicator = Math.min(CombatifyClient.attackIndicatorMinValue.get().floatValue(), (Combatify.CONFIG.chargedAttacks() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) ? 2 : 1);
		if (minIndicator == maxIndicator) minIndicator = 0;
		boolean shouldPick = false;
		EntityHitResult hitResult = minecraft.hitResult instanceof EntityHitResult ? (EntityHitResult) minecraft.hitResult : null;
		minecraft.crosshairPickEntity = hitResult != null ? hitResult.getEntity() : minecraft.crosshairPickEntity;
		if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && attackStrengthScale >= maxIndicator) {
			shouldPick = player.getEyePosition().distanceTo(MethodHandler.getNearestPointTo(this.minecraft.crosshairPickEntity.getBoundingBox(), player.getEyePosition())) <= MethodHandler.getCurrentAttackReach(player, 0.0F);
			shouldPick &= this.minecraft.crosshairPickEntity.isAlive();
		}
		if (shouldPick) guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_ATTACK_INDICATOR_FULL_SPRITE, mutableXPos.getAndAdd(offset), yPos, 18, 18);
		else if (attackStrengthScale > minIndicator && attackStrengthScale < maxIndicator) renderHotbarProgress(guiGraphics, mutableXPos.getAndAdd(offset), yPos, (attackStrengthScale - minIndicator) / (maxIndicator - minIndicator + 0.00000005F));
		else if (CombatifyClient.projectileChargeIndicator.get().equals(AttackIndicatorStatus.HOTBAR)) renderProjectileChargeOnHotbar(guiGraphics, player, mutableXPos, yPos, humanoidArm == HumanoidArm.RIGHT);

		ci.cancel();
	}
	@Inject(method = "renderItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void renderHotbar1(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci, Player player, ItemStack itemStack, HumanoidArm humanoidArm, int i) {
		int yPos = guiGraphics.guiHeight() - 20;
		int xPos = i + 91 + 6;
		assert minecraft.player != null;
		if (humanoidArm == HumanoidArm.RIGHT)
			xPos = i - 91 - 22;
		boolean shieldIndicatorEnabled = CombatifyClient.shieldIndicator.get() == ShieldIndicatorStatus.HOTBAR && shieldNonDelayed();
		boolean isShieldCooldown = isShieldOnCooldown();
		if (shieldIndicatorEnabled && isShieldCooldown)
			guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_SHIELD_INDICATOR_DISABLED_SPRITE, xPos, yPos, 18, 18);
		else if (shieldIndicatorEnabled && this.minecraft.player.isBlocking())
			guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_SHIELD_INDICATOR_FULL_SPRITE, xPos, yPos, 18, 18);
		MutableInt mutableXPos = new MutableInt(xPos);
		if (!minecraft.options.attackIndicator().get().equals(AttackIndicatorStatus.HOTBAR) && CombatifyClient.projectileChargeIndicator.get().equals(AttackIndicatorStatus.HOTBAR)) renderProjectileChargeOnHotbar(guiGraphics, player, mutableXPos, yPos, humanoidArm == HumanoidArm.RIGHT);
	}
	@Unique
	private void renderProjectileChargeOnHotbar(GuiGraphics guiGraphics, Player player, MutableInt mutableXPos, int yPos, boolean right) {
		ItemStack useItem = player.getUseItem();
		int time = useItem.getUseDuration(player) - player.getUseItemRemainingTicks();
		switch (useItem.getItem()) {
			case BowItem ignored -> {
				float power = BowItem.getPowerForTime(time);
				int xPos = mutableXPos.getAndAdd(right ? -20 : 20);
				if (power < 1) renderHotbarProgress(guiGraphics, xPos, yPos, power);
				else if (getFatigueForTime(time) <= 0.5f) {
					if (Combatify.CONFIG.bowFatigue() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) {
						float fatigueProgress = 1 - ((float) (time - 20) / 40);
						renderHotbarProgress(guiGraphics, xPos, yPos, fatigueProgress);
						int indicatorXPos = xPos + 1;
						int indicatorYPos = yPos + 1;
						guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_SPECIAL_INDICATOR_FULL_SPRITE, indicatorXPos, indicatorYPos, 5, 5);
					} else guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_ATTACK_INDICATOR_FULL_SPRITE, xPos, yPos, 18, 18);
				}
			}
			case CrossbowItem ignored -> {
				float power = (float) time / CrossbowItem.getChargeDuration(useItem, player);
				int xPos = mutableXPos.getAndAdd(right ? -20 : 20);
				if (power < 1) renderHotbarProgress(guiGraphics, xPos, yPos, power);
				else guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_ATTACK_INDICATOR_FULL_SPRITE, xPos, yPos, 18, 18);
			}
			case TridentItem ignored -> {
				float power = (float) time / 10;
				int xPos = mutableXPos.getAndAdd(right ? -20 : 20);
				if (power < 1) renderHotbarProgress(guiGraphics, xPos, yPos, power);
				else guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_ATTACK_INDICATOR_FULL_SPRITE, xPos, yPos, 18, 18);
			}
			default -> {}
		}
	}
	@Unique
	private void renderProjectileChargeOnCrosshair(GuiGraphics guiGraphics, Player player, int xPos, MutableInt mutableYPos) {
		ItemStack useItem = player.getUseItem();
		int time = useItem.getUseDuration(player) - player.getUseItemRemainingTicks();
		switch (useItem.getItem()) {
			case BowItem ignored -> {
				float power = BowItem.getPowerForTime(time);
				int yPos = mutableYPos.getAndAdd(8);
				if (power < 1) renderCrosshairProgress(guiGraphics, xPos, yPos, power);
				else if (getFatigueForTime(time) <= 0.5f) {
					if (Combatify.CONFIG.bowFatigue() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) {
						float fatigueProgress = 1 - ((float) (time - 20) / 40);
						renderCrosshairProgress(guiGraphics, xPos, yPos, fatigueProgress);
						int indicatorXPos = guiGraphics.guiWidth() / 2 - 1;
						int indicatorYPos = yPos + 4;
						guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_SPECIAL_INDICATOR_FULL_SPRITE, indicatorXPos, indicatorYPos, 3, 3);
					} else guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE, xPos, yPos, 16, 16);
				}
			}
			case CrossbowItem ignored -> {
				float power = (float) time / CrossbowItem.getChargeDuration(useItem, player);
				int yPos = mutableYPos.getAndAdd(8);
				if (power < 1) renderCrosshairProgress(guiGraphics, xPos, yPos, power);
				else guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE, xPos, yPos, 16, 16);
			}
			case TridentItem ignored -> {
				float power = (float) time / 10;
				int yPos = mutableYPos.getAndAdd(8);
				if (power < 1) renderCrosshairProgress(guiGraphics, xPos, yPos, power);
				else guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE, xPos, yPos, 16, 16);
			}
			default -> {}
		}
	}
	@Unique
	private void renderSideCrosshairProgress(GuiGraphics guiGraphics, HumanoidArm humanoidArm, int xPos, int yPos, float fastRatio, float chargeRatio, boolean left) {
		float ratio = humanoidArm == (left ? HumanoidArm.RIGHT : HumanoidArm.LEFT) ? fastRatio : chargeRatio;
		int height = (int) Mth.clamp(ratio * 12.0F, 0, 11);
		guiGraphics.blitSprite(RenderType::crosshair, left ? CROSSHAIR_ATTACK_INDICATOR_LEFT_BACKGROUND_SPRITE : CROSSHAIR_ATTACK_INDICATOR_RIGHT_BACKGROUND_SPRITE, xPos, yPos, 4, 11);
		guiGraphics.blitSprite(RenderType::crosshair, left ? CROSSHAIR_ATTACK_INDICATOR_LEFT_PROGRESS_SPRITE : CROSSHAIR_ATTACK_INDICATOR_RIGHT_PROGRESS_SPRITE, 4, 11, 0, 11 - height, xPos, yPos + 11 - height, 4, height);
	}
	@Unique
	private void renderCrosshairProgress(GuiGraphics guiGraphics, int xPos, int yPos, float ratio) {
		int height = (int) Mth.clamp(ratio * 17.0F, 0, 16);
		guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE, xPos, yPos, 16, 4);
		guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE, 16, 4, 0, 0, xPos, yPos, height, 4);
	}
	@Unique
	private void renderHotbarProgress(GuiGraphics guiGraphics, int xPos, int yPos, float ratio) {
		int height = (int) Mth.clamp(ratio * 19.0F, 0, 18);
		guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE, xPos, yPos, 18, 18);
		guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE, 18, 18, 0, 18 - height, xPos, yPos + 18 - height, 18, height);
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
