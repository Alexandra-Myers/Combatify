package net.atlas.combatify.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableEntityData;
import net.atlas.combatify.extensions.IPlayerGameMode;
import net.atlas.combatify.networking.NetworkingHandler;
import net.atlas.combatify.util.MethodHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin implements IPlayerGameMode {

	@Shadow
	protected abstract void ensureHasSentCarriedItem();

	@Shadow
	private GameType localPlayerMode;

	@Shadow
	public abstract GameType getPlayerMode();

	@Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;resetAttackStrengthTicker()V"))
	public void redirectReset(Player instance, @Local(ordinal = 0, argsOnly = true) Entity target) {
		boolean isMiscTarget = false;
		ConfigurableEntityData configurableEntityData;
		if ((configurableEntityData = MethodHandler.forEntity(target)) != null) {
			if (configurableEntityData.isMiscEntity() != null)
				isMiscTarget = configurableEntityData.isMiscEntity();
		}
		instance.combatify$resetAttackStrengthTicker(!Combatify.CONFIG.improvedMiscEntityAttacks() || !isMiscTarget);
	}
	@Redirect(method = "stopDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetAttackStrengthTicker()V"))
	public void redirectReset2(LocalPlayer instance) {
		if(getPlayerMode() == GameType.ADVENTURE)
			return;
		instance.combatify$resetAttackStrengthTicker(false);
	}

	@Override
	public void swingInAir(Player player) {
		ensureHasSentCarriedItem();
		ClientPlayNetworking.send(new NetworkingHandler.ServerboundMissPacket());
		if (localPlayerMode != GameType.SPECTATOR) {
			player.combatify$attackAir();
			player.combatify$resetAttackStrengthTicker(false);
		}
	}
}
