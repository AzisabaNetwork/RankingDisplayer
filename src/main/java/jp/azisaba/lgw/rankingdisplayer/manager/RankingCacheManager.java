package jp.azisaba.lgw.rankingdisplayer.manager;

import jp.azisaba.lgw.kdstatus.sql.KillRankingData;
import jp.azisaba.lgw.rankingdisplayer.integration.KDSAPI;
import jp.azisaba.lgw.rankingdisplayer.ranking.RankingType;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class RankingCacheManager {
    @Getter
    private static final RankingCacheManager Instance = new RankingCacheManager();
    @Setter
    private Logger logger;
    private final long cacheHoldMilliSec = 1000 * 10;

    private final HashMap<RankingType, List<KillRankingData>> killRankingCache = new HashMap<>();

    private final HashMap<RankingType, Long> lastUpdated = new HashMap<>();

    // TODO move this to config
    private static final int RANKING_SIZE = 7;

    public long getLastUpdateTimeMill(RankingType type) {
        return lastUpdated.get(type);
    }

    public long getLastUpdateAgo(RankingType type) {
        return lastUpdated.get(type) - System.currentTimeMillis();
    }

    public boolean isNeedToUpdateCache(RankingType type) {
        return getLastUpdateAgo(type) >= cacheHoldMilliSec;
    }

    private synchronized void updateCache(RankingType type) {
        List<KillRankingData> dataList = KDSAPI.getTopKillRanking(type.getKdStatusTimeUnit(), RANKING_SIZE);
        if(dataList.isEmpty()) {
            logger.warning("Failed to get top kill ranking");
        } else {
            killRankingCache.put(type, dataList);
        }
        lastUpdated.put(type, System.currentTimeMillis());
    }

    public List<String> getRankingAsArray(RankingType type, Player p) {
        List<String> lines = new ArrayList<>();
        int rank = 0;
        int before = Integer.MIN_VALUE;
        boolean containsHim = false;
        
        if(isNeedToUpdateCache(type)) updateCache(type);
        List<KillRankingData> dataList = killRankingCache.get(type);
        int count = 0;
        for(KillRankingData data: dataList) {
            if(count >= RANKING_SIZE) {
                break;
            }
            
            int kills = data.getKills();
            
            if(kills <= 0) {
                break;
            }
            
            UUID uuid = data.getUuid();
            String playerName = data.getName();

            count++;

            boolean hide = RankingHideManager.isHiding(uuid);
            if (hide && !playerName.equals(p.getName())) {
                playerName = ChatColor.DARK_RED + "{匿名プレイヤー}";
            }
            
            if(kills != before) {
                rank = count;
                before = kills;
            }

            String line = ChatColor.YELLOW + "" + rank + "位 " + ChatColor.GOLD + "{PLAYER}" + ChatColor.RED + ": "
                    + ChatColor.AQUA + kills + " kill(s)";

            if (playerName.equals(p.getName())) {
                line = ChatColor.BLUE + "YOU" + ChatColor.RED + " » " + line;
                line = line.replace("{PLAYER}", p.getName());
                containsHim = true;

                if (hide) {
                    line = ChatColor.DARK_RED + "(Hide) " + line;
                }
            } else {
                line = line.replace("{PLAYER}", playerName);
            }
            lines.add(line);
        }
        
        while(count < RANKING_SIZE) {
            count++;
            lines.add(ChatColor.YELLOW + "" + count + "位 " + ChatColor.GOLD + "なし");
        }
        
        lines.add(ChatColor.AQUA + StringUtils.repeat("=", 25));

        if (!containsHim) {
            int ranking = KDSAPI.getPlayerRanking(p.getUniqueId(), type.getKdStatusTimeUnit());
            int kills = KDSAPI.getPlayerKills(p, type.getKdStatusTimeUnit());

            String rankingStr = "" + ranking;
            if (ranking <= 0 || kills <= 0) {
                rankingStr = "-";
            }

            String line = ChatColor.BLUE + "YOU" + ChatColor.RED + " » " + ChatColor.YELLOW + rankingStr + "位 " +
                    ChatColor.GOLD + p.getName() + ChatColor.RED + ": " + ChatColor.AQUA + kills + " kill(s)";

            if (RankingHideManager.isHiding(p.getUniqueId())) {
                line = ChatColor.DARK_RED + "(Hide) " + line;
            }
            lines.add(line);
        }

        return lines;
    }
}
