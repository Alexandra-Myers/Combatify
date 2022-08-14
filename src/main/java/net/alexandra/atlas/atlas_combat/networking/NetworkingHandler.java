package net.alexandra.atlas.atlas_combat.networking;

import io.netty.buffer.Unpooled;
import net.alexandra.atlas.atlas_combat.extensions.ItemExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

public class NetworkingHandler {

	public ResourceLocation modDetectionNetworkChannel = new ResourceLocation("atlas-combat","networking");
	public ResourceLocation itemStackSizeNetworkChannel = new ResourceLocation("atlas-combat","items");

	public static boolean receivedAnswer = false;
	public static int ticksElapsed = 0;

	public NetworkingHandler() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->  {
			FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
			ClientPlayNetworking.send(modDetectionNetworkChannel,buf);
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> receivedAnswer = false);

		ServerPlayNetworking.registerGlobalReceiver(modDetectionNetworkChannel,(server, serverPlayer, serverGamePacketListener, buf, packetSender) -> {
			FriendlyByteBuf packetBuf = new FriendlyByteBuf(Unpooled.buffer());
			packetBuf.writeBoolean(true);
			ServerPlayNetworking.send(serverPlayer, modDetectionNetworkChannel,packetBuf);
		});

		ClientPlayNetworking.registerGlobalReceiver(modDetectionNetworkChannel,(client, handler, buf, responseSender) -> {
			if(buf.getBoolean(0)) {
				receivedAnswer = true;
			}
		});

		ClientTickEvents.END.register((client) -> {
			if(Minecraft.getInstance().getConnection() != null && Minecraft.getInstance().player != null && !receivedAnswer) {
				ticksElapsed++;

				System.out.println(ticksElapsed);
				System.out.println(receivedAnswer);

				if(ticksElapsed >= 50 && !receivedAnswer) {
					Minecraft.getInstance().player.connection.getConnection().disconnect(Component.literal("Mod not present on server!"));
					ticksElapsed = 0;
					receivedAnswer = false;
				}
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(itemStackSizeNetworkChannel,(client, handler, buf, responseSender) -> {
			FriendlyByteBuf friendlyByteBuf = buf;
			Item changedItem = friendlyByteBuf.readItem().getItem();
			int newAmount = friendlyByteBuf.getInt(1);

			((ItemExtensions)changedItem).setStackSize(newAmount);
		});
	}
}
