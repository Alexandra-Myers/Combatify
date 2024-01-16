package net.atlas.combatify.networking;

import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import static net.atlas.combatify.Combatify.id;

public class PacketRegistration {
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel MAIN = NetworkRegistry.newSimpleChannel(
		id("combatify"),
		() -> PROTOCOL_VERSION,
		PROTOCOL_VERSION::equals,
		NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION::equals)
	);

	public void init() {
		int id = 0;
		MAIN.messageBuilder(AtlasConfigPacket.class, id).encoder(AtlasConfigPacket::encode).decoder(AtlasConfigPacket::decode).consumerMainThread(AtlasConfigPacket::handle).add();
		MAIN.messageBuilder(RemainingUseSyncPacket.class, id++).encoder(RemainingUseSyncPacket::encode).decoder(RemainingUseSyncPacket::decode).consumerMainThread(RemainingUseSyncPacket::handle).add();
		MAIN.messageBuilder(ServerboundMissPacket.class, id++).encoder(ServerboundMissPacket::encode).decoder(ServerboundMissPacket::decode).consumerMainThread(ServerboundMissPacket::handle).add();
		MAIN.messageBuilder(S2CConfigPacket.class, id++).encoder(S2CConfigPacket::encode).decoder(S2CConfigPacket::decode).consumerMainThread(S2CConfigPacket::handle).add();
		MAIN.messageBuilder(C2SConfigPacket.class, id + 1).encoder(C2SConfigPacket::encode).decoder(C2SConfigPacket::decode).consumerMainThread(C2SConfigPacket::handle).add();
	}
}
