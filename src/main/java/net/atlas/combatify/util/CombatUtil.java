package net.atlas.combatify.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.function.TriFunction;

/**
 *  Credits to <a href="https://github.com/Blumbo/CTS-AntiCheat/tree/master">Blumbo's CTS Anti-Cheat</a>, integrated into Combatify from there <br>
 *  <h4>Licensed under MIT</h4> <br>
 *  General Combat improvement utility used across several classes
 */
public class CombatUtil {

    /**
     * The amount of ticks players' previous locations are saved for <br>
     * A somewhat good rule for the optimal amount of ticks is ceiling(x/50ms) + 2 <br>
     * Where x is the highest ping (in milliseconds) the anti-cheat takes into account. <br>
     * E.g. if the highest "accepted" ping is 151ms-200ms this number would be 6, for 251-300ms it would be 8 etc.
     */
    public static int savedLocationTicks = 9;

    /**
     * If target is not in reach (possibly due to ping) check if target's previous locations are in reach
     */
    public static boolean allowReach(ServerPlayer attacker, ServerPlayer target, TriFunction<ServerPlayer, AABB, Double, Boolean> reachCheck) {
        MethodHandler.getCurrentAttackReach(attacker, 1F); // Force reach update

        if (reachCheck.apply(attacker, target.getBoundingBox(), 0.75)) return true;

        PlayerData victimData = PlayerData.get(target);
        for (AABB boundingBox : victimData.previousPositions) {
            if (boundingBox == null) continue;
            if (reachCheck.apply(attacker, boundingBox, 0.75)) return true;
        }

        return false;
    }

    /**
     * Updates player's positions. Used once every tick.
     */
    public static void setPosition(ServerPlayer player) {
        PlayerData playerData = PlayerData.get(player);

        playerData.positionIndex++;
        if (playerData.positionIndex >= playerData.previousPositions.length) playerData.positionIndex = 0;

        playerData.previousPositions[playerData.positionIndex] = player.getBoundingBox();
    }

}
