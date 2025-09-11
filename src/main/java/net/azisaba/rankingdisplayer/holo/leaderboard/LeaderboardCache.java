package net.azisaba.rankingdisplayer.holo.leaderboard;

import net.azisaba.kdstatusreloaded.api.KDSAPI;
import net.azisaba.kdstatusreloaded.playerkd.model.KDUserData;
import net.azisaba.rankingdisplayer.RankingDisplayer;
import net.azisaba.rankingdisplayer.holo.data.RankingType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LeaderboardCache {
    private static final int RANKING_SIZE = 7;
    private static final UUID EMPTY_UUID = UUID.fromString("4c47fc44-824a-42be-87fc-44824a32be54");
    protected final ConcurrentHashMap<RankingType, List<LeaderboardLineData>> rankingCache = new ConcurrentHashMap<>();
    private final BukkitRunnable updater;

    public LeaderboardCache(RankingDisplayer plugin) {
        updater = new BukkitRunnable() {
            @Override
            public void run() {
                doUpdate();
            }
        };

        // Update leaderboard every 30 seconds
        updater.runTaskTimerAsynchronously(plugin, 10, 20 * 30);
    }

    public void shutdown() {
        updater.cancel();
    }

    public List<LeaderboardLineData> getLines(RankingType type) {
        return rankingCache.get(type);
    }

    protected void doUpdate() {
        for (RankingType type : RankingType.values()) {
            List<KDUserData> topList = KDSAPI.getTops(type.killCountType, RANKING_SIZE);

            List<LeaderboardLineData> rankingList = new ArrayList<>();
            for (int i = 0; i < RANKING_SIZE; i++) {
                if (topList.size() <= i) {
                    // If not available
                    rankingList.add(RankingEntryLine.getLine(
                            EMPTY_UUID,
                            "",
                            i + 1,
                            -1
                    ));
                } else {
                    KDUserData kdUserData = topList.get(i);
                    rankingList.add(RankingEntryLine.getLine(
                            kdUserData.uuid,
                            kdUserData.name,
                            i + 1,
                            type.getKill(kdUserData)
                    ));
                }
            }
            rankingCache.put(type, rankingList);
        }
    }
}
