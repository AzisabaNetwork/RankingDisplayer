package jp.azisaba.lgw.rankingdisplayer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

public class PluginConfig {

    private final RankingDisplayer plugin;
    private final FileConfiguration conf;

    @ConfigOptions(path = "Chat.Prefix", type = OptionType.CHAT_FORMAT)
    public String chatPrefix = "&c[&6Ranking&c] ";

    @ConfigOptions(path = "Holographic.DisplayLocation", type = OptionType.LOCATION)
    public Location displayLocation = null;

    @ConfigOptions(path = "Holographic.UpdateButton", type = OptionType.LOCATION_LIST)
    public List<Location> updateButtonList = new ArrayList<>();

    public PluginConfig(RankingDisplayer plugin) {
        this.plugin = plugin;
        conf = plugin.getConfig();

        displayLocation = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        updateButtonList.add(new Location(Bukkit.getWorld("world"), 621, 11, 99));
    }

    public void additional() {

    }

    public void loadConfig() {
        for ( Field field : getClass().getFields() ) {
            ConfigOptions anno = field.getAnnotation(ConfigOptions.class);

            if ( anno == null ) {
                continue;
            }

            String path = anno.path();

            if ( conf.get(path) == null ) {

                try {

                    if ( anno.type() == OptionType.NONE ) {
                        conf.set(path, field.get(this));
                    } else if ( anno.type() == OptionType.LOCATION ) {
                        Location loc = (Location) field.get(this);

                        conf.set(path, loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ()
                                + "," + loc.getYaw() + "," + loc.getPitch());
                    } else if ( anno.type() == OptionType.CHAT_FORMAT ) {

                        String msg = (String) field.get(this);
                        conf.set(path, msg);

                        msg = ChatColor.translateAlternateColorCodes('&', msg);
                        field.set(this, msg);
                    } else if ( anno.type() == OptionType.SOUND ) {
                        conf.set(path, field.get(this).toString());
                    } else if ( anno.type() == OptionType.LOCATION_LIST ) {
                        @SuppressWarnings("unchecked")
                        List<Location> locations = (List<Location>) field.get(this);

                        List<String> strs = new ArrayList<>();

                        if ( !locations.isEmpty() ) {

                            for ( Location loc : locations ) {
                                strs.add(loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + ","
                                        + loc.getZ()
                                        + "," + loc.getYaw() + "," + loc.getPitch());
                            }
                        } else {
                            strs.add("WorldName,X,Y,Z,Yaw,Pitch");
                        }

                        conf.set(path, strs);
                    }

                    plugin.saveConfig();
                } catch ( Exception e ) {
                    Bukkit.getLogger().warning("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {

                try {
                    if ( anno.type() == OptionType.NONE ) {
                        field.set(this, conf.get(path));
                    } else if ( anno.type() == OptionType.LOCATION ) {

                        String[] strings = conf.getString(path).split(",");
                        Location loc = null;
                        try {
                            loc = new Location(Bukkit.getWorld(strings[0]), Double.parseDouble(strings[1]),
                                    Double.parseDouble(strings[2]), Double.parseDouble(strings[3]));
                            loc.setYaw(Float.parseFloat(strings[4]));
                            loc.setPitch(Float.parseFloat(strings[5]));
                        } catch ( Exception e ) {
                            // None
                        }

                        if ( loc == null ) {
                            Bukkit.getLogger().warning("Error. " + path + " の値がロードできませんでした。");
                            continue;
                        }

                        field.set(this, loc);
                    } else if ( anno.type() == OptionType.SOUND ) {

                        String name = conf.getString(path);
                        Sound sound;

                        try {
                            sound = Sound.valueOf(name.toUpperCase());
                        } catch ( Exception e ) {
                            Bukkit.getLogger().warning("Error. " + path + " の値がロードできませんでした。");
                            continue;
                        }

                        field.set(this, sound);
                    } else if ( anno.type() == OptionType.CHAT_FORMAT ) {

                        String unformatMessage = conf.getString(path);

                        unformatMessage = ChatColor.translateAlternateColorCodes('&', unformatMessage);

                        field.set(this, unformatMessage);
                    } else if ( anno.type() == OptionType.LOCATION_LIST ) {

                        List<String> strList = conf.getStringList(path);

                        List<Location> locList = new ArrayList<>();

                        for ( String str : strList ) {

                            String[] strings = str.split(",");
                            Location loc = null;
                            try {
                                loc = new Location(Bukkit.getWorld(strings[0]), Double.parseDouble(strings[1]),
                                        Double.parseDouble(strings[2]), Double.parseDouble(strings[3]));
                                loc.setYaw(Float.parseFloat(strings[4]));
                                loc.setPitch(Float.parseFloat(strings[5]));
                            } catch ( Exception e ) {
                                // None
                            }

                            if ( loc == null ) {
                                Bukkit.getLogger().warning("Error. " + path + " の " + str + "がロードできませんでした。");
                                continue;
                            }

                            locList.add(loc);
                        }

                        field.set(this, locList);
                    }
                } catch ( Exception e ) {
                    Bukkit.getLogger().warning("Error. " + e.getMessage());
                }
            }
        }

        additional();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ConfigOptions {
        public String path();

        public OptionType type() default OptionType.NONE;
    }

    public enum OptionType {
        LOCATION,
        LOCATION_LIST,
        SOUND,
        CHAT_FORMAT,
        NONE
    }
}
