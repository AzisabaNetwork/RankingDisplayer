package net.azisaba.rankingdisplayer.config;

import de.exlll.configlib.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.Location;

@Configuration
public class RDConfig {
    public ChatConfig chat = new ChatConfig();
    public HoloConfig holo = new HoloConfig();

    @Configuration
    public static class ChatConfig {
        public String chatPrefix = "&c[&6Ranking&c] ";
    }

    @Configuration
    public static class HoloConfig {
        public Location displayLocation = new Location(Bukkit.getWorld("world"), 0, 0, 0);
    }
}
