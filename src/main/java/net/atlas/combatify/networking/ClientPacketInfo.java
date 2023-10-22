package net.atlas.combatify.networking;

import net.minecraftforge.network.simple.SimpleChannel;

public record ClientPacketInfo<MSG>(SimpleChannel channel, MSG message) {

}
