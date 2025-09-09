package net.azisaba.rankingdisplayer.holo.command;

import net.azisaba.rankingdisplayer.holo.decent.DHHoloManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class RankingHoloCommand implements CommandExecutor, TabCompleter {
    public static final List<String> modes = Arrays.asList("place", "remove", "remove-all", "list");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length < 1) {
            return false;
        }

        String mode = args[0];
        if (!(sender instanceof Player player)) return false;

        switch (mode) {
            case "place" -> {
                sender.sendMessage("hologram was created!");
                DHHoloManager.addHolo(player.getLocation());
                return true;
            }
            case "remove" -> {
                if (args.length != 2) {
                    sender.sendMessage("/" + command.getName() + " remove <hologramName>");
                    return true;
                }
                if (!DHHoloManager.getAllHoloNames().contains(args[1])) {
                    sender.sendMessage("This hologramName is invalid.");
                    return true;
                }
                DHHoloManager.removeHolo(args[1]);
                sender.sendMessage("Successfully to remove hologram (name:" + args[1] + ")");
                return true;
            }
            case "remove-all" -> {
                DHHoloManager.removeAllHolo();
                sender.sendMessage("Successfully to remove all status holograms!");
                return true;
            }
            case "list" -> {
                Set<String> names = DHHoloManager.getAllHoloNames();
                sender.sendMessage("Size: " + names.size());
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            return modes;
        }

        if (args.length == 2) {
            if (args[0].equals("remove")) {
                return new ArrayList<>(DHHoloManager.getAllHoloNames());
            }
        }

        return Collections.emptyList();
    }
}

