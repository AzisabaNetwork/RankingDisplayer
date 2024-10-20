package jp.azisaba.lgw.rankingdisplayer.holo;

import eu.decentsoftware.holograms.event.DecentHologramsReloadEvent;
import jp.azisaba.lgw.rankingdisplayer.manager.HoloManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DHListener implements Listener {
    @EventHandler
    public void onHologramReload(DecentHologramsReloadEvent e) {
        HoloManager.onLoad();
    }
}
