package net.atlas.combatify.networking;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ItemConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ConfigurationTask;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static net.atlas.combatify.Combatify.*;

public class NetworkingHandler {

	public NetworkingHandler() {
		PayloadTypeRegistry.playC2S().register(ServerboundMissPacket.TYPE, ServerboundMissPacket.CODEC);
		PayloadTypeRegistry.playC2S().register(ServerboundClientInformationExtensionPacket.TYPE, ServerboundClientInformationExtensionPacket.CODEC);
		PayloadTypeRegistry.configurationC2S().register(ServerboundClientInformationExtensionPacket.TYPE, ServerboundClientInformationExtensionPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(RemainingUseSyncPacket.TYPE, RemainingUseSyncPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(UpdateBridgingStatusPacket.TYPE, UpdateBridgingStatusPacket.CODEC);
		PayloadTypeRegistry.configurationS2C().register(ClientboundClientInformationRetrievalPacket.TYPE, ClientboundClientInformationRetrievalPacket.CODEC);
		ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
			if (ServerConfigurationNetworking.canSend(handler, ClientboundClientInformationRetrievalPacket.TYPE))
				handler.addTask(new ClientRetrievalTask());
		});
		ServerPlayConnectionEvents.DISCONNECT.register(modDetectionNetworkChannel, (handler, server) -> {
			if (unmoddedPlayers.contains(handler.player.getUUID())) {
				unmoddedPlayers.remove(handler.player.getUUID());
				isPlayerAttacking.remove(handler.player.getUUID());
			}
			moddedPlayers.remove(handler.player.getUUID());
		});
		ServerPlayNetworking.registerGlobalReceiver(ServerboundMissPacket.TYPE, (packet, context) -> {
			ServerPlayer player = context.player().connection.getPlayer();
			final ServerLevel serverLevel = player.level();
			player.resetLastActionTime();
			if (!serverLevel.getWorldBorder().isWithinBounds(player.blockPosition()))
				return;
			player.combatify$attackAir();
		});
		ServerConfigurationNetworking.registerGlobalReceiver(ServerboundClientInformationExtensionPacket.TYPE, (payload, context) -> {
			context.networkHandler().combatify$setShieldOnCrouch(payload.useShieldOnCrouch);
			context.networkHandler().completeTask(ClientRetrievalTask.TYPE);
		});
		ServerPlayNetworking.registerGlobalReceiver(ServerboundClientInformationExtensionPacket.TYPE, (payload, context) -> context.player().connection.getPlayer().combatify$setShieldOnCrouch(payload.useShieldOnCrouch));
		ServerPlayConnectionEvents.JOIN.register(modDetectionNetworkChannel,(handler, sender, server) -> {
			boolean bl = CONFIG.configOnlyWeapons() || !CONFIG.letVanillaConnect();
			if(!ServerPlayNetworking.canSend(handler.player, RemainingUseSyncPacket.TYPE)) {
				if(bl) {
					handler.player.connection.disconnect(Component.literal("Combatify needs to be installed on the client to join this server!"));
					return;
				}
				Combatify.unmoddedPlayers.add(handler.player.getUUID());
				Combatify.isPlayerAttacking.put(handler.player.getUUID(), true);
				Combatify.LOGGER.info("Unmodded player joined: " + handler.player.getUUID());
				return;
			}
			moddedPlayers.add(handler.player.getUUID());
		});
		ServerLifecycleEvents.SERVER_STARTED.register(modDetectionNetworkChannel, server -> ITEMS = new ItemConfig());
	}
	public record UpdateBridgingStatusPacket(boolean enableBridging) implements CustomPacketPayload {
		public static final Type<UpdateBridgingStatusPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath("c", "update_status"));
		public static final StreamCodec<FriendlyByteBuf, UpdateBridgingStatusPacket> CODEC = CustomPacketPayload.codec(UpdateBridgingStatusPacket::write, UpdateBridgingStatusPacket::new);

		public UpdateBridgingStatusPacket(FriendlyByteBuf buf) {
			this(buf.readBoolean());
		}

