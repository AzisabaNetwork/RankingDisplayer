package jp.azisaba.lgw.rankingdisplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;

import com.google.common.base.Strings;

import jp.azisaba.lgw.kdstatus.KDStatusReloaded;
import jp.azisaba.lgw.kdstatus.KillDeathDataContainer.TimeUnit;
import jp.azisaba.lgw.kdstatus.PlayerInfo;

public class DisplayListener implements Listener {

    private final RankingDisplayer plugin;
    private KDStatusReloaded kdsPlugin;

    private List<Entry<PlayerInfo, Integer>> lastDailyResult, lastMonthlyResult, lastTotalResult;
    private HashMap<PlayerInfo, Integer> lastDailyKillMap, lastMonthlyKillMap, lastTotalKillMap;
    private long lastDailyUpdated = 0L, lastMonthlyUpdated = 0L, lastTotalUpdated = 0L;

    private final long cacheHoldMilliSec = 1000 * 10;

    private static String HEADER = ChatColor.AQUA + Strings.repeat("=", 8) + " " + ChatColor.GOLD
            + "Kill Ranking " + ChatColor.AQUA + Strings.repeat("=", 8);

    public DisplayListener(RankingDisplayer plugin) {
        this.plugin = plugin;
    }

    private final HashMap<Player, Long> updatedLong = new HashMap<>();
    private final HashMap<Player, DisplayType> displayTypeMap = new HashMap<>();
    private final List<Player> processing = new ArrayList<>();

