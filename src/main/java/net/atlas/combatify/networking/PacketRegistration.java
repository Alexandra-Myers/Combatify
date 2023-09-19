package net.atlas.combatify.networking;

import commonnetwork.api.Network;

public class PacketRegistration {

	public void init() {
		Network
			.registerPacket(ItemConfigPacket.CHANNEL, ItemConfigPacket.class, ItemConfigPacket::encode, ItemConfigPacket::decode, ItemConfigPacket::handle);
		Network
			.registerPacket(ServerboundMissPacket.CHANNEL, ServerboundMissPacket.class, ServerboundMissPacket::encode, ServerboundMissPacket::decode, ServerboundMissPacket::handle);
		Network
			.registerPacket(ClientboundResponsePacket.CHANNEL, ClientboundResponsePacket.class, ClientboundResponsePacket::encode, ClientboundResponsePacket::decode, ClientboundResponsePacket::handle);
		Network
			.registerPacket(S2CConfigPacket.CHANNEL, S2CConfigPacket.class, S2CConfigPacket::encode, S2CConfigPacket::decode, S2CConfigPacket::handle);
		Network
			.registerPacket(C2SConfigPacket.CHANNEL, C2SConfigPacket.class, C2SConfigPacket::encode, C2SConfigPacket::decode, C2SConfigPacket::handle);
	}
}
