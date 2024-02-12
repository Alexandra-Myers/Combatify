package net.atlas.combatify.networking;

import com.google.common.collect.ArrayListMultimap;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.config.AtlasConfig;
import net.atlas.combatify.config.ConfigurableItemData;
import net.atlas.combatify.config.ItemConfig;
import net.atlas.combatify.extensions.*;
import net.atlas.combatify.item.WeaponType;
import net.atlas.combatify.util.MethodHandler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.item.v1.ModifyItemAttributeModifiersCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.*;

import java.util.*;

import static net.atlas.combatify.Combatify.*;

public class NetworkingHandler {

	public NetworkingHandler() {
		PayloadTypeRegistry.playS2C().register(AtlasConfigPacket.TYPE, AtlasConfigPacket.CODEC);
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
			ServerPlayer player = context.player();
			final ServerLevel serverLevel = player.serverLevel();
			player.resetLastActionTime();
			if (!serverLevel.getWorldBorder().isWithinBounds(player.blockPosition())) {
				return;
			}
			double d = MethodHandler.getCurrentAttackReach(player, 1.0F) + 1;
			d *= d;
			if(!player.hasLineOfSight(player)) {
				d = 6.25;
			}

			AABB aABB = player.getBoundingBox();
			Vec3 eyePos = player.getEyePosition(0.0F);
			eyePos.distanceToSqr(MethodHandler.getNearestPointTo(aABB, eyePos));
			double dist = 0;
			if (dist < d) {
				((PlayerExtensions)player).attackAir();
			}

		});
		ServerPlayConnectionEvents.JOIN.register(modDetectionNetworkChannel,(handler, sender, server) -> {
			boolean bl = CONFIG.configOnlyWeapons() || CONFIG.defender() || CONFIG.piercer() || !CONFIG.letVanillaConnect();
			if(!ServerPlayNetworking.canSend(handler.player, AtlasConfigPacket.TYPE)) {
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
			for (AtlasConfig atlasConfig : AtlasConfig.configs.values()) {
				ServerPlayNetworking.send(handler.player, new AtlasConfigPacket(atlasConfig));
			}
			Combatify.LOGGER.info("Config packet sent to client.");
		});
		ModifyItemAttributeModifiersCallback.EVENT.register(modDetectionNetworkChannel, (stack, slot, attributeModifiers) -> {
			Item item = stack.getItem();
			if (Combatify.ITEMS.configuredItems.containsKey(item) && slot == EquipmentSlot.MAINHAND) {
				ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(item);
				if (configurableItemData.type != null) {
					if (attributeModifiers.containsKey(Attributes.ATTACK_DAMAGE)) {
						List<Integer> indexes = new ArrayList<>();
						List<AttributeModifier> modifiers = attributeModifiers.get(Attributes.ATTACK_DAMAGE).stream().toList();
						for (AttributeModifier modifier : modifiers)
							if (modifier.getId() == Item.BASE_ATTACK_DAMAGE_UUID)
								indexes.add(modifiers.indexOf(modifier));
						if (!indexes.isEmpty())
							for (Integer index : indexes)
								attributeModifiers.remove(Attributes.ATTACK_DAMAGE, modifiers.get(index));
					}
					if (attributeModifiers.containsKey(Attributes.ATTACK_SPEED)) {
						List<Integer> indexes = new ArrayList<>();
						List<AttributeModifier> modifiers = attributeModifiers.get(Attributes.ATTACK_SPEED).stream().toList();
						for (AttributeModifier modifier : modifiers)
							if (modifier.getId() == Item.BASE_ATTACK_SPEED_UUID || modifier.getId() == WeaponType.BASE_ATTACK_SPEED_UUID)
								indexes.add(modifiers.indexOf(modifier));
						if (!indexes.isEmpty())
							for (Integer index : indexes)
								attributeModifiers.remove(Attributes.ATTACK_SPEED, modifiers.get(index));
					}
					if (attributeModifiers.containsKey(Attributes.ENTITY_INTERACTION_RANGE)) {
						List<Integer> indexes = new ArrayList<>();
						List<AttributeModifier> modifiers = attributeModifiers.get(Attributes.ENTITY_INTERACTION_RANGE).stream().toList();
						for (AttributeModifier modifier : modifiers)
							if (modifier.getId() == WeaponType.BASE_ATTACK_REACH_UUID)
								indexes.add(modifiers.indexOf(modifier));
						if (!indexes.isEmpty())
							for (Integer index : indexes)
								attributeModifiers.remove(Attributes.ENTITY_INTERACTION_RANGE, modifiers.get(index));
					}
					ArrayListMultimap<Holder<Attribute>, AttributeModifier> modMap = ArrayListMultimap.create();
					configurableItemData.type.addCombatAttributes(item instanceof TieredItem tieredItem ? tieredItem.getTier() : item instanceof Tierable tierable ? tierable.getTier() : Tiers.NETHERITE, modMap);
					attributeModifiers.putAll(modMap);
				}
				if (configurableItemData.damage != null) {
					if (attributeModifiers.containsKey(Attributes.ATTACK_DAMAGE)) {
						List<Integer> indexes = new ArrayList<>();
						List<AttributeModifier> modifiers = attributeModifiers.get(Attributes.ATTACK_DAMAGE).stream().toList();
						for (AttributeModifier modifier : modifiers)
							if (modifier.getId() == Item.BASE_ATTACK_DAMAGE_UUID)
								indexes.add(modifiers.indexOf(modifier));
						if (!indexes.isEmpty())
							for (Integer index : indexes)
								attributeModifiers.remove(Attributes.ATTACK_DAMAGE, modifiers.get(index));
					}
					attributeModifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_UUID, "Config modifier", configurableItemData.damage - (CONFIG.fistDamage() ? 1 : 2), AttributeModifier.Operation.ADDITION));
				}
				if (configurableItemData.speed != null) {
					if (attributeModifiers.containsKey(Attributes.ATTACK_SPEED)) {
						List<Integer> indexes = new ArrayList<>();
						List<AttributeModifier> modifiers = attributeModifiers.get(Attributes.ATTACK_SPEED).stream().toList();
						for (AttributeModifier modifier : modifiers)
							if (modifier.getId() == Item.BASE_ATTACK_SPEED_UUID || modifier.getId() == WeaponType.BASE_ATTACK_SPEED_UUID)
								indexes.add(modifiers.indexOf(modifier));
						if (!indexes.isEmpty())
							for (Integer index : indexes)
								attributeModifiers.remove(Attributes.ATTACK_SPEED, modifiers.get(index));
					}
					attributeModifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(WeaponType.BASE_ATTACK_SPEED_UUID, "Config modifier", configurableItemData.speed - CONFIG.baseHandAttackSpeed(), AttributeModifier.Operation.ADDITION));
				}
				if (configurableItemData.reach != null) {
					if (attributeModifiers.containsKey(Attributes.ENTITY_INTERACTION_RANGE)) {
						List<Integer> indexes = new ArrayList<>();
						List<AttributeModifier> modifiers = attributeModifiers.get(Attributes.ENTITY_INTERACTION_RANGE).stream().toList();
						for (AttributeModifier modifier : modifiers)
							if (modifier.getId() == WeaponType.BASE_ATTACK_REACH_UUID)
								indexes.add(modifiers.indexOf(modifier));
						if (!indexes.isEmpty())
							for (Integer index : indexes)
								attributeModifiers.remove(Attributes.ENTITY_INTERACTION_RANGE, modifiers.get(index));
					}
					attributeModifiers.put(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(WeaponType.BASE_ATTACK_REACH_UUID, "Config modifier", configurableItemData.reach - 2.5, AttributeModifier.Operation.ADDITION));
				}
			}
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
			Combatify.ITEMS = new ItemConfig();

			List<Item> items = BuiltInRegistries.ITEM.stream().toList();

			for(Item item : items) {
				((ItemExtensions) item).modifyAttributeModifiers();
			}
			for(Item item : Combatify.ITEMS.configuredItems.keySet()) {
				ConfigurableItemData configurableItemData = Combatify.ITEMS.configuredItems.get(item);
				if (configurableItemData.stackSize != null)
					((ItemExtensions) item).setStackSize(configurableItemData.stackSize);
			}
		});
	}
	public record AtlasConfigPacket(AtlasConfig config) implements CustomPacketPayload {
		public static final Type<AtlasConfigPacket> TYPE = CustomPacketPayload.createType(Combatify.id("atlas_config").toString());
		public static final StreamCodec<FriendlyByteBuf, AtlasConfigPacket> CODEC = CustomPacketPayload.codec(AtlasConfigPacket::write, AtlasConfigPacket::new);

		public AtlasConfigPacket(FriendlyByteBuf buf) {
			this(AtlasConfig.staticLoadFromNetwork(buf));
		}

		public void write(FriendlyByteBuf buf) {
			buf.writeResourceLocation(config.name);
			config.saveToNetwork(buf);
		}

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
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