    @EventHandler
    public void onInteractUpdateButton(PlayerInteractEvent e) {
        if ( e.getAction() != Action.RIGHT_CLICK_BLOCK ) {
            return;
        }

        Player p = e.getPlayer();

        Block b = e.getClickedBlock();

        if ( !b.getType().toString().endsWith("BUTTON") ) {
            return;
        }

        if ( RankingDisplayer.getPluginConfig().updateButtonList.contains(b.getLocation()) ) {
            e.setCancelled(true);

            if ( updatedLong.containsKey(p) && updatedLong.get(p) + 5000 > System.currentTimeMillis() ) {
                p.sendMessage(ChatColor.RED + "更新ボタンを連打しないでください。");
                return;
            }

            displayRankingForPlayerAsync(p, true);

            updatedLong.put(p, System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onChangeDisplayType(PlayerInteractEvent e) {
        if ( !e.getAction().toString().startsWith("RIGHT_CLICK") ) {
            return;
        }
        Player p = e.getPlayer();

        if ( processing.contains(p) ) {
            return;
        }

        Location clickedLoc = getLookingAirBlock(p).getLocation();
        Location displayLoc = RankingDisplayer.getPluginConfig().displayLocation.clone();

        if ( clickedLoc.getWorld() != displayLoc.getWorld() ) {
            return;
        }

        double minus = clickedLoc.getY() - displayLoc.getY();
        boolean isYArea = minus <= 4 && minus >= -2;
        clickedLoc.setX(clickedLoc.getBlockX());
        clickedLoc.setY(0);
        clickedLoc.setZ(clickedLoc.getBlockZ());
        displayLoc.setY(0);

        if ( clickedLoc.distance(displayLoc) <= 2 && isYArea ) {
            if ( !displayTypeMap.containsKey(p) ) {
                displayTypeMap.put(p, DisplayType.MONTHLY);
            } else {
                switch (displayTypeMap.get(p)) {
                case DAILY:
                    displayTypeMap.put(p, DisplayType.MONTHLY);
                    break;
                case MONTHLY:
                    displayTypeMap.put(p, DisplayType.TOTAL);
                    break;
                case TOTAL:
                    displayTypeMap.put(p, DisplayType.DAILY);
                    break;
                }
            }

            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HAT, 1, 1);

            processing.add(p);
            displayRankingForPlayerAsync(p, false);
        }
    }

    public final Block getLookingAirBlock(Player player) {
        BlockIterator iter = new BlockIterator(player, 4);
        Block b = iter.next();

        int count = 0;
        while ( iter.hasNext() ) {
            count++;
            b = iter.next();

            if ( b.getType() != Material.AIR ) {
                return b;
            }

            if ( count >= 3 ) {
                break;
            }
        }
        return b;
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();
        World to = p.getWorld();

        Location display = RankingDisplayer.getPluginConfig().displayLocation;
        if ( display.getWorld() != to ) {
            return;
        }

        displayRankingForPlayerAsync(p, false);
    }

    @EventHandler
    public void onJoinServer(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        World world = p.getWorld();

        Location display = RankingDisplayer.getPluginConfig().displayLocation;
        if ( display.getWorld() != world ) {
            return;
        }

        displayRankingForPlayerAsync(p, false);
    }

    private final HashMap<Player, Hologram> holoMap = new HashMap<>();

    public void displayRankingForPlayerAsync(Player p, boolean ignoreCache) {

        if ( holoMap.containsKey(p) ) {
            holoMap.get(p).removeAll();
        }

        Location updatingLoc = RankingDisplayer.getPluginConfig().displayLocation.clone();
        updatingLoc.add(0, 2, 0);

        Hologram holo = Hologram.create(ChatColor.RED + "更新中...", updatingLoc);
        holo.display(p);

        if ( !displayTypeMap.containsKey(p) ) {
            displayDailyRanking(holo, p, ignoreCache);
        } else {

            switch (displayTypeMap.get(p)) {
            case DAILY:
                displayDailyRanking(holo, p, ignoreCache);
                break;
            case MONTHLY:
                displayMonthlyRanking(holo, p, ignoreCache);
                break;
            case TOTAL:
                displayTotalRanking(holo, p, ignoreCache);
                break;
            }

        }
    }

    private void displayDailyRanking(Hologram holo, Player p, boolean ignoreCache) {
        new Thread(() -> {

            long start = System.currentTimeMillis();
            holo.setLine(0, HEADER);

            long waitingSynchro = System.currentTimeMillis();
            long rankingDataFetchTime = updateDailyKillsData(ignoreCache);
            long endedSynchro = System.currentTimeMillis();

            setRankingData(holo, p, lastDailyKillMap, lastDailyResult);
            holo.addLine(getFooter(DisplayType.DAILY));
            holo.setLocation(holo.getLocation().subtract(0, 2, 0));
            holo.update(p);
            holoMap.put(p, holo);

            long end = System.currentTimeMillis();

            plugin.getLogger().info("Displayed Daily Ranking for " + p.getName() + " async ("
                    + (end - start - (endedSynchro - waitingSynchro) + rankingDataFetchTime) + "ms)");

            new BukkitRunnable() {
                @Override
                public void run() {
                    if ( processing.contains(p) ) {
                        processing.remove(p);
                    }
                }
            }.runTaskLater(plugin, 0);
        }).start();
    }

    private void displayMonthlyRanking(Hologram holo, Player p, boolean ignoreCache) {
        new Thread(() -> {

            long start = System.currentTimeMillis();
            holo.setLine(0, HEADER);

            long waitingSynchro = System.currentTimeMillis();
            long rankingDataFetchTime = updateMonthlyKillsData(ignoreCache);
            long endedSynchro = System.currentTimeMillis();

            setRankingData(holo, p, lastMonthlyKillMap, lastMonthlyResult);
            holo.addLine(getFooter(DisplayType.MONTHLY));
            holo.setLocation(holo.getLocation().subtract(0, 2, 0));
            holo.update(p);
            holoMap.put(p, holo);

            long end = System.currentTimeMillis();

            plugin.getLogger().info("Displayed Monthly Ranking for " + p.getName() + " async ("
                    + (end - start - (endedSynchro - waitingSynchro) + rankingDataFetchTime) + "ms)");

            new BukkitRunnable() {
                @Override
                public void run() {
                    if ( processing.contains(p) ) {
                        processing.remove(p);
                    }
                }
            }.runTaskLater(plugin, 0);
        }).start();
    }

    private void displayTotalRanking(Hologram holo, Player p, boolean ignoreCache) {
        new Thread(() -> {

            long start = System.currentTimeMillis();

            holo.setLine(0, HEADER);

            long waitingSynchro = System.currentTimeMillis();
            long rankingDataFetchTime = updateTotalKillsData(ignoreCache);
            long endedSynchro = System.currentTimeMillis();

            setRankingData(holo, p, lastTotalKillMap, lastTotalResult);
            holo.addLine(getFooter(DisplayType.TOTAL));
            holo.setLocation(holo.getLocation().subtract(0, 2, 0));
            holo.update(p);
            holoMap.put(p, holo);

            long end = System.currentTimeMillis();

            plugin.getLogger().info("Displayed Total Ranking for " + p.getName() + " async ("
                    + (end - start - (endedSynchro - waitingSynchro) + rankingDataFetchTime) + "ms)");

            new BukkitRunnable() {
                @Override
                public void run() {
                    if ( processing.contains(p) ) {
                        processing.remove(p);
                    }
                }
            }.runTaskLater(plugin, 0);
        }).start();
    }

    private void setRankingData(Hologram holo, Player p, HashMap<PlayerInfo, Integer> data,
            List<Entry<PlayerInfo, Integer>> sorted) {
        int num = 0;
        int rank = 0;
        int before = Integer.MIN_VALUE;
        boolean containsHim = false;
        for ( Entry<PlayerInfo, Integer> mapEntry : sorted ) {

            PlayerInfo player = mapEntry.getKey();
            int kill = mapEntry.getValue();

            String playerName = player.getName();

            boolean hide = RankingHideManager.isHiding(player.getUuid());
            if ( hide && !player.getName().equals(p.getName()) ) {
                playerName = ChatColor.DARK_RED + "{匿名プレイヤー}";
            }

            if ( num >= 7 ) {
                break;
            }

            num++;
            if ( kill != before ) {
                rank = num;
                before = kill;
            }

            String line = ChatColor.YELLOW + "" + rank + "位 " + ChatColor.GOLD + "{PLAYER}" + ChatColor.RED + ": "
                    + ChatColor.AQUA + kill + " kill(s)";

            if ( player.getName().equals(p.getName()) ) {
                line = ChatColor.BLUE + "YOU" + ChatColor.RED + " » " + line;
                line = line.replace("{PLAYER}", p.getName());
                containsHim = true;

                if ( hide ) {
                    line = ChatColor.DARK_RED + "(Hide) " + line;
                }
            } else {
                line = line.replace("{PLAYER}", playerName);
            }

            holo.addLine(line);
        }

        while ( num < 7 ) {
            num++;
            holo.addLine(ChatColor.YELLOW + "" + num + "位 " + ChatColor.GOLD + "なし");
        }

        holo.addLine(ChatColor.AQUA + StringUtils.repeat("=", 25));

        if ( !containsHim ) {
            String ranking = "-";
            int kill = 0;
            boolean hide = RankingHideManager.isHiding(p.getUniqueId());

            List<String> rankingPlayerList = new ArrayList<>();

            for ( Entry<PlayerInfo, Integer> entry : sorted ) {
                if ( !rankingPlayerList.contains(entry.getKey().getName()) ) {
                    rankingPlayerList.add(entry.getKey().getName());
                }
            }

            int n = rankingPlayerList.indexOf(p.getName()) + 1;

            if ( n > 0 ) {
                ranking = n + "";
                kill = sorted.get(n - 1).getValue();
            }

            String line = ChatColor.BLUE + "YOU" + ChatColor.RED + " » " + ChatColor.YELLOW + "" + ranking + "位 " +
                    ChatColor.GOLD + p.getName() + ChatColor.RED + ": " + ChatColor.AQUA + kill + " kill(s)";
            if ( hide ) {
                line = ChatColor.DARK_RED + "(Hide) " + line;
            }
            holo.addLine(line);
        }
    }

    private synchronized long updateDailyKillsData(boolean ignoreCache) {
        long start = System.currentTimeMillis();

        // KDStatusReloadedがない場合は取得
        if ( kdsPlugin == null || !kdsPlugin.isEnabled() ) {
            // 取得し、失敗したらエラー
            if ( !getKDSPlugin() ) {
                throw new IllegalStateException("Failed to get plugin \"KDStatusReloaded\"");
            }
        }

        if ( ignoreCache || lastDailyUpdated + cacheHoldMilliSec < System.currentTimeMillis() ) {
            lastDailyKillMap = kdsPlugin.getKdDataContainer().getAllKills(TimeUnit.DAILY);
            lastDailyResult = new ArrayList<>(lastDailyKillMap.entrySet());

            Collections.sort(lastDailyResult, (entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

            lastDailyUpdated = System.currentTimeMillis();
        }
        return System.currentTimeMillis() - start;
    }

    private synchronized long updateMonthlyKillsData(boolean ignoreCache) {
        long start = System.currentTimeMillis();

        // KDStatusReloadedがない場合は取得
        if ( kdsPlugin == null || !kdsPlugin.isEnabled() ) {
            // 取得し、失敗したらエラー
            if ( !getKDSPlugin() ) {
                throw new IllegalStateException("Failed to get plugin \"KDStatusReloaded\"");
            }
        }

        if ( ignoreCache || lastMonthlyUpdated + cacheHoldMilliSec < System.currentTimeMillis() ) {
            lastMonthlyKillMap = kdsPlugin.getKdDataContainer().getAllKills(TimeUnit.MONTHLY);
            lastMonthlyResult = new ArrayList<>(lastMonthlyKillMap.entrySet());

            Collections.sort(lastMonthlyResult, (entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

            lastMonthlyUpdated = System.currentTimeMillis();
        }
        return System.currentTimeMillis() - start;
    }

    private synchronized long updateTotalKillsData(boolean ignoreCache) {
        long start = System.currentTimeMillis();

        // KDStatusReloadedがない場合は取得
        if ( kdsPlugin == null || !kdsPlugin.isEnabled() ) {
            // 取得し、失敗したらエラー
            if ( !getKDSPlugin() ) {
                throw new IllegalStateException("Failed to get plugin \"KDStatusReloaded\"");
            }
        }

        if ( ignoreCache || lastTotalUpdated + cacheHoldMilliSec < System.currentTimeMillis() ) {
            lastTotalKillMap = kdsPlugin.getKdDataContainer().getAllTotalKills();
            lastTotalResult = new ArrayList<>(lastTotalKillMap.entrySet());

            Collections.sort(lastTotalResult, (entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

            lastTotalUpdated = System.currentTimeMillis();
        }
        return System.currentTimeMillis() - start;
    }

    public void removeAllBoards() {
        for ( Hologram holo : holoMap.values() ) {
            holo.removeAll();
        }
    }

    private String getFooter(DisplayType type) {
        StringBuilder builder = new StringBuilder();
        for ( DisplayType type2 : DisplayType.values() ) {
            if ( type2 != type ) {
                builder.append(ChatColor.GRAY);
            } else {
                builder.append(ChatColor.GREEN + "" + ChatColor.BOLD);
            }

            builder.append(type2.toString().substring(0, 1) + type2.toString().substring(1).toLowerCase() + " ");
        }

        return builder.toString().trim();
    }

    private boolean getKDSPlugin() {
        // Pluginを取得
        Plugin pl = Bukkit.getPluginManager().getPlugin("KDStatusReloaded");
        // nullならreturn false
        if ( pl == null ) {
            return false;
        }
        // 代入
        kdsPlugin = (KDStatusReloaded) pl;
        // 無効化されていたらreturn false
        if ( !kdsPlugin.isEnabled() ) {
            return false;
        }
        return true;
    }

    private enum DisplayType {
        DAILY,
        MONTHLY,
        TOTAL
    }
}
