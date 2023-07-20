package net.alexandra.atlas.atlas_combat.networking;

import io.wispforest.owo.Owo;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.ops.TextOps;
import net.alexandra.atlas.atlas_combat.config.AtlasConfig;
import net.alexandra.atlas.atlas_combat.extensions.ItemExtensions;
import net.alexandra.atlas.atlas_combat.mixin.OwoOptionAccessor;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static io.wispforest.owo.Owo.PREFIX;
import static net.alexandra.atlas.atlas_combat.AtlasCombat.*;

public class ClientNetworkingHandler {
	@SuppressWarnings("unchecked")
	public ClientNetworkingHandler() {
		ClientPlayNetworking.registerGlobalReceiver(modDetectionNetworkChannel,(client, handler, buf, responseSender) -> {
		});
		ClientPlayConnectionEvents.JOIN.register(modDetectionNetworkChannel,(handler, sender, client) -> {
			boolean bl = Objects.requireNonNull(handler.getServerData()).protocol == 803;
			if(!ClientPlayNetworking.canSend(modDetectionNetworkChannel) && !bl) {
				handler.getConnection().disconnect(Component.literal("Atlas Combat needs to be installed on the server to join with this client"));
				return;
			}
			List<Item> items = BuiltInRegistries.ITEM.stream().toList();

			if (bl) {
				for (Option<Boolean> option : getBooleansFromInstance(CONFIG)) {
					FriendlyByteBuf buf = PacketByteBufs.create();
					buf.writeBoolean(option.defaultValue());
					OwoOptionAccessor<Boolean> accessor = OwoOptionAccessor.class.cast(option);
					Boolean ret = accessor.readFromBuffer(buf);
					if(ret != null) {
						createErrorFromOptionNonDefault(option, handler);
						return;
					}
				}
				for (Option<Integer> option : getIntegersFromInstance(CONFIG)) {
					FriendlyByteBuf buf = PacketByteBufs.create();
					buf.writeInt(option.defaultValue());
					OwoOptionAccessor<Integer> accessor = OwoOptionAccessor.class.cast(option);
					Integer ret = accessor.readFromBuffer(buf);
					if(ret != null) {
						createErrorFromOptionNonDefault(option, handler);
						return;
					}
				}
				for (Option<Float> option : getFloatsFromInstance(CONFIG)) {
					FriendlyByteBuf buf = PacketByteBufs.create();
					buf.writeFloat(option.defaultValue());
					OwoOptionAccessor<Float> accessor = OwoOptionAccessor.class.cast(option);
					Float ret = accessor.readFromBuffer(buf);
					if(ret != null) {
						createErrorFromOptionNonDefault(option, handler);
						return;
					}
				}
			}

			for(Item item : items) {
				((ItemExtensions) item).modifyAttributeModifiers();
			}
		});
	}
	@SuppressWarnings("unchecked")
	public static List<Option<Boolean>> getBooleansFromInstance(AtlasConfig config) {
		return Arrays.stream(AtlasConfig.class.getFields())
			.filter(field -> field.getType().isAssignableFrom(Option.class))
			.map(field -> {
				try {
					field.setAccessible(true);
					return field.get(config);
				} catch (Throwable t) {
					return null;
				}
			})
			.filter(value -> value instanceof Option<?> o && o.value() instanceof Boolean)
			.map(value -> (Option<Boolean>)value)
			.toList();
	}
	@SuppressWarnings("unchecked")
	public static List<Option<Integer>> getIntegersFromInstance(AtlasConfig config) {
		return Arrays.stream(AtlasConfig.class.getFields())
			.filter(field -> field.getType().isAssignableFrom(Option.class))
			.map(field -> {
				try {
					field.setAccessible(true);
					return field.get(config);
				} catch (Throwable t) {
					return null;
				}
			})
			.filter(value -> value instanceof Option<?> o && o.value() instanceof Integer)
			.map(value -> (Option<Integer>)value)
			.toList();
	}
	@SuppressWarnings("unchecked")
	public static List<Option<Float>> getFloatsFromInstance(AtlasConfig config) {
		return Arrays.stream(AtlasConfig.class.getFields())
			.filter(field -> field.getType().isAssignableFrom(Option.class))
			.map(field -> {
				try {
					field.setAccessible(true);
					return field.get(config);
				} catch (Throwable t) {
					return null;
				}
			})
			.filter(value -> value instanceof Option<?> o && o.value() instanceof Float)
			.map(value -> (Option<Float>)value)
			.toList();
	}
	public static void createErrorFromOptionNonDefault(Option<?> option, ClientPacketListener handler) {
		Owo.LOGGER.error("Aborting connection, non-syncable config values were mismatched");
		Owo.LOGGER.error("- Option {} in config '{}' has value '{}' but server requires '{}'", option.key().asString(), option.configName(), option.value(), option.defaultValue());

		var errorMessage = Component.empty();

		errorMessage.append(TextOps.withFormatting("in config ", ChatFormatting.GRAY)).append(option.configName()).append("\n");
		errorMessage.append(Component.translatable(option.translationKey()).withStyle(ChatFormatting.YELLOW)).append(" -> ");
		errorMessage.append(option.value().toString()).append(TextOps.withFormatting(" (client)", ChatFormatting.GRAY));
		errorMessage.append(TextOps.withFormatting(" / ", ChatFormatting.DARK_GRAY));
		errorMessage.append(option.toString()).append(TextOps.withFormatting(" (server)", ChatFormatting.GRAY)).append("\n");
		errorMessage.append("\n");

		errorMessage.append(TextOps.withFormatting("these options could not be synchronized because\n", ChatFormatting.GRAY));
		errorMessage.append(TextOps.withFormatting("they require your client to be restarted\n", ChatFormatting.GRAY));
		errorMessage.append(TextOps.withFormatting("change them manually and restart if you want to join this server", ChatFormatting.GRAY));

		handler.getConnection().disconnect(TextOps.concat(PREFIX, errorMessage));
	}
}
