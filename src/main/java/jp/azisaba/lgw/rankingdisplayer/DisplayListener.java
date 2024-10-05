package jp.azisaba.lgw.rankingdisplayer;

import com.google.common.base.Strings;
import jp.azisaba.lgw.kdstatus.KDStatusReloaded;
import jp.azisaba.lgw.kdstatus.sql.KillRankingData;
import jp.azisaba.lgw.kdstatus.utils.TimeUnit;
import jp.azisaba.lgw.rankingdisplayer.ranking.RankingDisplayer;
import jp.azisaba.lgw.rankingdisplayer.ranking.RankingHideManager;
import jp.azisaba.lgw.rankingdisplayer.util.DateFormatUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockIterator;

import java.util.*;

@RequiredArgsConstructor
public class DisplayListener implements Listener {

    private final RankingDisplayer plugin;
    private KDStatusReloaded kdsPlugin;

    private final HashMap<RankingType, List<KillRankingData>> dataMap = new HashMap<>();
    private final HashMap<RankingType, Long> lastUpdated = new HashMap<>();

    private final long cacheHoldMilliSec = 1000 * 10;

    private static String HEADER = ChatColor.AQUA + Strings.repeat("=", 8) + " " + ChatColor.GOLD
            + "Kill Ranking " + ChatColor.AQUA + Strings.repeat("=", 8);

    private final HashMap<Player, Long> updatedTime = new HashMap<>();
    private final HashMap<Player, RankingType> displayTypeMap = new HashMap<>();
    private final List<Player> processingPlayers = new ArrayList<>();

    private final HashMap<Player, Hologram> holoMap = new HashMap<>();

