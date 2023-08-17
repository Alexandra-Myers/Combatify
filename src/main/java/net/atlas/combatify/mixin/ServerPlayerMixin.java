package net.atlas.combatify.mixin;

import net.atlas.combatify.extensions.IServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends PlayerMixin implements IServerPlayer {
	private boolean receivedAnswer = false;
	public ServerPlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}
	@Inject(method = "swing", at = @At(value = "HEAD"), cancellable = true)
	public void removeReset(InteractionHand hand, CallbackInfo ci) {
		super.swing(hand);
		ci.cancel();
	}

	@Override
	public void setReceivedAnswer(boolean bl) {
		receivedAnswer = bl;
	}
	@Override
	public boolean getReceivedAnswer() {
		return receivedAnswer;
	}
}