		public void write(FriendlyByteBuf buf) {
			buf.writeBoolean(enableBridging);
		}
		@Override
		public @NotNull Type<?> type() {
			return TYPE;
		}
	}

	public record ServerboundMissPacket() implements CustomPacketPayload {
		public static final Type<ServerboundMissPacket> TYPE = new Type<>(Combatify.id("miss_attack"));
		public static final StreamCodec<FriendlyByteBuf, ServerboundMissPacket> CODEC = CustomPacketPayload.codec(ServerboundMissPacket::write, ServerboundMissPacket::new);

		public ServerboundMissPacket(FriendlyByteBuf buf) {
			this();
		}

		public void write(FriendlyByteBuf buf) {

		}
		@Override
		public @NotNull Type<?> type() {
			return TYPE;
		}
	}
	public record RemainingUseSyncPacket(int id, int ticks) implements CustomPacketPayload {
		public static final Type<RemainingUseSyncPacket> TYPE = new Type<>(Combatify.id("remaining_use_ticks"));
		public static final StreamCodec<FriendlyByteBuf, RemainingUseSyncPacket> CODEC = CustomPacketPayload.codec(RemainingUseSyncPacket::write, RemainingUseSyncPacket::new);

		public RemainingUseSyncPacket(FriendlyByteBuf buf) {
			this(buf.readVarInt(), buf.readInt());
		}

		public void write(FriendlyByteBuf buf) {
			buf.writeVarInt(id);
			buf.writeInt(ticks);
		}

		/**
		 * Returns the packet type of this packet.
		 *
		 * <p>Implementations should store the packet type instance in a {@code static final}
		 * field and return that here, instead of creating a new instance.
		 *
		 * @return the type of this packet
		 */
		@Override
		public @NotNull Type<?> type() {
			return TYPE;
		}
	}

	public record ClientRetrievalTask() implements ConfigurationTask {
		public static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type(Combatify.id("client_info_retrieval").toString());

		@Override
		public void start(Consumer<Packet<?>> sender) {
			sender.accept(ServerConfigurationNetworking.createS2CPacket(new ClientboundClientInformationRetrievalPacket()));
		}

		@Override
		public @NotNull Type type() {
			return TYPE;
		}
	}

	public record ClientboundClientInformationRetrievalPacket() implements CustomPacketPayload {
		public static final Type<ClientboundClientInformationRetrievalPacket> TYPE = new Type<>(Combatify.id("client_retrieval"));
		public static final StreamCodec<FriendlyByteBuf, ClientboundClientInformationRetrievalPacket> CODEC = CustomPacketPayload.codec(ClientboundClientInformationRetrievalPacket::write, ClientboundClientInformationRetrievalPacket::new);

		public ClientboundClientInformationRetrievalPacket(FriendlyByteBuf buf) {
			this();
		}

		public void write(FriendlyByteBuf buf) {

		}

		/**
		 * Returns the packet type of this packet.
		 *
		 * <p>Implementations should store the packet type instance in a {@code static final}
		 * field and return that here, instead of creating a new instance.
		 *
		 * @return the type of this packet
		 */
		@Override
		public @NotNull Type<?> type() {
			return TYPE;
		}
	}

	public record ServerboundClientInformationExtensionPacket(boolean useShieldOnCrouch) implements CustomPacketPayload {
		public static final Type<ServerboundClientInformationExtensionPacket> TYPE = new Type<>(Combatify.id("client_extras"));
		public static final StreamCodec<FriendlyByteBuf, ServerboundClientInformationExtensionPacket> CODEC = CustomPacketPayload.codec(ServerboundClientInformationExtensionPacket::write, ServerboundClientInformationExtensionPacket::new);

		public ServerboundClientInformationExtensionPacket(FriendlyByteBuf buf) {
			this(buf.readBoolean());
		}

		public void write(FriendlyByteBuf buf) {
			buf.writeBoolean(useShieldOnCrouch);
		}

		/**
		 * Returns the packet type of this packet.
		 *
		 * <p>Implementations should store the packet type instance in a {@code static final}
		 * field and return that here, instead of creating a new instance.
		 *
		 * @return the type of this packet
		 */
		@Override
		public @NotNull Type<?> type() {
			return TYPE;
		}
	}
}
