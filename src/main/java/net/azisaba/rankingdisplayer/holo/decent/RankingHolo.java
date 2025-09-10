package net.azisaba.rankingdisplayer.holo.decent;

import com.google.common.base.Strings;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import net.azisaba.rankingdisplayer.holo.placeholder.RankingExpansion;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;

public class RankingHolo {
    public static final List<String> pageNames = Arrays.asList("Daily", "Monthly", "Total");
    private static final String STATUS_HEADER = ChatColor.AQUA + Strings.repeat("=", 8) + ChatColor.GOLD + " Kill Ranking " + ChatColor.AQUA + Strings.repeat("=", 8);
    private static final String STATUS_FOOTER = ChatColor.AQUA + Strings.repeat("=", 25);
    private static final String USER_STATUS = ChatColor.BLUE + "YOU " + ChatColor.RED + ">> " + ChatColor.YELLOW + "%rankingdisplayer_playerranking_{timeunit}%位 " + ChatColor.GOLD + "{player}" + ChatColor.RED + ": " + ChatColor.AQUA + "%rankingdisplayer_playerkillcount_{timeunit}%" + " kill(s)";
    private static final String LAST_UPDATED = ChatColor.RED + "ランキング最終更新: " + ChatColor.GREEN + "%rankingdisplayer_lastupdate_{timeunit}%秒前";
    private static final int RANKING_SIZE = 7;

    /**
     * @param targetHolo target hologram
     * @return is completed
     */
    public static boolean setRanking(Hologram targetHolo) {
        while (targetHolo.getPages().size() < 3) {
            DHAPI.addHologramPage(targetHolo);
        }
        setRankingLines(targetHolo.getPage(0), RANKING_SIZE, "daily");
        setRankingLines(targetHolo.getPage(1), RANKING_SIZE, "monthly");
        setRankingLines(targetHolo.getPage(2), RANKING_SIZE, "total");

        if (!DHHoloFeatures.applyPageRotation(targetHolo, pageNames, ChatColor.GREEN)) {
            return false;
        }

        // TODO check below -> DH側がtick処理で確認しているためいらない可能性がある
        targetHolo.updateAll();
        return true;
    }

    private static String getRankingLine(String timeUnitName, int order) {
        return RankingExpansion.Names.getKillRankingPlaceholder(timeUnitName, order);
    }

    private static void setRankingLines(HologramPage targetPage, int maxSize, String timeUnitName) {
        targetPage.addLine(new HologramLine(targetPage, targetPage.getNextLineLocation(), STATUS_HEADER));
        for (int i = 0; i < maxSize; i++) {
            targetPage.addLine(new HologramLine(targetPage, targetPage.getNextLineLocation(), getRankingLine(timeUnitName, i + 1)));
        }
        targetPage.addLine(new HologramLine(targetPage, targetPage.getNextLineLocation(), STATUS_FOOTER));
        targetPage.addLine(new HologramLine(targetPage, targetPage.getNextLineLocation(), USER_STATUS.replace("{timeunit}", timeUnitName)));
//        targetPage.addLine(new HologramLine(targetPage, targetPage.getNextLineLocation(), LAST_UPDATED.replace("{timeunit}", timeUnitName)));
    }
}
