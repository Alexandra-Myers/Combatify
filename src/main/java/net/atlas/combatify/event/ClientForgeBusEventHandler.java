package net.atlas.combatify.event;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.networking.ClientPacketInfo;
import net.atlas.combatify.util.ArrayListExtensions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

@Mod.EventBusSubscriber(modid = Combatify.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeBusEventHandler {
	private static final ArrayListExtensions<ClientPacketInfo<?>> scheduledPackets = new ArrayListExtensions<>();
	@SubscribeEvent
	public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
		Combatify.CONFIG.options.forEach((s, synchableOption) -> synchableOption.restore());
	}
	@SubscribeEvent
	public static void clientJoin(ClientPlayerNetworkEvent.LoggingIn event) {
		scheduledPackets.forEach(packet -> packet.channel().sendTo(packet.message(), event.getConnection(), NetworkDirection.PLAY_TO_SERVER));
	}
	public static void schedulePacket(ClientPacketInfo<?> info) {
		scheduledPackets.add(info);
	}
}
