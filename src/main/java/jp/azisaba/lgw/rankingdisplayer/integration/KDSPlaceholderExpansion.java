package jp.azisaba.lgw.rankingdisplayer.integration;

import jp.azisaba.lgw.rankingdisplayer.ranking.RankingType;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KDSPlaceholderExpansion extends PlaceholderExpansion {
    public static class Names {
        public static final String PLAYER_RANKING = "player_ranking";
        public static final String PLAYER_KILL_COUNT = "player_kill_count";
    }
    @Override
    public @NotNull String getIdentifier() {
        return "rankingdisplay";
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
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if(params.startsWith(getIdentifier() + "_")) {
            String name = params.split(getIdentifier() + "_")[1];
            if(name.startsWith(Names.PLAYER_RANKING)) {
                name = name.split(Names.PLAYER_RANKING + "_")[1];
                RankingType type = RankingType.getType(name);
                if(type==null) return null;
                return String.valueOf(KDSAPI.getPlayerRanking(player.getUniqueId(), type.getKdStatusTimeUnit()));
            } else if (name.startsWith(Names.PLAYER_KILL_COUNT)) {
                name = name.split(Names.PLAYER_KILL_COUNT + "_")[1];
                RankingType type = RankingType.getType(name);
                if(type==null) return null;
                return String.valueOf(KDSAPI.getPlayerKills(player.getPlayer(), type.getKdStatusTimeUnit()));
            }
        }
        return null;
    }
}
