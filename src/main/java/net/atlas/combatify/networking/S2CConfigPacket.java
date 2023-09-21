package net.atlas.combatify.networking;

import net.atlas.combatify.client.ClientConfigHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static net.atlas.combatify.config.ConfigSynchronizer.write;

public class S2CConfigPacket {

	public S2CConfigPacket() {
	}

	public static S2CConfigPacket decode(FriendlyByteBuf buf) {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientConfigHandlers.generalConfigPacket(buf));
		return new S2CConfigPacket();
	}

	public void encode(FriendlyByteBuf buf) {
		write(buf, 2);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {

	}
}
