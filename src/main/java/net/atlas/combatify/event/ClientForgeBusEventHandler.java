package net.atlas.combatify.event;

import net.atlas.combatify.Combatify;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Combatify.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeBusEventHandler {
	@SubscribeEvent
	public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
		Combatify.CONFIG.options.forEach((s, synchableOption) -> synchableOption.restore());
	}
}
