package net.azisaba.rankingdisplayer.setting;

import net.azisaba.rankingdisplayer.setting.external.PlayerSettingsAPI;
import org.bukkit.entity.Player;

import java.util.UUID;

public class RankingHideManager {
    private static final String KEY = "RankingDisplayer.Anonymous";

    public static void setHiding(UUID uuid, boolean hide) {
        PlayerSettingsAPI.set(uuid, KEY, hide ? true : null);
    }

    public static boolean isHiding(Player p) {
        return isHiding(p.getUniqueId());
    }

    public static boolean isHiding(UUID uuid) {
        return PlayerSettingsAPI.getBoolean(uuid, KEY);
    }
}
