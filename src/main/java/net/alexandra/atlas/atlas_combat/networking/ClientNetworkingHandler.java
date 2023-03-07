package net.alexandra.atlas.atlas_combat.networking;

import net.alexandra.atlas.atlas_combat.config.AtlasConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.TextComponent;

import static net.alexandra.atlas.atlas_combat.AtlasCombat.modDetectionNetworkChannel;

public class ClientNetworkingHandler {
	public ClientNetworkingHandler() {

		ClientPlayNetworking.registerGlobalReceiver(modDetectionNetworkChannel,(client, handler, buf, responseSender) -> {
			AtlasConfig.toolsAreWeapons = buf.getBoolean(0);
			if(AtlasConfig.bedrockBlockReach != buf.getBoolean(1)) {
				boolean oldValue = AtlasConfig.bedrockBlockReach;
				AtlasConfig.bedrockBlockReach = buf.getBoolean(1);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.bedrockBlockReach = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			AtlasConfig.refinedCoyoteTime = buf.getBoolean(2);
			AtlasConfig.midairKB = buf.getBoolean(3);
			AtlasConfig.fishingHookKB = buf.getBoolean(4);
			if(AtlasConfig.fistDamage != buf.getBoolean(5)) {
				boolean oldValue = AtlasConfig.bedrockBlockReach;
				AtlasConfig.fistDamage = buf.getBoolean(5);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.fistDamage = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			AtlasConfig.swordBlocking = buf.getBoolean(6);
			AtlasConfig.saturationHealing = buf.getBoolean(7);
			if(AtlasConfig.axeReachBuff != buf.getBoolean(8)) {
				boolean oldValue = AtlasConfig.bedrockBlockReach;
				AtlasConfig.axeReachBuff = buf.getBoolean(8);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.axeReachBuff = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			if(AtlasConfig.blockReach != buf.getBoolean(9)) {
				boolean oldValue = AtlasConfig.bedrockBlockReach;
				AtlasConfig.blockReach = buf.getBoolean(9);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.blockReach = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			if(AtlasConfig.attackReach != buf.getBoolean(10)) {
				boolean oldValue = AtlasConfig.bedrockBlockReach;
				AtlasConfig.attackReach = buf.getBoolean(10);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.attackReach = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			AtlasConfig.eatingInterruption = buf.getBoolean(11);
			AtlasConfig.swordProtectionEfficacy = buf.getInt(0);
			AtlasConfig.potionUseDuration = buf.getInt(1);
			AtlasConfig.honeyBottleUseDuration = buf.getInt(2);
			AtlasConfig.milkBucketUseDuration = buf.getInt(3);
			AtlasConfig.stewUseDuration = buf.getInt(4);
			AtlasConfig.instantHealthBonus = buf.getInt(5);
			AtlasConfig.eggItemCooldown = buf.getInt(6);
			AtlasConfig.snowballItemCooldown = buf.getInt(7);
			AtlasConfig.snowballDamage = buf.getFloat(0);
			AtlasConfig.bowUncertainty = buf.getFloat(1);

		});
	}
}
