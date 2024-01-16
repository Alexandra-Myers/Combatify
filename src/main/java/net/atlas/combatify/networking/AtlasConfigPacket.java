package net.atlas.combatify.networking;

import net.atlas.combatify.config.AtlasConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record AtlasConfigPacket(AtlasConfig config) {
	public static AtlasConfigPacket decode(FriendlyByteBuf buf) {
		return new AtlasConfigPacket(AtlasConfig.staticLoadFromNetwork(buf));
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeResourceLocation(config.name);
		config.saveToNetwork(buf);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		config.handleExtraSync(ctx);
	}
}
