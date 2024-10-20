package jp.azisaba.lgw.rankingdisplayer.manager;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.DecentHologramsAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import jp.azisaba.lgw.rankingdisplayer.RankingDisplayer;
import jp.azisaba.lgw.rankingdisplayer.holo.RankingHolo;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HoloManager {
    /**
     * Prefix of hologram's name
     */
    private static final String HOLO_PREFIX = "_ranking_kill";

    /**
     * Key: Location of hologram
     * Value: Milliseconds of last updated time
     */
    private static final ConcurrentHashMap<String, Long> lastUpdateMap = new ConcurrentHashMap<>();

    public static boolean addHolo(Location holoLocation) {
        String _holoName = getHoloName(holoLocation);
        if (lastUpdateMap.containsKey(_holoName)) {
            return false;
        }
        lastUpdateMap.put(_holoName, System.currentTimeMillis());
        if (DHAPI.getHologram(_holoName) != null) return false;
        Hologram holo = DHAPI.createHologram(_holoName, holoLocation, true);
        return RankingHolo.setRanking(holo);
    }

    public static boolean placeFromConfig() {
        return addHolo(RankingDisplayer.getPluginConfig().displayLocation);
    }

    public static void getAllHolo() {
        Set<String> names = DecentHologramsAPI.get().getHologramManager().getHologramNames();
        System.out.println("Names size: " + names.size());
        for (String n : names) {
            if (n.startsWith(HOLO_PREFIX)) {
                lastUpdateMap.put(n, System.currentTimeMillis());
            }
        }
    }

    public static void onLoad() {
        clearCache();
        getAllHolo();
        placeFromConfig();
    }

    public static void removeHolo(String holoName) {
        DHAPI.removeHologram(holoName);
        lastUpdateMap.remove(holoName);
    }

    public static void removeAllHolo() {
        for (String key : lastUpdateMap.keySet()) {
            removeHolo(key);
        }
    }

    public static void clearCache() {
        lastUpdateMap.clear();
    }

    public static Set<String> getAllHoloNames() {
        return new HashSet<>(lastUpdateMap.keySet());
    }

    private static String getHoloName(Location location) {
        return String.format(
                "%s_%d_%d_%d",
                HOLO_PREFIX,
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }
}
