package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.IPlayerGameMode;
import net.atlas.combatify.extensions.PlayerExtensions;
import net.atlas.combatify.networking.NetworkingHandler;
import net.atlas.combatify.util.MethodHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin implements IPlayerGameMode {

	@Shadow
	protected abstract void ensureHasSentCarriedItem();

	@Shadow
	private GameType localPlayerMode;

	@Shadow
	public abstract GameType getPlayerMode();

	@Shadow
	@Final
	private Minecraft minecraft;

	@SuppressWarnings("unused")
	@ModifyExpressionValue(
		method = "getPickRange",
		require = 2, allow = 2, at = { @At(value = "CONSTANT", args = "floatValue=5.0F"), @At(value = "CONSTANT", args = "floatValue=4.5F") })
	private float getActualReachDistance(final float reachDistance) {
		if (minecraft.player != null) {
			return (float) MethodHandler.getCurrentAttackReach(minecraft.player, 0.0F) + 2;
		}
		return 4.5F;
	}

	@Inject(method = "hasFarPickRange", at = @At(value = "RETURN"), cancellable = true)
	public void hasFarPickRange(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}

	@Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;resetAttackStrengthTicker()V"))
	public void redirectReset(Player instance, @Local(ordinal = 0) Entity target) {
		boolean isMiscTarget = target.getType().equals(EntityType.END_CRYSTAL)
			|| target.getType().equals(EntityType.ITEM_FRAME)
			|| target.getType().equals(EntityType.GLOW_ITEM_FRAME)
			|| target.getType().equals(EntityType.PAINTING);
		((PlayerExtensions)instance).resetAttackStrengthTicker(!Combatify.CONFIG.improvedMiscEntityAttacks() || !isMiscTarget);
	}
	@Redirect(method = "stopDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetAttackStrengthTicker()V"))
	public void redirectReset2(LocalPlayer instance) {
		if(getPlayerMode() == GameType.ADVENTURE)
			return;
		((PlayerExtensions)instance).resetAttackStrengthTicker(true);
	}

	@Override
	public void swingInAir(Player player) {
		ensureHasSentCarriedItem();
		ClientPlayNetworking.send(new NetworkingHandler.ServerboundMissPacket());
		if (localPlayerMode != GameType.SPECTATOR) {
			((PlayerExtensions)player).attackAir();
			((PlayerExtensions)player).resetAttackStrengthTicker(false);
		}
	}
}
