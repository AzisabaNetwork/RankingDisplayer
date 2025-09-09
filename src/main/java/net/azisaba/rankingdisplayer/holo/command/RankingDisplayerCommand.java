package net.azisaba.rankingdisplayer.holo.command;

import net.azisaba.rankingdisplayer.RankingDisplayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RankingDisplayerCommand implements CommandExecutor {
    private final RankingDisplayer plugin;

    public RankingDisplayerCommand(RankingDisplayer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length < 1) {
            return false;
        }
        if (sender instanceof Player player) {
            if (args[0].equals("reload")) {
                if (args.length != 2) {
                    sender.sendMessage("/" + command.getName() + " reload <type>");
                    return true;
                }
                if (args[1].equals("config")) {
                    sender.sendMessage("Reloading config...");
                    plugin.reloadPluginConfig();
                    sender.sendMessage("Completed to reload config!");
                    return true;
                }
                sender.sendMessage("No type matched: " + args[1]);
                return true;
            }
        }
        return false;
    }
}
