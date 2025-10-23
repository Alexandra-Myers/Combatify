package net.atlas.combatify.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
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
	@ModifyReturnValue(method = "getPickRange", at = @At(value = "RETURN"))
	private float getActualReachDistance(float reachDistance) {
		if (localPlayerMode.isCreative())
			reachDistance -= 0.5F;
		if (minecraft.player != null && MethodHandler.getCurrentAttackReach(minecraft.player, 0.0F) > reachDistance)
			return (float) MethodHandler.getCurrentAttackReach(minecraft.player, 0.0F);
		return reachDistance;
	}

	@Inject(method = "hasFarPickRange", at = @At(value = "RETURN"), cancellable = true)
	public void hasFarPickRange(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}

	@WrapOperation(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;resetAttackStrengthTicker()V"))
	public void redirectReset(Player instance, Operation<Void> original, @Local(ordinal = 0) Entity target) {
		boolean isMiscTarget = target.getType().equals(EntityType.END_CRYSTAL)
			|| target.getType().equals(EntityType.ITEM_FRAME)
			|| target.getType().equals(EntityType.GLOW_ITEM_FRAME)
			|| target.getType().equals(EntityType.PAINTING)
			|| target instanceof ArmorStand
			|| target instanceof Boat
			|| target instanceof AbstractMinecart
			|| target instanceof Interaction;
		((PlayerExtensions)instance).combatify$resetAttackStrengthTicker(!Combatify.CONFIG.improvedMiscEntityAttacks() || !isMiscTarget);
	}
	@WrapOperation(method = "stopDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetAttackStrengthTicker()V"))
	public void redirectReset2(LocalPlayer instance, Operation<Void> original) {
		if(getPlayerMode() == GameType.ADVENTURE)
			return;
		((PlayerExtensions)instance).combatify$resetAttackStrengthTicker(true);
	}

	@Override
	public void combatify$swingInAir(Player player) {
		ensureHasSentCarriedItem();
		ClientPlayNetworking.send(new NetworkingHandler.ServerboundMissPacket());
		if (localPlayerMode != GameType.SPECTATOR) {
			((PlayerExtensions)player).combatify$attackAir();
			((PlayerExtensions)player).combatify$resetAttackStrengthTicker(false);
		}
	}
}
