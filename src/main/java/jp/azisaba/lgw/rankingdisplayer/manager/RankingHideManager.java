package jp.azisaba.lgw.rankingdisplayer.manager;

import net.azisaba.playersettings.PlayerSettings;
import net.azisaba.playersettings.util.SettingsData;
import org.bukkit.entity.Player;

import java.util.UUID;

public class RankingHideManager {

    private static final String key = "RankingDisplayer.Anonymous";

    public static boolean isHiding(Player p) {
        return isHiding(p.getUniqueId());
    }

    public static void setHiding(UUID uuid, boolean hide) {
        SettingsData data = PlayerSettings.getPlugin().getManager().getSettingsData(uuid);

        if (hide) {
            data.set(key, true);
        } else {
            data.set(key, null);
        }
    }

    public static boolean isHiding(UUID uuid) {
        SettingsData data = PlayerSettings.getPlugin().getManager().getSettingsData(uuid);
        return data.isSet(key) && data.getBoolean(key);
    }
}
