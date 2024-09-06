package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.CookeyMod;
import net.atlas.combatify.extensions.*;
import net.atlas.combatify.screen.ScreenBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
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
	@Final
	public MouseHandler mouseHandler;

	@Shadow
	public abstract void setScreen(@Nullable Screen screen);

	@Inject(method = "tick", at = @At("TAIL"))
	public void openMenuOnKeyPress(CallbackInfo ci) {
		if (CookeyMod.getKeybinds().openOptions().isDown() && this.screen == null)
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
	@ModifyExpressionValue(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
	public boolean checkIfCrouch(boolean original) {
		if (player != null && !Combatify.CONFIG.canInteractWhenCrouchShield()) {
			original |= player.isBlocking();
		}
		return original;
	}
	@ModifyExpressionValue(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 4))
	public boolean redirectContinue(boolean original) {
		return original || retainAttack;
	}
	@WrapOperation(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;startAttack()Z"))
	public boolean redirectAttack(Minecraft instance, Operation<Boolean> original) {
		if (player == null || hitResult == null)
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
	@ModifyExpressionValue(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z", ordinal = 0))
	public boolean ensureNotReachingAround(boolean original) {
        if (original) return true;
        assert this.hitResult != null;
        return ((BlockHitResultExtensions)this.hitResult).isLedgeEdge();
	}
	@WrapOperation(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetAttackStrengthTicker()V"))
	public void redirectReset(LocalPlayer instance, Operation<Void> original) {
		if (gameMode != null)
			((IPlayerGameMode) gameMode).swingInAir(instance);
	}

	@Inject(method = "continueAttack", at = @At(value = "HEAD"), cancellable = true)
	private void continueAttack(boolean bl, CallbackInfo ci) {
		boolean bl1 = this.screen == null && (this.options.keyAttack.isDown() || this.retainAttack) && this.mouseHandler.isMouseGrabbed();
		boolean bl2 = (CombatifyClient.autoAttack.get() && Combatify.CONFIG.autoAttackAllowed()) || this.retainAttack;
		if (player != null && missTime <= 0) {
			boolean cannotPerform = this.player.isUsingItem() || (!Combatify.CONFIG.canInteractWhenCrouchShield() && player.isBlocking());
			if (!cannotPerform) {
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
	@ModifyExpressionValue(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z", ordinal = 0))
	public boolean ensureNotReachingAroundContinue(boolean original) {
		if (original) return true;
		assert this.hitResult != null;
		return ((BlockHitResultExtensions)this.hitResult).isLedgeEdge();
	}
	@ModifyExpressionValue(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
	public boolean alterResult(boolean original) {
		if (player != null && !Combatify.CONFIG.canInteractWhenCrouchShield()) {
			original |= player.isBlocking();
		}
		return original;
	}
}
