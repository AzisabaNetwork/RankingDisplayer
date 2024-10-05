package jp.azisaba.lgw.rankingdisplayer.ranking;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.ChatColor;

public class RankingCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage(ChatColor.RED + "/settings" + ChatColor.GRAY + "に移行しました！");
        return true;
    }
}
