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
				boolean oldValue = AtlasConfig.fistDamage;
				AtlasConfig.fistDamage = buf.getBoolean(5);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.fistDamage = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			AtlasConfig.swordBlocking = buf.getBoolean(6);
			AtlasConfig.saturationHealing = buf.getBoolean(7);
			AtlasConfig.autoAttackAllowed = buf.getBoolean(8);
			if(AtlasConfig.axeReachBuff != buf.getBoolean(9)) {
				boolean oldValue = AtlasConfig.axeReachBuff;
				AtlasConfig.axeReachBuff = buf.getBoolean(9);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.axeReachBuff = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			if(AtlasConfig.blockReach != buf.getBoolean(10)) {
				boolean oldValue = AtlasConfig.blockReach;
				AtlasConfig.blockReach = buf.getBoolean(10);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.blockReach = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			if(AtlasConfig.attackReach != buf.getBoolean(11)) {
				boolean oldValue = AtlasConfig.attackReach;
				AtlasConfig.attackReach = buf.getBoolean(11);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.attackReach = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			AtlasConfig.attackSpeed = buf.getBoolean(12);
			if(AtlasConfig.ctsAttackBalancing != buf.getBoolean(13)) {
				boolean oldValue = AtlasConfig.ctsAttackBalancing;
				AtlasConfig.ctsAttackBalancing = buf.getBoolean(13);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.ctsAttackBalancing = oldValue;
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
			AtlasConfig.eggDamage = buf.getFloat(1);
			AtlasConfig.bowUncertainty = buf.getFloat(2);
			if(AtlasConfig.swordAttackDamage != buf.getFloat(3)) {
				float oldValue = AtlasConfig.swordAttackDamage;
				AtlasConfig.swordAttackDamage = buf.getFloat(3);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.swordAttackDamage = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			if(AtlasConfig.axeAttackDamage != buf.getFloat(4)) {
				float oldValue = AtlasConfig.axeAttackDamage;
				AtlasConfig.axeAttackDamage = buf.getFloat(4);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.axeAttackDamage = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			if(AtlasConfig.baseHoeAttackDamage != buf.getFloat(5)) {
				float oldValue = AtlasConfig.baseHoeAttackDamage;
				AtlasConfig.baseHoeAttackDamage = buf.getFloat(5);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.baseHoeAttackDamage = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			if(AtlasConfig.ironDiaHoeAttackDamage != buf.getFloat(6)) {
				float oldValue = AtlasConfig.ironDiaHoeAttackDamage;
				AtlasConfig.ironDiaHoeAttackDamage = buf.getFloat(6);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.ironDiaHoeAttackDamage = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			if(AtlasConfig.netheriteHoeAttackDamage != buf.getFloat(7)) {
				float oldValue = AtlasConfig.netheriteHoeAttackDamage;
				AtlasConfig.netheriteHoeAttackDamage = buf.getFloat(7);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.netheriteHoeAttackDamage = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			if(AtlasConfig.tridentAttackDamage != buf.getFloat(8)) {
				float oldValue = AtlasConfig.tridentAttackDamage;
				AtlasConfig.tridentAttackDamage = buf.getFloat(8);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.tridentAttackDamage = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			if(AtlasConfig.swordAttackSpeed != buf.getFloat(9)) {
				float oldValue = AtlasConfig.swordAttackSpeed;
				AtlasConfig.swordAttackSpeed = buf.getFloat(9);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.swordAttackSpeed = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			if(AtlasConfig.axeAttackSpeed != buf.getFloat(10)) {
				float oldValue = AtlasConfig.axeAttackSpeed;
				AtlasConfig.axeAttackSpeed = buf.getFloat(10);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.axeAttackSpeed = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			if(AtlasConfig.woodenHoeAttackSpeed != buf.getFloat(11)) {
				float oldValue = AtlasConfig.woodenHoeAttackSpeed;
				AtlasConfig.woodenHoeAttackSpeed = buf.getFloat(11);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.woodenHoeAttackSpeed = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			if(AtlasConfig.stoneHoeAttackSpeed != buf.getFloat(12)) {
				float oldValue = AtlasConfig.stoneHoeAttackSpeed;
				AtlasConfig.stoneHoeAttackSpeed = buf.getFloat(12);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.stoneHoeAttackSpeed = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			if(AtlasConfig.ironHoeAttackSpeed != buf.getFloat(13)) {
				float oldValue = AtlasConfig.ironHoeAttackSpeed;
				AtlasConfig.ironHoeAttackSpeed = buf.getFloat(13);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.ironHoeAttackSpeed = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			if(AtlasConfig.goldDiaNethHoeAttackSpeed != buf.getFloat(14)) {
				float oldValue = AtlasConfig.goldDiaNethHoeAttackSpeed;
				AtlasConfig.goldDiaNethHoeAttackSpeed = buf.getFloat(14);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.goldDiaNethHoeAttackSpeed = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			if(AtlasConfig.tridentAttackSpeed != buf.getFloat(15)) {
				float oldValue = AtlasConfig.tridentAttackSpeed;
				AtlasConfig.tridentAttackSpeed = buf.getFloat(15);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.tridentAttackSpeed = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			if(AtlasConfig.defaultAttackSpeed != buf.getFloat(16)) {
				float oldValue = AtlasConfig.defaultAttackSpeed;
				AtlasConfig.defaultAttackSpeed = buf.getFloat(16);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.defaultAttackSpeed = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			if(AtlasConfig.slowestToolAttackSpeed != buf.getFloat(17)) {
				float oldValue = AtlasConfig.slowestToolAttackSpeed;
				AtlasConfig.slowestToolAttackSpeed = buf.getFloat(17);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.slowestToolAttackSpeed = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			if(AtlasConfig.slowToolAttackSpeed != buf.getFloat(18)) {
				float oldValue = AtlasConfig.slowToolAttackSpeed;
				AtlasConfig.slowToolAttackSpeed = buf.getFloat(18);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.slowToolAttackSpeed = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			if(AtlasConfig.fastToolAttackSpeed != buf.getFloat(19)) {
				float oldValue = AtlasConfig.fastToolAttackSpeed;
				AtlasConfig.fastToolAttackSpeed = buf.getFloat(19);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.fastToolAttackSpeed = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
			if(AtlasConfig.fastestToolAttackSpeed != buf.getFloat(20)) {
				float oldValue = AtlasConfig.fastestToolAttackSpeed;
				AtlasConfig.fastestToolAttackSpeed = buf.getFloat(20);
				AtlasConfig.write("atlas-combat");
				AtlasConfig.fastestToolAttackSpeed = oldValue;
				handler.getConnection().disconnect(new TextComponent("Cannot connect to this server without restarting due to a config mismatch!"));
			}
		});
	}
}
