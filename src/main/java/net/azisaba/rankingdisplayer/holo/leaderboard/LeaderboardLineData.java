package net.azisaba.rankingdisplayer.holo.leaderboard;

import net.azisaba.rankingdisplayer.setting.RankingHideManager;
import org.bukkit.ChatColor;

import java.util.UUID;

public record LeaderboardLineData(
        UUID uuid,
        String dataPlayerName,
        boolean isNothing,
        String rankPrefix,
        String killSuffix
) {
    public String getLine(String playerName) {
        boolean isHiding = RankingHideManager.isHiding(uuid);
        if (isNothing) return rankPrefix;

        StringBuilder builder = new StringBuilder(rankPrefix);
        boolean isMe = dataPlayerName.equals(playerName);
        if (isMe) {
            // もし、自分自身なら先頭に"YOU"をついかする
            builder.insert(0, ChatColor.BLUE + "YOU" + ChatColor.RED + " » ");
            if (isHiding) builder.insert(0, ChatColor.DARK_RED + "(Hide) ");
        }
        if (isHiding && !isMe) {
            // 非公開 + 自分自身ではない 場合に"匿名プレイヤー"にする。
            builder.append(ChatColor.DARK_RED).append("{匿名プレイヤー}");
        } else {
            builder.append(dataPlayerName);
        }
        builder.append(killSuffix);
        return builder.toString();
    }
}
