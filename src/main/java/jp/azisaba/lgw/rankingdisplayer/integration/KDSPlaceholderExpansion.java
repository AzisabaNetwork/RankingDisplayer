package jp.azisaba.lgw.rankingdisplayer.integration;

import jp.azisaba.lgw.rankingdisplayer.manager.RankingCacheManager;
import jp.azisaba.lgw.rankingdisplayer.ranking.RankingType;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class KDSPlaceholderExpansion extends PlaceholderExpansion {
    /**
     * Holds all placeholder names for KDStatusReloaded player data
     */
    public static class Names {
        public static final String IDENTIFIER = "rankingdisplayer";
        public static final String KILL_RANKING = "kill_ranking";
        public static final String PLAYER_RANKING = "player_ranking";
        public static final String PLAYER_KILL_COUNT = "player_kill_count";
        public static final String LAST_UPDATE = "last_update";

        public static String getKillRankingPlaceholder(String timeUnitName, int order) {
            return "%" + IDENTIFIER + "_" + KILL_RANKING + "_" + timeUnitName + "_" + order + "%";
        }
    }

    @Override
    public @NotNull String getIdentifier() {
        return Names.IDENTIFIER;
    }

    @Override
    public @NotNull String getAuthor() {
        return "azisaba";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.1";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String name) {
        // Check prefix
//        String name = params.split(getIdentifier() + "_")[1];

        // === Handlers ===
        // %rankingdisplay_kill_ranking_{timeUnitName}_{order}%
        if (name.startsWith(Names.KILL_RANKING)) {
            String[] args = name.split(Names.KILL_RANKING + "_")[1].split("_", 2);
            RankingType type = RankingType.getType(args[0]);
            if (type == null) return null;
            String numStr = args[1];
            int order;
            try {
                order = Integer.parseInt(numStr);
                return RankingCacheManager.getInstance().getKillRankingLine(type, order, player.getName());
            } catch (NumberFormatException e) {
                // If failed to parse
                return null;
            }
        }

        // %rankingdisplay_player_ranking_{timeunit}%
        if (name.startsWith(Names.PLAYER_RANKING)) {
            name = name.split(Names.PLAYER_RANKING + "_")[1];
            RankingType type = RankingType.getType(name.toLowerCase(Locale.ROOT));
            if (type == null) return null;
            return String.valueOf(KDSAPI.getPlayerRanking(player.getUniqueId(), type.getKdStatusTimeUnit()));
        }

        // %rankingdisplay_player_kill_count_{timeunit}%
        if (name.startsWith(Names.PLAYER_KILL_COUNT)) {
            name = name.split(Names.PLAYER_KILL_COUNT + "_")[1];
            RankingType type = RankingType.getType(name.toLowerCase(Locale.ROOT));
            if (type == null) return null;
            return String.valueOf(KDSAPI.getPlayerKills(player.getPlayer(), type.getKdStatusTimeUnit()));
        }

        // %rankingdisplay_last_update_{timeunit}%
        if (name.startsWith(Names.LAST_UPDATE)) {
            name = name.split(Names.LAST_UPDATE + "_")[1];
            RankingType type = RankingType.getType(name.toLowerCase(Locale.ROOT));
            if (type == null) return null;
            double n = Math.floor((double) RankingCacheManager.getInstance().getLastUpdateAgo(type) / 1000);
            return n < 1 ? "今" : String.valueOf(n);
        }
        return null;
    }
}
