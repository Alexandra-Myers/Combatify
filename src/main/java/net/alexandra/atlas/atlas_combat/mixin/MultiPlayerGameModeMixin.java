package net.alexandra.atlas.atlas_combat.mixin;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.alexandra.atlas.atlas_combat.extensions.IPlayerGameMode;
import net.alexandra.atlas.atlas_combat.extensions.IServerboundInteractPacket;
import net.alexandra.atlas.atlas_combat.extensions.PlayerExtensions;
import net.alexandra.atlas.atlas_combat.item.WeaponType;
import net.alexandra.atlas.atlas_combat.networking.NewServerboundInteractPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import static net.alexandra.atlas.atlas_combat.networking.NewServerboundInteractPacket.MISS_ATTACK_ACTION;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin implements IPlayerGameMode {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	protected abstract void ensureHasSentCarriedItem();

	@Shadow
	@Final
	private ClientPacketListener connection;

	@Shadow
	private GameType localPlayerMode;

	@ModifyConstant(
			method = "getPickRange",
			require = 2, allow = 2, constant = { @Constant(floatValue = 5.0F), @Constant(floatValue = 4.5F) })
	private float getActualReachDistance(final float reachDistance) {
		if (minecraft.player != null) {
			return (float) ((PlayerExtensions)minecraft.player).getAttackRange(minecraft.player, 4.5F);
		}
		return 4.5F;
	}
	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	public boolean hasFarPickRange() {
		return false;
	}

	@Override
	public void swingInAir(Player player) {
		ensureHasSentCarriedItem();
		connection.send(NewServerboundInteractPacket.createMissPacket(player.getId(), player.isShiftKeyDown()));
		if (localPlayerMode != GameType.SPECTATOR) {
			((PlayerExtensions)player).attackAir();
		}
	}
}
