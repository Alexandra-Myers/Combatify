package net.atlas.combatify.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableEntityData;
import net.atlas.combatify.extensions.IPlayerGameMode;
import net.atlas.combatify.networking.NetworkingHandler;
import net.atlas.combatify.util.MethodHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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

	@Shadow
	private int destroyDelay;

	@WrapOperation(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;resetAttackStrengthTicker()V"))
	public void redirectReset(Player instance, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) Entity target) {
		boolean isMiscTarget = false;
		ConfigurableEntityData configurableEntityData;
		if ((configurableEntityData = MethodHandler.forEntity(target)) != null) {
			if (configurableEntityData.isMiscEntity() != null)
				isMiscTarget = configurableEntityData.isMiscEntity();
		}
		instance.combatify$resetAttackStrengthTicker(!Combatify.CONFIG.improvedMiscEntityAttacks() || !isMiscTarget);
	}
	@Inject(method = "startDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAbilities()Lnet/minecraft/world/entity/player/Abilities;", ordinal = 0))
	public void addReset1(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return;
		if (getPlayerMode() == GameType.ADVENTURE || getPlayerMode() == GameType.SPECTATOR) return;
		this.minecraft.player.combatify$resetAttackStrengthTicker(false);
	}
	@WrapOperation(method = "stopDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetAttackStrengthTicker()V"))
	public void removeReset(LocalPlayer instance, Operation<Void> original) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) original.call(instance);
	}
	@Inject(method = "continueDestroyBlock", at = @At(value = "FIELD", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;destroyDelay:I", ordinal = 0))
	public void addReset0(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) return;
		if (destroyDelay > 0) return;
		if (getPlayerMode() == GameType.ADVENTURE || getPlayerMode() == GameType.SPECTATOR) return;
		this.minecraft.player.combatify$resetAttackStrengthTicker(false);
	}

	@Override
	public void combatify$swingInAir(Player player) {
		if (Combatify.getState().equals(Combatify.CombatifyState.VANILLA)) {
			player.combatify$resetAttackStrengthTicker(false);
			return;
		}
		ensureHasSentCarriedItem();
		ClientPlayNetworking.send(new NetworkingHandler.ServerboundMissPacket());
		if (localPlayerMode != GameType.SPECTATOR) {
			player.combatify$attackAir();
			player.combatify$resetAttackStrengthTicker(false);
		}
	}
}
