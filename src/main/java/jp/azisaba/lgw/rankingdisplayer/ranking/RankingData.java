package jp.azisaba.lgw.rankingdisplayer.ranking;

import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;

@RequiredArgsConstructor
public class RankingData {
    public final int rank;
    public final String playerName;
    public final int kill;
    public final boolean isHiding;

    public String getLine(String targetPlayerName) {
        if (kill <= 0) {
            return ChatColor.YELLOW + "" + rank + "位 " + ChatColor.GOLD + "なし";
        }
        StringBuilder builder = new StringBuilder("" + ChatColor.YELLOW + rank + "位 " + ChatColor.GOLD);
        if (isHiding && !playerName.equals(targetPlayerName)) {
            builder.append(ChatColor.DARK_RED).append("{匿名プレイヤー}");
        } else {
            builder.append(playerName);
        }
        builder.append(ChatColor.RED).append(": ").append(ChatColor.AQUA).append(kill).append(" kill(s)");
        if (playerName.equals(targetPlayerName)) {
            builder.insert(0, ChatColor.BLUE + "YOU" + ChatColor.RED + " » ");
            if (isHiding) builder.insert(0, ChatColor.DARK_RED + "(Hide) ");
        }
        return builder.toString();
    }
}
