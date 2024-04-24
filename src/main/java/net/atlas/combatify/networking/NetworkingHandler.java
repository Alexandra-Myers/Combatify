package net.atlas.combatify.networking;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ItemConfig;
import net.atlas.combatify.extensions.*;
import net.atlas.combatify.util.MethodHandler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.phys.*;

import static net.atlas.combatify.Combatify.*;

public class NetworkingHandler {

	public NetworkingHandler() {
		PayloadTypeRegistry.playC2S().register(ServerboundMissPacket.TYPE, ServerboundMissPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(RemainingUseSyncPacket.TYPE, RemainingUseSyncPacket.CODEC);
		ServerPlayConnectionEvents.DISCONNECT.register(modDetectionNetworkChannel, (handler, server) -> {
			if (unmoddedPlayers.contains(handler.player.getUUID())) {
				unmoddedPlayers.remove(handler.player.getUUID());
				isPlayerAttacking.remove(handler.player.getUUID());
			}
			moddedPlayers.remove(handler.player.getUUID());
		});
		ServerPlayNetworking.registerGlobalReceiver(ServerboundMissPacket.TYPE, (packet, context) -> {
			ServerPlayer player = context.player().connection.getPlayer();
			final ServerLevel serverLevel = player.serverLevel();
			player.resetLastActionTime();
			if (!serverLevel.getWorldBorder().isWithinBounds(player.blockPosition()))
				return;
			((PlayerExtensions)player).attackAir();
		});
		ServerPlayConnectionEvents.JOIN.register(modDetectionNetworkChannel,(handler, sender, server) -> {
			boolean bl = CONFIG.configOnlyWeapons() || CONFIG.defender() || !CONFIG.letVanillaConnect();
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
		AttackEntityCallback.EVENT.register(modDetectionNetworkChannel, (player, world, hand, pos, direction) -> {
			if(Combatify.unmoddedPlayers.contains(player.getUUID()))
				Combatify.isPlayerAttacking.put(player.getUUID(), false);
			return InteractionResult.PASS;
		});
		AttackBlockCallback.EVENT.register(modDetectionNetworkChannel, (player, world, hand, pos, direction) -> {
			if(Combatify.unmoddedPlayers.contains(player.getUUID())) {
				Combatify.isPlayerAttacking.put(player.getUUID(), false);
				HitResult hitResult = new BlockHitResult(Vec3.atCenterOf(pos), direction, pos, false);
				hitResult = MethodHandler.redirectResult(player, hitResult);
				if (hitResult.getType() == HitResult.Type.ENTITY && player instanceof ServerPlayer serverPlayer) {
					serverPlayer.connection.handleInteract(ServerboundInteractPacket.createAttackPacket(((EntityHitResult) hitResult).getEntity(), player.isShiftKeyDown()));
					return InteractionResult.FAIL;
				}
			}
			return InteractionResult.PASS;
		});
		UseBlockCallback.EVENT.register(modDetectionNetworkChannel, (player, world, hand, hitResult) -> {
			if(Combatify.unmoddedPlayers.contains(player.getUUID()))
				Combatify.isPlayerAttacking.put(player.getUUID(), false);
			return InteractionResult.PASS;
		});
		UseEntityCallback.EVENT.register(modDetectionNetworkChannel, (player, world, hand, entity, hitResult) -> {
			if(Combatify.unmoddedPlayers.contains(player.getUUID()))
				Combatify.isPlayerAttacking.put(player.getUUID(), false);
			return InteractionResult.PASS;
		});
		UseItemCallback.EVENT.register(modDetectionNetworkChannel, (player, world, hand) -> {
			if(Combatify.unmoddedPlayers.contains(player.getUUID()))
				Combatify.isPlayerAttacking.put(player.getUUID(), false);
			return InteractionResultHolder.pass(player.getItemInHand(hand));
		});
		ServerLifecycleEvents.SERVER_STARTED.register(modDetectionNetworkChannel, server -> {
			if (ITEMS == null)
				ITEMS = new ItemConfig();
		});
	}
	public record ServerboundMissPacket() implements CustomPacketPayload {
		public static final Type<ServerboundMissPacket> TYPE = CustomPacketPayload.createType(Combatify.id("miss_attack").toString());
		public static final StreamCodec<FriendlyByteBuf, ServerboundMissPacket> CODEC = CustomPacketPayload.codec(ServerboundMissPacket::write, ServerboundMissPacket::new);

		public ServerboundMissPacket(FriendlyByteBuf buf) {
			this();
		}

		public void write(FriendlyByteBuf buf) {

		}
		@Override
		public Type<?> type() {
			return TYPE;
		}
	}
	public record RemainingUseSyncPacket(int id, int ticks) implements CustomPacketPayload {
		public static final Type<RemainingUseSyncPacket> TYPE = CustomPacketPayload.createType(Combatify.id("remaining_use_ticks").toString());
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
		public Type<?> type() {
			return TYPE;
		}
	}
}
