package net.atlas.combatify.networking;

import io.netty.buffer.Unpooled;
import net.atlas.combatify.Combatify;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.Objects;
import java.util.function.Supplier;

import static net.atlas.combatify.Combatify.ITEMS;
import static net.atlas.combatify.config.ConfigSynchronizer.*;

public class C2SConfigPacket {
	public FriendlyByteBuf buf;

	public C2SConfigPacket() {
		buf = new FriendlyByteBuf(Unpooled.buffer());
	}

	public static C2SConfigPacket decode(FriendlyByteBuf buf) {
		C2SConfigPacket packet = new C2SConfigPacket();
		packet.buf = buf;
		return packet;
	}

	public void encode(FriendlyByteBuf buf) {
		write(buf, 1);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		applyServer(Objects.requireNonNull(ctx.get().getSender()), buf);
		PacketRegistration.MAIN.send(PacketDistributor.PLAYER.with(() -> ctx.get().getSender()), new ItemConfigPacket(ITEMS));
		Combatify.LOGGER.info("Config packet sent to client.");
	}
}
