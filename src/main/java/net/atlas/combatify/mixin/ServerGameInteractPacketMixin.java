package net.atlas.combatify.mixin;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.IHandler;
import net.atlas.combatify.extensions.PlayerExtensions;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net/minecraft/server/network/ServerGamePacketListenerImpl$1")
public abstract class ServerGameInteractPacketMixin implements IHandler {
	@Override
	public void onMissAttack() {
		((PlayerExtensions) Combatify.player).attackAir();
	}
}
