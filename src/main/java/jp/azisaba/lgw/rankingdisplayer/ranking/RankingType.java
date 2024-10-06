package jp.azisaba.lgw.rankingdisplayer.ranking;

import jp.azisaba.lgw.kdstatus.utils.TimeUnit;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public enum RankingType {
    DAILY(TimeUnit.DAILY, "Daily"),
    MONTHLY(TimeUnit.MONTHLY, "Monthly"),
    TOTAL(TimeUnit.LIFETIME, "Total");
    private static final HashMap<String, RankingType> convertMap = new HashMap<>();

    @Getter
    private final TimeUnit kdStatusTimeUnit;
    private final String name;

    RankingType(TimeUnit kdStatusTimeUnit, String name) {
        this.kdStatusTimeUnit = kdStatusTimeUnit;
        this.name = name;
        addConversion(name, this);
    }

    @Override
    public String toString() {
        return name;
    }

    private static void addConversion(String name, RankingType type) {
        convertMap.put(name, type);
    }

    @Nullable
    public static RankingType getType(String name) {
        return convertMap.get(name);
    }
}
