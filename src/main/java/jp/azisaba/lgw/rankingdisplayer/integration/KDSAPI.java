package jp.azisaba.lgw.rankingdisplayer.integration;

import jp.azisaba.lgw.kdstatus.KDStatusReloaded;
import jp.azisaba.lgw.kdstatus.sql.KillRankingData;
import jp.azisaba.lgw.kdstatus.utils.TimeUnit;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class KDSAPI {
    private static final String PLUGIN_NAME = "KDStatusReloaded";
    private static KDStatusReloaded kdsPlugin;
    @Getter
    private static boolean loaded = false;

    /**
     * This function needs to call when KDStatusReloaded was loaded or reloaded
     * @param logger info & error logger
     */
    public static void loadPlugin(Logger logger) {
        Plugin pl = Bukkit.getPluginManager().getPlugin("KDStatusReloaded");
        if(pl != null) {
            kdsPlugin = (KDStatusReloaded) pl;
            loaded = true;
            logger.info("Successfully to load KDStatusReloaded plugin instance");
        } else {
            loaded = false;
            logger.warning("Failed to load KDStatusReloaded plugin instance");
        }
    }

    /**
     * Get ranking order of target player
     * @param playerUUID UUID of target player
     * @param timeUnit TimeUnit of target ranking
     * @return Ranking order of player. If plugin is not loaded, returns -1
     */
    public static int getPlayerRanking(UUID playerUUID, TimeUnit timeUnit) {
        if(!loaded) return -1;
        return kdsPlugin.getKdDataContainer().getRanking(playerUUID, timeUnit);
    }

    /**
     * Get kill count of target player
     * @param targetPlayer Target player
     * @param timeUnit TimeUnit of kill count
     * @return kill count of target player in timeUnit. If plugin is not loaded, returns -1
     */
    public static int getPlayerKills(Player targetPlayer, TimeUnit timeUnit) {
        if(!loaded) return -1;
        return kdsPlugin.getKdDataContainer().getPlayerData(targetPlayer, true).getKills(timeUnit);
    }

    /**
     * Get kill ranking of target duration
     * @param timeUnit TimeUnit of ranking
     * @param MAX_SIZE Maximum size of kill ranking
     * @return List of {@link KillRankingData}. If plugin is not loaded, returns empty list
     */
    public static  List<KillRankingData> getTopKillRanking(TimeUnit timeUnit, int MAX_SIZE) {
        if(!loaded) return Collections.emptyList();
        return kdsPlugin.getKdDataContainer().getTopKillRankingData(timeUnit, MAX_SIZE);
    }
}
