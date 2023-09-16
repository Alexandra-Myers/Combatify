package net.atlas.combatify.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.IPlayerGameMode;
import net.atlas.combatify.extensions.PlayerExtensions;
import net.atlas.combatify.networking.NewServerboundInteractPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
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
	@Final
	private ClientPacketListener connection;

	@Shadow
	private GameType localPlayerMode;

	@Shadow
	public abstract GameType getPlayerMode();

	@Shadow
	@Final
	private Minecraft minecraft;

	@SuppressWarnings("unused")
	@ModifyExpressionValue(
		method = "getPickRange", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getBlockReach()D"))
	private float getActualReachDistance(final float original) {
		if (minecraft.player != null) {
			float mod = original - 2.5F;
			return (float) ((PlayerExtensions)minecraft.player).getCurrentAttackReach(0.0F) + mod;
		}
		return original;
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
		((PlayerExtensions)instance).resetAttackStrengthTicker(!Combatify.CONFIG.improvedMiscEntityAttacks.get() || !isMiscTarget);
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
		connection.send(NewServerboundInteractPacket.createMissPacket(player.getId(), player.isShiftKeyDown()));
		if (localPlayerMode != GameType.SPECTATOR) {
			((PlayerExtensions)player).attackAir();
			((PlayerExtensions)player).resetAttackStrengthTicker(false);
		}
	}
}
