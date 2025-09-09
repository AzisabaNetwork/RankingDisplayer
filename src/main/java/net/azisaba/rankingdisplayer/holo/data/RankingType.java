package net.azisaba.rankingdisplayer.holo.data;

import net.azisaba.kdstatusreloaded.api.KillCountType;
import net.azisaba.kdstatusreloaded.playerkd.model.KDUserData;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.function.Function;

public enum RankingType {
    DAILY(KillCountType.DAILY, k -> k.dailyKills),
    MONTHLY(KillCountType.MONTHLY, k -> k.monthlyKills),
    TOTAL(KillCountType.TOTAL, k -> k.totalKills);

    public final KillCountType killCountType;
    private final Function<KDUserData, Integer> killGetter;

    RankingType(KillCountType killCountType, Function<KDUserData, Integer> killGetter) {
        this.killCountType = killCountType;
        this.killGetter = killGetter;
    }

    @Nullable
    public static RankingType getType(@NonNull String name) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "daily" -> DAILY;
            case "monthly" -> MONTHLY;
            case "total" -> TOTAL;
            default -> null;
        };
    }

    public int getKill(KDUserData kdUserData) {
        return killGetter.apply(kdUserData);
    }
}
