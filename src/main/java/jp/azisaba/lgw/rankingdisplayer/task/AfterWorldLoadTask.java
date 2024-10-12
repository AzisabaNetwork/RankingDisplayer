package jp.azisaba.lgw.rankingdisplayer.task;

import jp.azisaba.lgw.rankingdisplayer.manager.HoloManager;
import org.bukkit.scheduler.BukkitRunnable;

public class AfterWorldLoadTask extends BukkitRunnable {
    @Override
    public void run() {
        HoloManager.onLoad();
    }
}
