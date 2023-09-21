package net.atlas.combatify.networking;

import net.atlas.combatify.extensions.PlayerExtensions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class ServerboundMissPacket {
	public ServerboundMissPacket() {
	}

	public static ServerboundMissPacket decode(FriendlyByteBuf buf) {
		return new ServerboundMissPacket();
	}

	public void encode(FriendlyByteBuf buf) {
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		((PlayerExtensions) Objects.requireNonNull(ctx.get().getSender())).attackAir();
	}
}
