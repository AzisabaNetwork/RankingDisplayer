package net.azisaba.rankingdisplayer.setting.convert;

import net.azisaba.rankingdisplayer.setting.RankingHideManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.UUID;

public class OldSettingConverter {
    public static void convertSettingsFromLocalFile(File pluginFolder) {
        File file = new File(pluginFolder, "HideFromRanking.yml");

        if (!file.exists()) {
            return;
        }

        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);

        if (conf.getConfigurationSection("") == null || conf.getConfigurationSection("").getKeys(false) == null) {
            return;
        }

        for (String key : conf.getConfigurationSection("").getKeys(false)) {
            UUID uuid = null;
            try {
                uuid = UUID.fromString(key);
            } catch (Exception e) {
                Bukkit.getLogger().warning("Could not parse String \"" + key + "\"");
                continue;
            }

            RankingHideManager.setHiding(uuid, true);
        }

        file.delete();
    }
}
