package net.alexandra.atlas.atlas_combat.networking;

import io.netty.buffer.Unpooled;
import net.alexandra.atlas.atlas_combat.config.AtlasConfig;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;

import static net.alexandra.atlas.atlas_combat.AtlasCombat.modDetectionNetworkChannel;

public class NetworkingHandler {

	public NetworkingHandler() {
		ServerPlayConnectionEvents.JOIN.register(modDetectionNetworkChannel,(handler, sender, server) -> {
			FriendlyByteBuf packetBuf = new FriendlyByteBuf(Unpooled.buffer());
			packetBuf.writeBoolean(AtlasConfig.toolsAreWeapons);
			packetBuf.writeBoolean(AtlasConfig.bedrockBlockReach);
			packetBuf.writeBoolean(AtlasConfig.refinedCoyoteTime);
			packetBuf.writeBoolean(AtlasConfig.midairKB);
			packetBuf.writeBoolean(AtlasConfig.fishingHookKB);
			packetBuf.writeBoolean(AtlasConfig.fistDamage);
			packetBuf.writeBoolean(AtlasConfig.swordBlocking);
			packetBuf.writeBoolean(AtlasConfig.saturationHealing);
			packetBuf.writeBoolean(AtlasConfig.axeReachBuff);
			packetBuf.writeBoolean(AtlasConfig.blockReach);
			packetBuf.writeBoolean(AtlasConfig.attackReach);
			packetBuf.writeBoolean(AtlasConfig.eatingInterruption);
			packetBuf.writeInt(AtlasConfig.swordProtectionEfficacy);
			packetBuf.writeInt(AtlasConfig.potionUseDuration);
			packetBuf.writeInt(AtlasConfig.honeyBottleUseDuration);
			packetBuf.writeInt(AtlasConfig.milkBucketUseDuration);
			packetBuf.writeInt(AtlasConfig.stewUseDuration);
			packetBuf.writeInt(AtlasConfig.instantHealthBonus);
			packetBuf.writeInt(AtlasConfig.eggItemCooldown);
			packetBuf.writeInt(AtlasConfig.snowballItemCooldown);
			packetBuf.writeFloat(AtlasConfig.snowballDamage);
			packetBuf.writeFloat(AtlasConfig.bowUncertainty);
			ServerPlayNetworking.send(handler.player, modDetectionNetworkChannel,packetBuf);
		});
	}
}
