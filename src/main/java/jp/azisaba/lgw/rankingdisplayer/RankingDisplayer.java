package jp.azisaba.lgw.rankingdisplayer;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class RankingDisplayer extends JavaPlugin {

	private static PluginConfig config;
	private DisplayListener listener;

	@Override
	public void onEnable() {

		RankingDisplayer.config = new PluginConfig(this);
		RankingDisplayer.config.loadConfig();

		this.listener = new DisplayListener(this);
		Bukkit.getPluginManager().registerEvents(this.listener, this);

		Bukkit.getPluginCommand("ranking").setExecutor(new RankingCommand());
		Bukkit.getPluginCommand("ranking").setPermissionMessage(ChatColor.RED + "権限がありません！");

		if (Bukkit.getOnlinePlayers().size() > 0) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				World world = p.getWorld();
				if (world == config.displayLocation.getWorld())
					listener.displayRankingForPlayerAsync(p, false);
			}
		}

		Bukkit.getLogger().info(getName() + " enabled.");
	}

	@Override
	public void onDisable() {
		if (this.listener != null) {
			listener.removeAllBoards();
		}

		Bukkit.getLogger().info(getName() + " disabled.");
	}

	public void reloadPluginConfig() {

		this.reloadConfig();

		RankingDisplayer.config = new PluginConfig(this);
		RankingDisplayer.config.loadConfig();
	}

	public static PluginConfig getPluginConfig() {
		return config;
	}
}
