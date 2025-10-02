package net.atlas.combatify.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.extensions.MinecraftExtensions;
import net.atlas.combatify.util.MethodHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
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

import static net.atlas.combatify.util.MethodHandler.getBlockingType;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements MinecraftExtensions {
	@Unique
	public boolean retainAttack;
	@Unique
	public HitResult aimAssistHitResult;
	@Unique
	public int aimAssistTicks;
	@Shadow
	@Final
	public Options options;

	@Shadow
	@Nullable
	public LocalPlayer player;

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

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V"))
	public void injectAimAssistDecrement(CallbackInfo ci) {
		if (--aimAssistTicks <= 0) this.aimAssistHitResult = null;
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
		if (!original || !Combatify.getState().equals(Combatify.CombatifyState.COMBATIFY)) return false;
		if (player != null) {
			ItemStack stack = player.getUseItem();
			boolean bl = getBlockingType(stack).canBlockHit() && !getBlockingType(stack).isEmpty();
			if (bl && this.player.combatify$isAttackAvailable(0.0F))
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
		if (missTime <= 0 && hitResult != null) {
			assert player != null;
			if (!player.combatify$isAttackAvailable(0.0F) && hitResult.getType() != HitResult.Type.BLOCK) {
				float var1 = this.player.getAttackStrengthScale(0.0F);
				if (var1 < 0.8F)
					return false;

				if (var1 < 1.0F) {
					this.retainAttack = true;
					return false;
				}
			}
		}
		return original.call(instance) && Combatify.getState().equals(Combatify.CombatifyState.VANILLA);
	}
	@WrapOperation(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;attack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)V"))
	public void addReachCheck(MultiPlayerGameMode instance, Player player, Entity entity, Operation<Void> original) {
		if (player.getEyePosition().distanceTo(MethodHandler.getNearestPointTo(entity.getBoundingBox(), player.getEyePosition())) <= MethodHandler.getCurrentAttackReach(player, 0.0F)) original.call(instance, player, entity);
		else instance.combatify$swingInAir(player);
	}
	@Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
	private void startAttack(CallbackInfoReturnable<Boolean> cir) {
		this.retainAttack = false;
	}
	@ModifyExpressionValue(method = "startAttack", slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;")), at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;"))
	public HitResult modifyHitResult(HitResult original) {
		if (this.aimAssistHitResult != null && original.getType() != HitResult.Type.ENTITY) original = this.aimAssistHitResult;
		return original;
	}
	@SuppressWarnings("unused")
	@ModifyExpressionValue(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasMissTime()Z"))
	public boolean removeMissTime(boolean original) {
		if (Combatify.CONFIG.hasMissTime() || Combatify.getState().equals(Combatify.CombatifyState.VANILLA))
			return original;
		return false;
	}
	@ModifyExpressionValue(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z", ordinal = 0))
	public boolean ensureNotReachingAround(boolean original) {
        if (original) return true;
        assert this.hitResult != null;
        return ((BlockHitResult)this.hitResult).combatify$isLedgeEdge();
	}
	@WrapOperation(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetAttackStrengthTicker()V"))
	public void redirectReset(LocalPlayer instance, Operation<Void> original) {
		if (gameMode != null)
			gameMode.combatify$swingInAir(instance);
	}

	@Inject(method = "continueAttack", at = @At(value = "HEAD"), cancellable = true)
	private void continueAttack(boolean bl, CallbackInfo ci) {
		boolean bl1 = this.screen == null && (this.options.keyAttack.isDown() || this.retainAttack) && this.mouseHandler.isMouseGrabbed();
		boolean bl2 = (CombatifyClient.autoAttack.get() && Combatify.CONFIG.autoAttackAllowed() && !Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) || this.retainAttack;
		if (player != null && missTime <= 0) {
			boolean cannotPerform = this.player.isUsingItem() || (!Combatify.CONFIG.canInteractWhenCrouchShield() && player.isBlocking());
			if (!cannotPerform) {
				boolean canAutoAttack = !Combatify.CONFIG.canAttackEarly() ? this.player.combatify$isAttackAvailable(-1.0F) : this.player.getAttackStrengthScale(-1.0F) >= 1.0F;
				if (bl1 && this.hitResult != null && this.hitResult.getType() == HitResult.Type.BLOCK && this.aimAssistHitResult == null) {
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
		return ((BlockHitResult)this.hitResult).combatify$isLedgeEdge() || this.aimAssistHitResult != null;
	}
	@ModifyExpressionValue(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
	public boolean alterResult(boolean original) {
		if (player != null && !Combatify.CONFIG.canInteractWhenCrouchShield()) {
			original |= player.isBlocking();
		}
		return original;
	}

	@Override
	public void combatify$setAimAssistHitResult(@Nullable HitResult aimAssistHitResult) {
		this.aimAssistHitResult = aimAssistHitResult;
		if (aimAssistHitResult != null) aimAssistTicks = Combatify.CONFIG.aimAssistTicks();
		else aimAssistTicks = 0;
	}
}
