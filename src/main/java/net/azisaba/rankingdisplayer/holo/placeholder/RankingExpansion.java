package net.azisaba.rankingdisplayer.holo.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.azisaba.kdstatusreloaded.api.KDSAPI;
import net.azisaba.rankingdisplayer.RankingDisplayer;
import net.azisaba.rankingdisplayer.holo.data.RankingType;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class RankingExpansion extends PlaceholderExpansion {
    @Override
    public @NonNull String getIdentifier() {
        return Names.IDENTIFIER;
    }

    @Override
    public @NonNull String getAuthor() {
        return "azisaba";
    }

    @Override
    public @NonNull String getVersion() {
        return "1.1.0";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NonNull String name) {

        String[] args = name.split("_");
//        System.out.println("Got " + name);
        if (args.length < 2) return null;

        String type = args[0];
        RankingType rankingType = RankingType.getType(args[1]);
        if (rankingType == null) return null;

        switch (type) {
            // %rankingdisplay_killranking_{timeUnitName}_{order}%
            case Names.KILL_RANKING -> {
                if (args.length != 3) return null;
                try {
                    int order = Integer.parseInt(args[2]);
                    return RankingDisplayer.getLeaderboardCache().getLines(rankingType).get(order - 1).getLine(player.getName());
                } catch (NumberFormatException e) {
                    return null;
                }
            }

            // %rankingdisplay_playerranking_{timeunit}%
            case Names.PLAYER_RANKING -> {
                int rank = KDSAPI.getPlayerRanking(rankingType.killCountType, player.getUniqueId());
                return "" + (rank == -1 ? "?" : rank);
            }

            // %rankingdisplay_playerkillcount_{timeunit}%
            case Names.PLAYER_KILL_COUNT -> {
                return "" + rankingType.getKill(KDSAPI.getPlayerData(player.getUniqueId()));
            }
        }
        return null;
    }

    /**
     * Holds all placeholder names for KDStatusReloaded player data
     */
    public static class Names {
        public static final String IDENTIFIER = "rankingdisplayer";
        public static final String KILL_RANKING = "killranking";
        public static final String PLAYER_RANKING = "playerranking";
        public static final String PLAYER_KILL_COUNT = "playerkillcount";

        public static String getKillRankingPlaceholder(String type, int order) {
            return "%" + IDENTIFIER + "_" + KILL_RANKING + "_" + type + "_" + order + "%";
        }
    }
}
