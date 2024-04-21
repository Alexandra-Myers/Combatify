package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.extensions.*;
import net.atlas.combatify.util.ClientMethodHandler;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import net.atlas.combatify.screen.ScreenBuilder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements IMinecraft {
	@Shadow
	@Final
	public Options options;

	@Shadow
	@Nullable
	public LocalPlayer player;
	@Unique
	public boolean retainAttack;

	@Shadow
	@Nullable
	public HitResult hitResult;
	@Shadow
	private int rightClickDelay;
	@Shadow
	@Final
	private static Logger LOGGER;

	@Shadow
	@Nullable
	public MultiPlayerGameMode gameMode;

	@Shadow
	@Nullable
	public ClientLevel level;

	@Shadow
	protected abstract boolean startAttack();

	@Shadow
	public int missTime;

	@Shadow
	@Nullable
	public Screen screen;

	@Shadow
	public abstract void startUseItem();

	@Shadow
	@Final
	public MouseHandler mouseHandler;

	@Shadow
	public abstract void setScreen(@Nullable Screen screen);

	@Unique
	KeyMapping openCookeyModMenu;

	@Inject(method = "<init>", at = @At("TAIL"))
	public void initialize(GameConfig gameConfig, CallbackInfo ci) {
		openCookeyModMenu = CombatifyClient.getInstance().getKeybinds().openOptions();
	}

	@Inject(method = "tick", at = @At("TAIL"))
	public void openMenuOnKeyPress(CallbackInfo ci) {
		if (openCookeyModMenu.isDown() && this.screen == null)
			setScreen(ScreenBuilder.buildConfig(null));
	}

	@Inject(method = "tick", at = @At(value = "TAIL"))
	public void injectSomething(CallbackInfo ci) {
		if (screen != null)
			this.retainAttack = false;
	}
	@ModifyExpressionValue(method = "handleKeybinds",
			slice = @Slice(
					from = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z", ordinal = 0)
			),
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 0))
	public boolean allowBlockHitting(boolean original) {
		if (!original) return false;
		if (player != null) {
			ItemExtensions item = ((ItemExtensions) player.getUseItem().getItem());
			boolean bl = item.getBlockingType().canBlockHit() && !item.getBlockingType().isEmpty();
			if (bl && ((PlayerExtensions) this.player).isAttackAvailable(0.0F))
				if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK)
					startAttack();
			return bl;
		}
		return true;
	}
	@Inject(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 2))
	public void checkIfCrouch(CallbackInfo ci) {
		if (player != null) {
			ItemExtensions item = (ItemExtensions) MethodHandler.getBlockingItem(player).getItem();
			if (Combatify.CONFIG.canInteractWhenCrouchShield() && ((PlayerExtensions) player).hasEnabledShieldOnCrouch() && (player.isCrouching() || player.isPassenger()) && item.getBlockingType().canCrouchBlock() && !item.getBlockingType().isEmpty()) {
				while (options.keyUse.consumeClick()) {
					startUseItem();
				}
				while (options.keyAttack.consumeClick()) {
					startAttack();
				}
			}
		}
	}
	@WrapOperation(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V"))
	public void checkIfCrouch(MultiPlayerGameMode instance, Player player, Operation<Void> original) {
		Item blockingItem = MethodHandler.getBlockingItem(player).getItem();
		boolean bl = Combatify.CONFIG.shieldOnlyWhenCharged() && player.getAttackStrengthScale(1.0F) < Combatify.CONFIG.shieldChargePercentage() / 100F && ((ItemExtensions)blockingItem).getBlockingType().requireFullCharge();
		if(!((PlayerExtensions) player).hasEnabledShieldOnCrouch() || (!player.isCrouching() && !player.isPassenger()) || !((ItemExtensions) blockingItem).getBlockingType().canCrouchBlock() || ((ItemExtensions) blockingItem).getBlockingType().isEmpty() || bl || (!player.onGround() && !player.isPassenger()))
			original.call(instance, player);
	}
	@ModifyExpressionValue(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 4))
	public boolean redirectContinue(boolean original) {
		return original || retainAttack;
	}
	@WrapOperation(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;startAttack()Z"))
	public boolean redirectAttack(Minecraft instance, Operation<Boolean> original) {
		if (hitResult != null)
			ClientMethodHandler.redirectResult(hitResult);
		if (player == null)
			return original.call(instance);
		if (!((PlayerExtensions) player).isAttackAvailable(0.0F)) {
			if (hitResult.getType() != HitResult.Type.BLOCK) {
				float var1 = this.player.getAttackStrengthScale(0.0F);
				if (var1 < 0.8F)
					return false;

				if (var1 < 1.0F) {
					this.retainAttack = true;
					return false;
				}
			}
		}
		return original.call(instance);
	}
	@Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
	private void startAttack(CallbackInfoReturnable<Boolean> cir) {
		this.retainAttack = false;
	}
	@SuppressWarnings("unused")
	@ModifyExpressionValue(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasMissTime()Z"))
	public boolean removeMissTime(boolean original) {
		if (Combatify.CONFIG.hasMissTime())
			return original;
		return false;
	}
	@WrapOperation(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetAttackStrengthTicker()V"))
	public void redirectReset(LocalPlayer instance, Operation<Void> original) {
		if (gameMode != null)
			((IPlayerGameMode) gameMode).swingInAir(instance);
	}
	@Unique
	@Override
	public final void combatify$startUseItem(InteractionHand interactionHand) {
		if (gameMode != null && !gameMode.isDestroying()) {
			this.rightClickDelay = 4;
			if (player != null && !this.player.isHandsBusy()) {
				if (this.hitResult == null) {
					LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");
				}
				ItemStack itemStack = this.player.getItemInHand(interactionHand);
				if (!itemStack.isEmpty())
					this.gameMode.useItem(this.player, interactionHand);
			}
		}
	}

	@Inject(method = "continueAttack", at = @At(value = "HEAD"), cancellable = true)
	private void continueAttack(boolean bl, CallbackInfo ci) {
		ClientMethodHandler.redirectResult(hitResult);
		boolean bl1 = this.screen == null && (this.options.keyAttack.isDown() || this.retainAttack) && this.mouseHandler.isMouseGrabbed();
		boolean bl2 = (((IOptions) options).autoAttack().get() && Combatify.CONFIG.autoAttackAllowed()) || this.retainAttack;
		if (missTime <= 0) {
			if (player != null && !this.player.isUsingItem()) {
				boolean canAutoAttack = !Combatify.CONFIG.canAttackEarly() ? ((PlayerExtensions) this.player).isAttackAvailable(-1.0F) : this.player.getAttackStrengthScale(-1.0F) >= 1.0F;
				if (bl1 && this.hitResult != null && this.hitResult.getType() == HitResult.Type.BLOCK) {
					this.retainAttack = false;
				} else if (bl1 && canAutoAttack && bl2) {
					this.startAttack();
					ci.cancel();
				}
			}
		}
	}
	@WrapOperation(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;useItemOn(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"))
	public InteractionResult addRequirement(MultiPlayerGameMode instance, LocalPlayer localPlayer, InteractionHand interactionHand, BlockHitResult blockHitResult, Operation<InteractionResult> original, @Local ItemStack stack) {
		if(Combatify.CONFIG.shieldOnlyWhenCharged() && localPlayer.getAttackStrengthScale(1.0F) < Combatify.CONFIG.shieldChargePercentage() / 100F && ((ItemExtensions) stack.getItem()).getBlockingType().requireFullCharge())
			return InteractionResult.PASS;
		return original.call(instance, localPlayer, interactionHand, blockHitResult);
	}
	@WrapOperation(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;useItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
	public InteractionResult addRequirement1(MultiPlayerGameMode instance, Player player, InteractionHand interactionHand, Operation<InteractionResult> original, @Local ItemStack stack) {
		if(Combatify.CONFIG.shieldOnlyWhenCharged() && player.getAttackStrengthScale(1.0F) < Combatify.CONFIG.shieldChargePercentage() / 100F && ((ItemExtensions) stack.getItem()).getBlockingType().requireFullCharge())
			return InteractionResult.PASS;
		return original.call(instance, player, interactionHand);
	}
}
