package net.alexandra.atlas.atlas_combat.extensions;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.entity.Entity;

public interface IServerboundInteractPacket {
	ServerboundInteractPacket createAttackPacket(Entity entity, boolean playerSneaking);
}
