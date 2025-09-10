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
        public LocationData displayLocation = new LocationData("world", 0, 0, 0);
    }

    public record LocationData(
            String worldName,
            int x,
            int y,
            int z
    ) {
        public Location toLocation() {
            return new Location(Bukkit.getWorld(worldName), x, y, z);
        }
    }
}
