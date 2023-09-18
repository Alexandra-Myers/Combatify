package net.atlas.combatify.networking;

import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.config.ItemConfig;
import net.atlas.combatify.extensions.ItemExtensions;
import net.atlas.combatify.extensions.PlayerExtensions;
import net.atlas.combatify.extensions.ServerPlayerExtensions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.*;

import static net.atlas.combatify.Combatify.*;

public class NetworkingHandler {

	public NetworkingHandler() {
		ServerPlayNetworking.registerGlobalReceiver(modDetectionNetworkChannel,(server, player, handler, buf, responseSender) -> {
		});
		ServerPlayConnectionEvents.DISCONNECT.register(modDetectionNetworkChannel, (handler, server) -> {
			if (unmoddedPlayers.contains(handler.player.getUUID())) {
				Timer timer = scheduleHitResult.get(handler.getPlayer().getUUID());
				timer.cancel();
				timer.purge();
			}
		});
		ServerPlayConnectionEvents.JOIN.register(modDetectionNetworkChannel,(handler, sender, server) -> {
			boolean bl = CONFIG.configOnlyWeapons.get() || CONFIG.defender.get() || CONFIG.piercer.get() || !CONFIG.letVanillaConnect.get();
			if(!ServerPlayNetworking.canSend(handler.player, ItemConfigPacket.TYPE)) {
				if(bl) {
					handler.player.connection.disconnect(Component.literal("Combatify needs to be installed on the client to join this server!"));
					return;
				}
				Combatify.unmoddedPlayers.add(handler.player.getUUID());
				Combatify.isPlayerAttacking.put(handler.player.getUUID(), true);
				Combatify.finalizingAttack.put(handler.player.getUUID(), true);
				scheduleHitResult.put(handler.player.getUUID(), new Timer());
				Combatify.LOGGER.info("Unmodded player joined: " + handler.player.getUUID());
				return;
			}
			if (unmoddedPlayers.contains(handler.player.getUUID())) {
				unmoddedPlayers.remove(handler.player.getUUID());
				isPlayerAttacking.remove(handler.player.getUUID());
				finalizingAttack.remove(handler.player.getUUID());
			}
			ServerPlayNetworking.send(handler.player, new ItemConfigPacket(ITEMS));
			Combatify.LOGGER.info("Config packet sent to client.");
		});
		ServerPlayNetworking.registerGlobalReceiver(ServerboundMissPacket.TYPE, (packet, player, responseSender) -> ((PlayerExtensions)player).attackAir());
		AttackBlockCallback.EVENT.register(modDetectionNetworkChannel, (player, world, hand, pos, direction) -> {
			if (Combatify.unmoddedPlayers.contains(player.getUUID()) && finalizingAttack.get(player.getUUID()) && player instanceof ServerPlayer serverPlayer) {
				Map<HitResult, Float[]> hitResultToRotationMap = ((ServerPlayerExtensions)serverPlayer).getHitResultToRotationMap();
				((ServerPlayerExtensions) serverPlayer).getPresentResult();
				for (HitResult hitResultToChoose : ((ServerPlayerExtensions)serverPlayer).getOldHitResults()) {
					if(hitResultToChoose == null)
						continue;
					Float[] rotations = null;
					if (hitResultToRotationMap.containsKey(hitResultToChoose))
						rotations = hitResultToRotationMap.get(hitResultToChoose);
					float xRot = serverPlayer.getXRot() % 360;
					float yRot = serverPlayer.getYHeadRot() % 360;
					if(rotations != null) {
						float xDiff = Math.abs(xRot - rotations[1]);
						float yDiff = Math.abs(yRot - rotations[0]);
						if(xDiff > 20 || yDiff > 20)
							continue;
					}
					if (hitResultToChoose.getType() == HitResult.Type.ENTITY) {
						return InteractionResult.FAIL;
					}
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
			ITEMS = new ItemConfig();

			IForgeRegistry<Item> items = ForgeRegistries.ITEMS;

			for(Item item : items) {
				((ItemExtensions) item).modifyAttributeModifiers();
			}
			for(Item item : ITEMS.configuredItems.keySet()) {
				ConfigurableItemData configurableItemData = ITEMS.configuredItems.get(item);
				if (configurableItemData.stackSize != null)
					((ItemExtensions) item).setStackSize(configurableItemData.stackSize);
			}
		});
	}
	public record ItemConfigPacket(ItemConfig config) implements FabricPacket {
		public static final PacketType<ItemConfigPacket> TYPE = PacketType.create(Combatify.id("item_config"), ItemConfigPacket::new);

		public ItemConfigPacket(FriendlyByteBuf buf) {
			this(ITEMS.loadFromNetwork(buf));
		}

		/**
		 * Writes the contents of this packet to the buffer.
		 *
		 * @param buf the output buffer
		 */
		@Override
		public void write(FriendlyByteBuf buf) {
			ITEMS.saveToNetwork(buf);
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
		public PacketType<?> getType() {
			return TYPE;
		}
	}
	public record ServerboundMissPacket() implements FabricPacket {
		public static final PacketType<ServerboundMissPacket> TYPE = PacketType.create(Combatify.id("player_miss"), ServerboundMissPacket::new);

		public ServerboundMissPacket(FriendlyByteBuf buf) {
			this();
		}

		/**
		 * Writes the contents of this packet to the buffer.
		 *
		 * @param buf the output buffer
		 */
		@Override
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
		public PacketType<?> getType() {
			return TYPE;
		}
	}
}
