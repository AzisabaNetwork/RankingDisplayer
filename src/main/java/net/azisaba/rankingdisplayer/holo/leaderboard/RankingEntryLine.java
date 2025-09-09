package net.azisaba.rankingdisplayer.holo.leaderboard;

import org.bukkit.ChatColor;

import java.util.UUID;

public class RankingEntryLine {
    public static LeaderboardLineData getLine(UUID uuid, int rank, int kill) {
        boolean isNothing = kill <= 0;
        String rankPrefix = ChatColor.YELLOW + String.format("%d位", rank);
        if (isNothing) {
            // If not available
            rankPrefix += ChatColor.GOLD + " なし";
        }

        String killSuffix = ChatColor.RED + ": " + ChatColor.AQUA + kill + " kill(s)";
        return new LeaderboardLineData(
                uuid,
                isNothing,
                rankPrefix,
                killSuffix
        );
    }
}
