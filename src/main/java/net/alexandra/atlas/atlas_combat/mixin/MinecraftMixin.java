package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.extensions.IMinecraft;
import net.alexandra.atlas.atlas_combat.extensions.IOptions;
import net.alexandra.atlas.atlas_combat.extensions.PlayerExtensions;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements IMinecraft {
	@Shadow
	@Final
	public Options options;

	@Shadow
	protected int missTime;

	@Shadow
	protected abstract boolean startAttack();

	@Shadow
	@Nullable
	public LocalPlayer player;

	@Shadow
	protected abstract void continueAttack(boolean b);

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
	@Final
	public GameRenderer gameRenderer;

	@Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;continueAttack(Z)V"))
	public void redirectContinueAttack(Minecraft instance, boolean b) {
		if(hitResult.getType() == HitResult.Type.BLOCK) {
			continueAttack(b);
		}else {
			if (b && ((IOptions) options).autoAttack().get()) {
				if((((PlayerExtensions)player).getMissedAttackRecovery())){
					if ((float)(((PlayerExtensions)player).getAttackStrengthStartValue() - ((float)player.attackStrengthTicker - 0.5F)) > 88.0F) {
						startAttack();
					}
				}else{
					if ((float)(((PlayerExtensions)player).getAttackStrengthStartValue() - ((float)player.attackStrengthTicker - 0.5F)) > 56.0F) {
						startAttack();
					}
				}
			} else {
				continueAttack(b);
			}
		}
	}
	@ModifyConstant(method = "startAttack", constant = @Constant(intValue = 10))
	public int redirectMissPenalty(int constant) {
		return 4;
	}
	@Inject(method = "startAttack", at = @At(value = "HEAD"), cancellable = true)
	private void injectDelay(CallbackInfoReturnable<Boolean> cir){
		assert player != null;
		for(InteractionHand hand : InteractionHand.values()) {
			if (player.isUsingItem() && player.getItemInHand(hand).getItem() instanceof ShieldItem) {
				((PlayerExtensions) player).customShieldInteractions(0.1F);
			}
		}
		if((((PlayerExtensions)player).getMissedAttackRecovery())) {
			if((float)(((PlayerExtensions)player).getAttackStrengthStartValue() - ((float)player.attackStrengthTicker - 0.5F)) < 80.0F){
				cir.setReturnValue(false);
				cir.cancel();
			}
		}else if ((((PlayerExtensions)player).getAttackStrengthStartValue() - ((float)player.attackStrengthTicker - 0.5F)) < 48.0F) {
			cir.setReturnValue(false);
			cir.cancel();
		}
	}
	@Unique
	@Override
	public final void startUseItem(InteractionHand interactionHand) {
		if (!gameMode.isDestroying()) {
			this.rightClickDelay = 4;
			if (!this.player.isHandsBusy()) {
				if (this.hitResult == null) {
					LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");
				}
					ItemStack itemStack = this.player.getItemInHand(interactionHand);
					if (!itemStack.isEmpty()) {
						InteractionResult interactionResult3 = this.gameMode.useItem(this.player, interactionHand);
						if (interactionResult3.consumesAction()) {
							if (interactionResult3.shouldSwing()) {
								this.player.swing(interactionHand);
							}
							return;
						}
					}
				}
		}
	}
}
