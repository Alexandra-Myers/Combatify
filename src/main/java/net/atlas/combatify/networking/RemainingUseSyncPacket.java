package net.atlas.combatify.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record RemainingUseSyncPacket(int id, int ticks) {
	public static RemainingUseSyncPacket decode(FriendlyByteBuf buf) {
		return new RemainingUseSyncPacket(buf.readVarInt(), buf.readInt());
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeVarInt(id);
		buf.writeInt(ticks);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientOnly.setUseRemainingTicks(this, ctx));
	}
}
