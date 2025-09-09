package net.azisaba.rankingdisplayer.holo.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import net.azisaba.kdstatusreloaded.api.KDSAPI;
import net.azisaba.rankingdisplayer.holo.data.RankingType;

import java.time.Duration;
import java.util.HashMap;
import java.util.UUID;

public class PlayerRankCache {
    protected static final HashMap<RankingType, LoadingCache<UUID, Integer>> cacheMap = new HashMap<>();

    protected static LoadingCache<UUID, Integer> getCache(RankingType rankingType) {
        if (!cacheMap.containsKey(rankingType)) cacheMap.put(
                rankingType,
                Caffeine.newBuilder()
                        .maximumSize(10_000)
                        .expireAfterAccess(Duration.ofMinutes(15))
                        .refreshAfterWrite(Duration.ofSeconds(30))
                        .build(key -> KDSAPI.getPlayerRanking(rankingType.killCountType, key))
        );
        return cacheMap.get(rankingType);
    }

    public static void shutdown() {
        cacheMap.clear();
    }

    public static int getRank(RankingType type, UUID uuid) {
        var result = getCache(type).get(uuid);
        if (result == null) return -1;
        return result;
    }
}
