package jp.azisaba.lgw.rankingdisplayer;

import jp.azisaba.lgw.rankingdisplayer.command.RankingHoloCommand;
import jp.azisaba.lgw.rankingdisplayer.config.PluginConfig;
import jp.azisaba.lgw.rankingdisplayer.holo.DHListener;
import jp.azisaba.lgw.rankingdisplayer.integration.KDSAPI;
import jp.azisaba.lgw.rankingdisplayer.integration.KDSPlaceholderExpansion;
import jp.azisaba.lgw.rankingdisplayer.manager.RankingCacheManager;
import jp.azisaba.lgw.rankingdisplayer.manager.RankingHideManager;
import jp.azisaba.lgw.rankingdisplayer.ranking.RankingCommand;
import jp.azisaba.lgw.rankingdisplayer.task.AfterWorldLoadTask;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.UUID;

public class RankingDisplayer extends JavaPlugin {

    private static PluginConfig config;
    private DHListener dhListener;

    @Override
    public void onEnable() {
        if(!Bukkit.getPluginManager().isPluginEnabled("KDStatusReloaded")) {
            getLogger().severe("KDStatusReloaded is not loaded.");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        // Migrate
        convertSettingsFromLocalFile();

        // Config
        RankingDisplayer.config = new PluginConfig(this);
        RankingDisplayer.config.loadConfig();

        // KDStatusReloaded API Setup
        KDSAPI.loadPlugin(getLogger());

        // Setup cacheManager's logger
        RankingCacheManager.getInstance().setLogger(getLogger());
        RankingCacheManager.getInstance().loadRanking();

        // Events
        dhListener = new DHListener();
        Bukkit.getPluginManager().registerEvents(dhListener, this);

        // Tasks
        new AfterWorldLoadTask().runTaskLaterAsynchronously(this, 100);

        // Commands
        Bukkit.getPluginCommand("ranking").setExecutor(new RankingCommand());
        Bukkit.getPluginCommand("ranking").setPermissionMessage(ChatColor.RED + "権限がありません！");

        Bukkit.getPluginCommand("rankingholo").setExecutor(new RankingHoloCommand());
        Bukkit.getPluginCommand("rankingholo").setPermissionMessage(ChatColor.RED + "権限がありません！");

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new KDSPlaceholderExpansion().register();
        }

        // Finish message
        Bukkit.getLogger().info(getName() + " enabled.");
    }

    @Override
    public void onDisable() {
//        if (listener != null) {
//            listener.removeAllBoards();
//        }

        Bukkit.getLogger().info(getName() + " disabled.");
    }

    public void reloadPluginConfig() {

        reloadConfig();

        RankingDisplayer.config = new PluginConfig(this);
        RankingDisplayer.config.loadConfig();
    }

    public static PluginConfig getPluginConfig() {
        return config;
    }

    private void convertSettingsFromLocalFile() {
        File file = new File(getDataFolder(), "HideFromRanking.yml");

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
