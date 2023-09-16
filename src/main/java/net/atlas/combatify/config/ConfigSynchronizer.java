package net.atlas.combatify.config;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.logging.LogUtils;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.mixin.ServerGamePacketListenerAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;

public class ConfigSynchronizer {

    public static final ResourceLocation CONFIG_SYNC_CHANNEL = new ResourceLocation("owo", "config_sync");

    private static final Map<Connection, BiMap<String, SynchableOption<?>>> CLIENT_OPTION_STORAGE = new WeakHashMap<>();
	public static void init() {

	}

    /**
     * Retrieve the options which the given player's client
     * sent to the server during config synchronization
     *
     * @param player     The player for which to retrieve the client values
     * @return The player's client's values of the given config options,
     * or {@code null} if no config with the given name was synced
     */
    public static @Nullable Map<String, SynchableOption<?>> getClientOptions(ServerPlayer player) {
		return CLIENT_OPTION_STORAGE.get(((ServerGamePacketListenerAccessor) player.connection).combatify$getConnection());
    }

    private static void write(FriendlyByteBuf packet, int ordinal) {
        packet.writeVarInt(1);

        var configBuf = PacketByteBufs.create();
        var optionBuf = PacketByteBufs.create();
        packet.writeUtf("combatify-config");

        configBuf.resetReaderIndex().resetWriterIndex();
        configBuf.writeVarInt((int) Combatify.CONFIG.options.values().stream().filter(option -> 2 >= ordinal).count());

        Combatify.CONFIG.options.forEach((key, option) -> {
            if (2 < ordinal) return;

            configBuf.writeUtf(key);

            optionBuf.resetReaderIndex().resetWriterIndex();
            option.writeToBuf(optionBuf);

            configBuf.writeVarInt(optionBuf.readableBytes());
            configBuf.writeBytes(optionBuf);
        });

        packet.writeVarInt(configBuf.readableBytes());
        packet.writeBytes(configBuf);
    }

    private static void read(FriendlyByteBuf buf, BiConsumer<SynchableOption<?>, FriendlyByteBuf> optionConsumer) {
        int configCount = buf.readVarInt();
        for (int i = 0; i < configCount; i++) {
            var configName = buf.readUtf();
            var config = Combatify.CONFIG;
            if (!configName.equals("combatify-config")) {
                Combatify.LOGGER.error("Tried to sync an owo config which does not belong to Combatify");

                // skip size of current config
                buf.skipBytes(buf.readVarInt());
                continue;
            }

            // ignore size
            buf.readVarInt();

            int optionCount = buf.readVarInt();
            for (int j = 0; j < optionCount; j++) {
				var name = buf.readUtf();
                var option = config.options.get(name);
                if (option == null) {
                    Combatify.LOGGER.error("Received override for unknown option '" + name + "' in config `combatify-config`, skipping");

                    // skip size of current option
                    buf.skipBytes(buf.readVarInt());
                    continue;
                }

                // ignore size
                buf.readVarInt();

                optionConsumer.accept(option, buf);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    private static void applyClient(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender sender) {
        Combatify.LOGGER.info("Applying server overrides");
        var mismatchedOptions = new HashMap<SynchableOption<?>, Object>();

        if (!(client.isSingleplayer() && Objects.requireNonNull(client.getCurrentServer()).isLan())) {
            read(buf, (option, packetByteBuf) -> {
                var mismatchedValue = option.findMismatch(packetByteBuf);
                if (mismatchedValue != null) mismatchedOptions.put(option, mismatchedValue);
            });

            if (!mismatchedOptions.isEmpty()) {
                Combatify.LOGGER.error("Aborting connection, non-syncable config values were mismatched");
                mismatchedOptions.forEach((option, serverValue) -> LogUtils.getLogger().error("- Option {} in config '{}' has value '{}' but server requires '{}'",
						Combatify.CONFIG.options.inverse().get(option), "combatify-config", option.get(), serverValue));
				MutableComponent error = Component.literal("Aborting connection, non-syncable config values were mismatched.\n");
				mismatchedOptions.forEach(((option, o) -> error.append(Component.literal(Combatify.CONFIG.options.inverse().get(option) + " does not match the server's value of " + o + ".\n"))));
                handler.getConnection().disconnect(error);
                return;
            }
        }

        Combatify.LOGGER.info("Responding with client values");
        var packet = PacketByteBufs.create();
        write(packet, 1);

        sender.sendPacket(CONFIG_SYNC_CHANNEL, packet);
    }

    private static void applyServer(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender sender) {
        Combatify.LOGGER.info("Receiving client config");
        var connection = ((ServerGamePacketListenerAccessor) player.connection).combatify$getConnection();

        read(buf, (option, optionBuf) -> {
            var config = CLIENT_OPTION_STORAGE.computeIfAbsent(connection, $ -> HashBiMap.create());
            config.put(config.inverse().get(option), option.trySync(optionBuf));
        });
    }

    static {
        var earlyPhase = new ResourceLocation("owo", "early");
        ServerPlayConnectionEvents.JOIN.addPhaseOrdering(earlyPhase, Event.DEFAULT_PHASE);
        ServerPlayConnectionEvents.JOIN.register(earlyPhase, (handler, sender, server) -> {
            Combatify.LOGGER.info("Sending server config values to client");

            var packet = PacketByteBufs.create();
            write(packet, 2);

            sender.sendPacket(CONFIG_SYNC_CHANNEL, packet);
        });

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(CONFIG_SYNC_CHANNEL, ConfigSynchronizer::applyClient);

            ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> Combatify.CONFIG.options.forEach((s, synchableOption) -> synchableOption.restore()));
        }

        ServerPlayNetworking.registerGlobalReceiver(CONFIG_SYNC_CHANNEL, ConfigSynchronizer::applyServer);
    }
}
