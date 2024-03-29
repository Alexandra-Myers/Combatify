package net.atlas.combatify.mixin;

import net.atlas.combatify.util.PlayerData;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {
	/**
	 *  Credits to <a href="https://github.com/Blumbo/CTS-AntiCheat/tree/master">Blumbo's CTS Anti-Cheat</a>, integrated into Metis from there <br>
	 *  <h4>Licensed under MIT</h4> <br>
	 *  Creates player data when they join the server
	 */
	@Inject(at = @At("HEAD"), method = "placeNewPlayer")
	private void providePlayerData(Connection connection, ServerPlayer serverPlayer, CallbackInfo ci) {
		PlayerData.addPlayerData(serverPlayer);
	}

	/**
	 *  Credits to <a href="https://github.com/Blumbo/CTS-AntiCheat/tree/master">Blumbo's CTS Anti-Cheat</a>, integrated into Metis from there <br>
	 *  <h4>Licensed under MIT</h4> <br>
	 *  Clears player data when they exit the server
	 */
	@Inject(at = @At("TAIL"), method = "remove")
	private void clearPlayerData(ServerPlayer serverPlayer, CallbackInfo ci) {
		PlayerData.removePlayerData(serverPlayer);
	}

}
