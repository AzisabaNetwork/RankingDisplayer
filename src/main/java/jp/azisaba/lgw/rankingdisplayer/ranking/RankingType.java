package jp.azisaba.lgw.rankingdisplayer.ranking;

import jp.azisaba.lgw.kdstatus.utils.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RankingType {

    DAILY(TimeUnit.DAILY, "Daily"),
    MONTHLY(TimeUnit.MONTHLY, "Monthly"),
    TOTAL(TimeUnit.LIFETIME, "Total");

    @Getter
    private final TimeUnit kdStatusTimeUnit;
    private final String name;

    @Override
    public String toString() {
        return name;
    }
}
