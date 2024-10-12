package jp.azisaba.lgw.rankingdisplayer.manager;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.DecentHologramsAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import jp.azisaba.lgw.rankingdisplayer.holo.RankingHolo;
import org.bukkit.Location;

import java.util.Collection;
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
        Hologram holo = DHAPI.createHologram(_holoName, holoLocation, true);
        return RankingHolo.setRanking(holo);
    }

    public static void getAllHolo() {
        Collection<Hologram> holograms = DecentHologramsAPI.get().getHologramManager().getHolograms();
        for(Hologram h: holograms) {
            if(h.getId().startsWith(HOLO_PREFIX)) {
                lastUpdateMap.put(h.getId(), System.currentTimeMillis());
            }
        }
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
