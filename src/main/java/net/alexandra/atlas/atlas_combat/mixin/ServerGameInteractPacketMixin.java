package net.alexandra.atlas.atlas_combat.mixin;

import net.alexandra.atlas.atlas_combat.AtlasCombat;
import net.alexandra.atlas.atlas_combat.extensions.IHandler;
import net.alexandra.atlas.atlas_combat.extensions.PlayerExtensions;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net/minecraft/server/network/ServerGamePacketListenerImpl$1")
public abstract class ServerGameInteractPacketMixin implements IHandler {
	@Override
	public void onMissAttack() {
		var listener = ServerGamePacketListenerImpl.class.cast(this);
		((PlayerExtensions) listener.player).attackAir();
	}
}
