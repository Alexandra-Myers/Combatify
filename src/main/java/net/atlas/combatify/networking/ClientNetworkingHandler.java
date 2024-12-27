package net.atlas.combatify.networking;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.CombatifyClient;
import net.atlas.combatify.config.ItemConfig;
import net.atlas.combatify.extensions.ItemStackExtensions;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import static net.atlas.combatify.Combatify.*;

@SuppressWarnings("unused")
public class ClientNetworkingHandler {
	public static ConnectionState connectionState = ConnectionState.LOGIN;
	public static void init() {
		ClientPlayNetworking.registerGlobalReceiver(NetworkingHandler.ClientboundTooltipUpdatePacket.TYPE, (payload, context) -> {
			ItemStack stack = Minecraft.getInstance().player.getInventory().getItem(payload.slot());
			if (!stack.isEmpty()) {
				((ItemStackExtensions)(Object)stack).combatify$setBlockerInformation(payload.components(), payload.dataType());
			}
		});
		ClientPlayNetworking.registerGlobalReceiver(NetworkingHandler.UpdateBridgingStatusPacket.TYPE, (packet, context) -> CONFIG.setBridging(packet.enableBridging()));
		ClientPlayNetworking.registerGlobalReceiver(NetworkingHandler.RemainingUseSyncPacket.TYPE, (packet, player) -> {
			Entity entity = Minecraft.getInstance().level.getEntity(packet.id());
			if (entity instanceof LivingEntity livingEntity)
				livingEntity.combatify$setUseItemRemaining(packet.ticks());
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
		});
		ClientLifecycleEvents.CLIENT_STARTED.register(modDetectionNetworkChannel, client -> {
			ITEMS = new ItemConfig();

			Combatify.LOGGER.info("Loaded items config.");
		});
	}
	public enum ConnectionState {
		LOGIN,
		CONFIGURATION,
		PLAY
	}
}
