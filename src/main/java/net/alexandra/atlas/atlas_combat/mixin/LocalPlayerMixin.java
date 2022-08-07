package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.extensions.LocalPlayerExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin implements LocalPlayerExtensions {

	@Unique
	public boolean wasShieldBlocking;

	@Inject(method = "tick", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V"))
	public void injectTick(CallbackInfo ci) {
		boolean isBlocking = ((LocalPlayer)(Object)this).isBlocking();
		if (isBlocking != this.wasShieldBlocking) {
			this.wasShieldBlocking = isBlocking;
			Minecraft.getInstance().gameRenderer.itemInHandRenderer.itemUsed(InteractionHand.OFF_HAND);
		}
	}

	//TODO: Make a setting for this
	@Override
	public boolean hasEnabledShieldOnCrouch() {return true;}
}
