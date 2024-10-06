package jp.azisaba.lgw.rankingdisplayer.integration;

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
            if(name.equalsIgnoreCase(Names.PLAYER_RANKING)) {
                // TODO get player ranking
                return "ranking";
            } else if (name.equalsIgnoreCase(Names.PLAYER_KILL_COUNT)) {
                // TODO get player's kill count
                return "kill_count";
            }
        }
        return null;
    }
}
