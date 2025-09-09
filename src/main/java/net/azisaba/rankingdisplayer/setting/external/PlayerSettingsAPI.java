package net.azisaba.rankingdisplayer.setting.external;

import net.azisaba.playersettings.PlayerSettings;
import net.azisaba.playersettings.util.SettingsData;

import java.util.UUID;

public class PlayerSettingsAPI {
    public static SettingsData getSettings(UUID uuid) {
        return PlayerSettings.getPlugin().getManager().getSettingsData(uuid);
    }

    public static void set(UUID uuid, String key, Object value) {
        getSettings(uuid).set(key, value);
    }

    public static boolean getBoolean(UUID uuid, String key) {
        return getSettings(uuid).getBoolean(key);
    }
}
