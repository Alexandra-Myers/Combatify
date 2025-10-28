package net.atlas.combatify.networking;

import net.atlas.combatify.extensions.ClientInformationHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ServerboundClientInformationExtensionPacket(boolean useShieldOnCrouch) {
	public static ServerboundClientInformationExtensionPacket decode(FriendlyByteBuf buf) {
		return new ServerboundClientInformationExtensionPacket(buf.readBoolean());
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeBoolean(useShieldOnCrouch);
	}
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ServerPlayer player = ctx.get().getSender();
		if (player == null)
			return;
		((ClientInformationHolder) player).combatify$setShieldOnCrouch(useShieldOnCrouch);
	}
}
