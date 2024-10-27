package jp.azisaba.lgw.rankingdisplayer.command;

import jp.azisaba.lgw.rankingdisplayer.RankingDisplayer;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class RankingDisplayerCommand implements CommandExecutor {
    private final RankingDisplayer plugin;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(args.length < 1) {
            return false;
        }
        if(sender instanceof Player) {
            Player player = (Player) sender;
            switch (args[0]) {
                case "reload":
                    if(args.length != 2) {
                        sender.sendMessage("/" + command.getName() + " reload <type>");
                        return true;
                    }
                    switch (args[1]) {
                        case "config":
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
