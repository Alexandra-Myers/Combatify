package net.atlas.combatify.mixin;

import io.netty.buffer.Unpooled;
import io.wispforest.owo.config.ConfigSynchronizer;
import io.wispforest.owo.config.Option;
import net.atlas.combatify.Combatify;
import net.atlas.combatify.extensions.ConfigSyncBase;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ConfigSynchronizer.class)
public abstract class OwoConfigSyncMixin implements ConfigSyncBase {
	@Shadow
	private static void applyClient(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender sender) {

	}

	private static void write(FriendlyByteBuf packet, Option.SyncMode targetMode) {
		packet.writeVarInt(1);

		var configBuf = PacketByteBufs.create();
		var optionBuf = PacketByteBufs.create();
		packet.writeUtf("combatify-config");

		configBuf.writeVarInt((int) Combatify.CONFIG.allOptions().values().stream().filter(option -> option.syncMode().ordinal() >= targetMode.ordinal()).count());

		Combatify.CONFIG.allOptions().forEach((key, option) -> {
			if (option.syncMode().ordinal() < targetMode.ordinal()) return;

			configBuf.writeUtf(key.asString());

			optionBuf.resetReaderIndex().resetWriterIndex();
			if (option.defaultValue() instanceof Boolean bool)
				optionBuf.writeBoolean(bool);
			else if (option.defaultValue() instanceof Integer i)
				optionBuf.writeInt(i);
			else if (option.defaultValue() instanceof Float f)
				optionBuf.writeFloat(f);

			configBuf.writeVarInt(optionBuf.readableBytes());
			configBuf.writeBytes(optionBuf);
		});

		packet.writeVarInt(configBuf.readableBytes());
		packet.writeBytes(configBuf);
	}

	@Override
	public void applyDefault() {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		write(buf, Option.SyncMode.OVERRIDE_CLIENT);
		applyClient(Minecraft.getInstance(), Minecraft.getInstance().getConnection(), buf, ClientPlayNetworking.getSender());
	}
}
