package net.azisaba.rankingdisplayer;

import de.exlll.configlib.YamlConfigurations;
import java.io.File;
import java.util.function.Consumer;
import net.azisaba.rankingdisplayer.config.RDConfig;
import net.azisaba.rankingdisplayer.holo.cache.PlayerRankCache;
import net.azisaba.rankingdisplayer.holo.command.RankingCommand;
import net.azisaba.rankingdisplayer.holo.command.RankingDisplayerCommand;
import net.azisaba.rankingdisplayer.holo.command.RankingHoloCommand;
import net.azisaba.rankingdisplayer.holo.decent.DHHoloManager;
import net.azisaba.rankingdisplayer.holo.leaderboard.LeaderboardCache;
import net.azisaba.rankingdisplayer.holo.listener.DHListener;
import net.azisaba.rankingdisplayer.holo.placeholder.RankingExpansion;
import net.azisaba.rankingdisplayer.setting.convert.OldSettingConverter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class RankingDisplayer extends JavaPlugin {
    protected File configFile;
    protected RDConfig rdConfig;
    protected LeaderboardCache leaderboardCache;

    public static RankingDisplayer getPlugin() {
        return JavaPlugin.getPlugin(RankingDisplayer.class);
    }

    public static LeaderboardCache getLeaderboardCache() {
        return getPlugin().leaderboardCache;
    }

    public static RDConfig getRdConfig() {
        return getPlugin().rdConfig;
    }

    @Override
    public void onEnable() {
        configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            YamlConfigurations.save(configFile.toPath(), RDConfig.class, new RDConfig());
            getLogger().severe("設定ファイルを編集してから、再度有効化してください。");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        rdConfig = YamlConfigurations.load(configFile.toPath(), RDConfig.class);

        if (!Bukkit.getPluginManager().isPluginEnabled("KDStatusReloaded")) {
            getLogger().severe("KDStatusReloaded is not loaded.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        OldSettingConverter.convertSettingsFromLocalFile(getDataFolder());

        leaderboardCache = new LeaderboardCache(this);

        // register placeholder expansion for ranking hologram
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            if(!new RankingExpansion().register()) {
                getLogger().warning("Failed to register RankingExpansion");
            }
        } else {
            getLogger().warning("PlaceholderAPI wasn't loaded!");
        }

        // listeners
        registerListener(new DHListener());

        // tasks
        new BukkitRunnable() {
            @Override
            public void run() {
                // On after load world
                DHHoloManager.onLoad();
            }
        }.runTaskLaterAsynchronously(this, 100);

        // commands
        registerCommand("ranking", cmd -> {
            cmd.setExecutor(new RankingCommand());
            cmd.setPermissionMessage(ChatColor.RED + "権限がありません！");
        });

        registerCommand("rankingholo", cmd -> {
            cmd.setExecutor(new RankingHoloCommand());
            cmd.setPermissionMessage(ChatColor.RED + "権限がありません！");
        });

        registerCommand("rankingdisplayer", cmd -> {
            cmd.setExecutor(new RankingDisplayerCommand(this));
            cmd.setPermissionMessage(ChatColor.RED + "権限がありません！");
        });
    }

    @Override
    public void onDisable() {
        if (leaderboardCache != null) leaderboardCache.shutdown();
        PlayerRankCache.shutdown();
    }

    public void reloadPluginConfig() {
        getLogger().info("Reloading plugin config...");
        rdConfig = YamlConfigurations.load(configFile.toPath(), RDConfig.class);
        getLogger().info("Successfully to reload plugin config!");
    }

    public void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    public void registerCommand(String name, Consumer<PluginCommand> commandConsumer) {
        var cmd = Bukkit.getPluginCommand(name);
        if (cmd == null) throw new RuntimeException("Failed to get command " + name);
        commandConsumer.accept(cmd);
    }
}
