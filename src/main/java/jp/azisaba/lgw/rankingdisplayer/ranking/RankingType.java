package jp.azisaba.lgw.rankingdisplayer.ranking;

import jp.azisaba.lgw.kdstatus.utils.TimeUnit;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public enum RankingType {
    DAILY(TimeUnit.DAILY, "Daily"),
    MONTHLY(TimeUnit.MONTHLY, "Monthly"),
    TOTAL(TimeUnit.LIFETIME, "Total");
    @Getter
    private final TimeUnit kdStatusTimeUnit;
    private final String name;

    RankingType(TimeUnit kdStatusTimeUnit, String name) {
        this.kdStatusTimeUnit = kdStatusTimeUnit;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Nullable
    public static RankingType getType(String name) {
        switch (name) {
            case "daily":
                return DAILY;
            case "monthly":
                return MONTHLY;
            case "total":
                return TOTAL;
        }
        return null;
    }
}