    @EventHandler
    public void onInteractUpdateButton(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player p = e.getPlayer();
        Block b = e.getClickedBlock();

        if (!b.getType().toString().endsWith("BUTTON")) {
            return;
        }

        if (RankingDisplayer.getPluginConfig().updateButtonList.contains(b.getLocation())) {
            e.setCancelled(true);

            if (updatedTime.containsKey(p) && updatedTime.get(p) + 5000 > System.currentTimeMillis()) {
                p.sendMessage(ChatColor.RED + "更新ボタンを連打しないでください。");
                return;
            }

            displayRankingForPlayerAsync(p, true);
            updatedTime.put(p, System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onChangeDisplayType(PlayerInteractEvent e) {
        if (!e.getAction().toString().startsWith("RIGHT_CLICK")) {
            return;
        }
        Player p = e.getPlayer();

        if (processingPlayers.contains(p)) {
            return;
        }

        Location clickedLoc = getLookingAirBlock(p).getLocation();
        Location displayLoc = RankingDisplayer.getPluginConfig().displayLocation.clone();

        if (clickedLoc.getWorld() != displayLoc.getWorld()) {
            return;
        }

        double minus = clickedLoc.getY() - displayLoc.getY();
        boolean isYArea = minus <= 4 && minus >= -2;
        clickedLoc.setX(clickedLoc.getBlockX());
        clickedLoc.setY(0);
        clickedLoc.setZ(clickedLoc.getBlockZ());
        displayLoc.setY(0);

        if (clickedLoc.distance(displayLoc) <= 2 && isYArea) {
            displayTypeMap.put(p, getNext(displayTypeMap.getOrDefault(p, null)));

            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);

            processingPlayers.add(p);
            displayRankingForPlayerAsync(p, false);
        }
    }

    public final Block getLookingAirBlock(Player player) {
        BlockIterator iter = new BlockIterator(player, 4);
        Block b = iter.next();

        int count = 0;
        while (iter.hasNext()) {
            count++;
            b = iter.next();

            if (b.getType() != Material.AIR) {
                return b;
            }

            if (count >= 3) {
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
        if (display.getWorld() != to) {
            return;
        }

        displayRankingForPlayerAsync(p, false);
    }

    @EventHandler
    public void onJoinServer(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        World world = p.getWorld();

        Location display = RankingDisplayer.getPluginConfig().displayLocation;
        if (display.getWorld() != world) {
            return;
        }

        displayRankingForPlayerAsync(p, false);
    }

    public void displayRankingForPlayerAsync(Player p, boolean ignoreCache) {

        if (holoMap.containsKey(p)) {
            holoMap.get(p).removeAll();
        }

        Location updatingLoc = RankingDisplayer.getPluginConfig().displayLocation.clone();
        updatingLoc.add(0, 2, 0);

        Hologram holo = Hologram.create(ChatColor.RED + "更新中...", updatingLoc);
        holo.display(p);

        displayRanking(displayTypeMap.getOrDefault(p, RankingType.DAILY), holo, p, ignoreCache);
    }

    private void displayRanking(RankingType type, Hologram holo, Player p, boolean ignoreCache) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            long start = System.currentTimeMillis();

            holo.setLine(0, HEADER);

            long waitingSynchro = System.currentTimeMillis();
            long rankingDataFetchTime = updateData(type, ignoreCache);
            long endedSynchro = System.currentTimeMillis();

            setRankingData(holo, p, dataMap.get(type), type.getKdStatusTimeUnit());

            String formattedLastUpdated = null;

            // KDStatusReloadedがない場合は取得
            if (kdsPlugin == null) {
                // 取得し、失敗したら不明とする
                if (!getKDSPlugin()) {
                    formattedLastUpdated = "不明";
                }
            }

            if (kdsPlugin != null) {
                long updatedBefore = System.currentTimeMillis() - kdsPlugin.getSaveTask().getLastSavedTime();
                if (updatedBefore + (1000 * 5) > System.currentTimeMillis()) {
                    formattedLastUpdated = "今";
                } else {
                    formattedLastUpdated = DateFormatUtils.format(new Date(updatedBefore)) + "前";
                }
            }

            holo.addLine(ChatColor.RED + "ランキング最終更新: " + ChatColor.GREEN + formattedLastUpdated);

            holo.addLine(getFooter(type));
            holo.setLocation(holo.getLocation().subtract(0, 2, 0));
            holo.update(p);
            holoMap.put(p, holo);

            long end = System.currentTimeMillis();

            plugin.getLogger().info("Displayed " + type.toString() + " Ranking for " + p.getName() + " async ("
                    + (end - start - (endedSynchro - waitingSynchro) + rankingDataFetchTime) + "ms)");

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                processingPlayers.remove(p);
            }, 0);
        });
    }

    private void setRankingData(Hologram holo, Player p, List<KillRankingData> dataList, TimeUnit unit) {
        int num = 0;
        int rank = 0;
        int before = Integer.MIN_VALUE;
        boolean containsHim = false;

        for (KillRankingData data : dataList) {

            if (num >= 7) {
                break;
            }

            int kill = data.getKills();
            UUID uuid = data.getUuid();
            String playerName = data.getName();

            if (kill <= 0) {
                break;
            }

            num++;

            boolean hide = RankingHideManager.isHiding(uuid);
            if (hide && !playerName.equals(p.getName())) {
                playerName = ChatColor.DARK_RED + "{匿名プレイヤー}";
            }

            if (kill != before) {
                rank = num;
                before = kill;
            }

            String line = ChatColor.YELLOW + "" + rank + "位 " + ChatColor.GOLD + "{PLAYER}" + ChatColor.RED + ": "
                    + ChatColor.AQUA + kill + " kill(s)";

            if (playerName.equals(p.getName())) {
                line = ChatColor.BLUE + "YOU" + ChatColor.RED + " » " + line;
                line = line.replace("{PLAYER}", p.getName());
                containsHim = true;

                if (hide) {
                    line = ChatColor.DARK_RED + "(Hide) " + line;
                }
            } else {
                line = line.replace("{PLAYER}", playerName);
            }

            holo.addLine(line);
        }

        while (num < 7) {
            num++;
            holo.addLine(ChatColor.YELLOW + "" + num + "位 " + ChatColor.GOLD + "なし");
        }

        holo.addLine(ChatColor.AQUA + StringUtils.repeat("=", 25));

        if (!containsHim) {
            int ranking = kdsPlugin.getKdDataContainer().getRanking(p.getUniqueId(), unit);
            String rankingStr = "" + ranking;
            if (ranking <= 0 || kdsPlugin.getKdDataContainer().getPlayerData(p, true).getKills(unit) <= 0) {
                rankingStr = "-";
            }

            int kills = kdsPlugin.getKdDataContainer().getPlayerData(p, true).getKills(unit);

            String line = ChatColor.BLUE + "YOU" + ChatColor.RED + " » " + ChatColor.YELLOW + "" + rankingStr + "位 " +
                    ChatColor.GOLD + p.getName() + ChatColor.RED + ": " + ChatColor.AQUA + kills + " kill(s)";

            if (RankingHideManager.isHiding(p.getUniqueId())) {
                line = ChatColor.DARK_RED + "(Hide) " + line;
            }
            holo.addLine(line);
        }
    }

    private synchronized long updateData(RankingType type, boolean ignoreCache) {
        long start = System.currentTimeMillis();

        // KDStatusReloadedがない場合は取得
        if (kdsPlugin == null || !kdsPlugin.isEnabled()) {
            // 取得し、失敗したらエラー
            if (!getKDSPlugin()) {
                throw new IllegalStateException("Failed to get plugin \"KDStatusReloaded\"");
            }
        }

        if (ignoreCache || lastUpdated.getOrDefault(type, -1L) + cacheHoldMilliSec < System.currentTimeMillis()) {
            List<KillRankingData> dataList = kdsPlugin.getKdDataContainer().getTopKillRankingData(type.getKdStatusTimeUnit(), 7);
            dataMap.put(type, dataList);

            lastUpdated.put(type, System.currentTimeMillis());
        }
        return System.currentTimeMillis() - start;
    }

    public void removeAllBoards() {
        for (Hologram holo : holoMap.values()) {
            holo.removeAll();
        }
    }

    private String getFooter(RankingType type) {
        StringBuilder builder = new StringBuilder();
        for (RankingType type2 : RankingType.values()) {
            if (type2 != type) {
                builder.append(ChatColor.GRAY);
            } else {
                builder.append(ChatColor.GREEN).append(ChatColor.BOLD);
            }

            builder.append(type2.toString().charAt(0)).append(type2.toString().substring(1).toLowerCase()).append(" ");
        }

        return builder.toString().trim();
    }

    private boolean getKDSPlugin() {
        // Pluginを取得
        Plugin pl = Bukkit.getPluginManager().getPlugin("KDStatusReloaded");
        // nullならreturn false
        if (pl == null) {
            return false;
        }
        // 代入
        kdsPlugin = (KDStatusReloaded) pl;
        // 無効化されていたらreturn false
        return kdsPlugin.isEnabled();
    }

    private RankingType getNext(RankingType type) {
        List<RankingType> values = Arrays.asList(RankingType.values());
        if (type == null) {
            return values.get(1);
        }
        int index = values.indexOf(type) + 1;

        if (values.size() <= index) {
            index = 0;
        }

        return values.get(index);
    }
}
