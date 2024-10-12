package jp.azisaba.lgw.rankingdisplayer.manager;

import jp.azisaba.lgw.kdstatus.sql.KillRankingData;
import jp.azisaba.lgw.rankingdisplayer.integration.KDSAPI;
import jp.azisaba.lgw.rankingdisplayer.ranking.RankingData;
import jp.azisaba.lgw.rankingdisplayer.ranking.RankingType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class RankingCacheManager {
    @Getter
    private static final RankingCacheManager Instance = new RankingCacheManager();
    @Setter
    private Logger logger;
    private final long cacheHoldMilliSec = 1000 * 10;

    private final ConcurrentHashMap<RankingType, List<RankingData>> rankingCache = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<RankingType, Long> lastUpdated = new ConcurrentHashMap<>();

    // TODO move this to config
    private static final int RANKING_SIZE = 7;

    public long getLastUpdateTimeMill(RankingType type) {
        return lastUpdated.get(type);
    }

    public long getLastUpdateAgo(RankingType type) {
        long from = lastUpdated.getOrDefault(type, (long) 0);
        if (from == 0) return 0;
        return System.currentTimeMillis() - from;
    }

    public boolean isNeedToUpdateCache(RankingType type) {
        return getLastUpdateAgo(type) >= cacheHoldMilliSec;
    }

    public synchronized void updateCache(RankingType type) {
        List<KillRankingData> dataList = KDSAPI.getTopKillRanking(type.getKdStatusTimeUnit(), RANKING_SIZE);
        List<RankingData> parsedData = parseData(dataList);
        if (parsedData.isEmpty()) {
            logger.warning("Failed to get top kill ranking");
        } else {
            rankingCache.put(type, parsedData);
        }
        lastUpdated.put(type, System.currentTimeMillis());
    }

    public void loadRanking() {
        for (RankingType t : RankingType.values()) {
            logger.info("Loading ranking of " + t.name().toLowerCase(Locale.ROOT));
            updateCache(t);
            logger.info("Loaded! ranking of " + t.name().toLowerCase(Locale.ROOT));
        }
    }

    private List<RankingData> parseData(List<KillRankingData> dataList) {
        int num = 0;
        int rank = 0;
        int before = Integer.MIN_VALUE;
        List<RankingData> rankingDataList = new ArrayList<>();

        for (KillRankingData data : dataList) {
            if (num >= 7) break;

            int kill = data.getKills();
            if (kill <= 0) break;

            UUID uuid = data.getUuid();
            String playerName = data.getName();
            boolean hiding = RankingHideManager.isHiding(uuid);

            num++;

            if (kill != before) {
                rank = num;
                before = kill;
            }

            rankingDataList.add(new RankingData(rank, playerName, kill, hiding));
        }

        while (num < 7) {
            num++;
            rankingDataList.add(new RankingData(num, "なし", 0, false));
        }

        return rankingDataList;
    }

    public String getKillRankingLine(RankingType type, int order, String targetPlayerName) {
        if (RANKING_SIZE < order) {
            return null;
        }
        if (isNeedToUpdateCache(type)) updateCache(type);
        List<RankingData> d = rankingCache.get(type);
        if (d == null) return "what?";
        return d.get(order - 1).getLine(targetPlayerName);
    }
}
