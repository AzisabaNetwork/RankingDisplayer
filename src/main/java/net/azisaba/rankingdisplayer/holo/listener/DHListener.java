package net.azisaba.rankingdisplayer.holo.listener;

import eu.decentsoftware.holograms.event.DecentHologramsReloadEvent;
import net.azisaba.rankingdisplayer.holo.decent.DHHoloManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DHListener implements Listener {
    @EventHandler
    public void onHologramReload(DecentHologramsReloadEvent e) {
        DHHoloManager.onLoad();
    }
}
