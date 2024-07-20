package net.atlas.combatify.networking;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.config.ItemConfig;
import net.atlas.combatify.extensions.LivingEntityExtensions;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import static net.atlas.combatify.Combatify.*;

@SuppressWarnings("unused")
public class ClientNetworkingHandler {
	public static ConnectionState connectionState = ConnectionState.LOGIN;
	public static void init() {
		ClientPlayNetworking.registerGlobalReceiver(NetworkingHandler.RemainingUseSyncPacket.TYPE, (packet, player) -> {
			Entity entity = Minecraft.getInstance().level.getEntity(packet.id());
			if (entity instanceof LivingEntityExtensions livingEntity)
				livingEntity.setUseItemRemaining(packet.ticks());
		});
		ClientConfigurationNetworking.registerGlobalReceiver(NetworkingHandler.ClientboundClientInformationRetrievalPacket.TYPE, (packet, sender) -> {
			sender.responseSender().sendPacket(ClientPlayNetworking.createC2SPacket(new NetworkingHandler.ServerboundClientInformationExtensionPacket(CombatifyClient.shieldCrouch.get())));
			connectionState = ConnectionState.CONFIGURATION;
		});
		ClientPlayConnectionEvents.DISCONNECT.register(modDetectionNetworkChannel, (handler, client) -> Combatify.markCTS(false));
		ClientPlayConnectionEvents.JOIN.register(modDetectionNetworkChannel,(handler, sender, client) -> {
			connectionState = ConnectionState.PLAY;
			if (!ClientPlayNetworking.canSend(NetworkingHandler.ServerboundMissPacket.TYPE)) {
				CONFIG.reloadFromDefault();
				ITEMS.reloadFromDefault();
				Combatify.markCTS(true);
			}
			Combatify.modify();
		});
		ClientLifecycleEvents.CLIENT_STARTED.register(modDetectionNetworkChannel, client -> {
			ITEMS = new ItemConfig();
			Combatify.modify();

			Combatify.LOGGER.info("Loaded items config.");
		});
	}
	public enum ConnectionState {
		LOGIN,
		CONFIGURATION,
		PLAY
	}
}
