package net.atlas.combat_enhanced.mixin;

import net.atlas.combat_enhanced.CombatEnhanced;
import net.atlas.combat_enhanced.extensions.IHandler;
import net.atlas.combat_enhanced.extensions.PlayerExtensions;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net/minecraft/server/network/ServerGamePacketListenerImpl$1")
public abstract class ServerGameInteractPacketMixin implements IHandler {
	@Override
	public void onMissAttack() {
		((PlayerExtensions) CombatEnhanced.player).attackAir();
	}
}
