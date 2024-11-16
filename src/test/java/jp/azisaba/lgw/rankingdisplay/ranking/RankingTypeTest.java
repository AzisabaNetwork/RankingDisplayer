package jp.azisaba.lgw.rankingdisplay.ranking;

import jp.azisaba.lgw.kdstatus.utils.TimeUnit;
import jp.azisaba.lgw.rankingdisplay.TestSize;
import jp.azisaba.lgw.rankingdisplayer.ranking.RankingType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag(TestSize.SMALL)
public class RankingTypeTest {
    @Test
    public void ConversionTest() {
        assertEquals(RankingType.DAILY, RankingType.getType("daily"));
        assertEquals(RankingType.MONTHLY, RankingType.getType("monthly"));
        assertEquals(RankingType.TOTAL, RankingType.getType("total"));
        assertNull(RankingType.getType("none"));
    }

    @Test
    public void ReConversionTest() {
        assertEquals(RankingType.DAILY, RankingType.getType(RankingType.DAILY.name().toLowerCase(Locale.ROOT)));
        assertEquals(RankingType.MONTHLY, RankingType.getType(RankingType.MONTHLY.name().toLowerCase(Locale.ROOT)));
        assertEquals(RankingType.TOTAL, RankingType.getType(RankingType.TOTAL.name().toLowerCase(Locale.ROOT)));
    }

    @Test
    public void TimeUnitMatchTest() {
        assertEquals(TimeUnit.DAILY, RankingType.DAILY.getKdStatusTimeUnit());
        assertEquals(TimeUnit.MONTHLY, RankingType.MONTHLY.getKdStatusTimeUnit());
        assertEquals(TimeUnit.LIFETIME, RankingType.TOTAL.getKdStatusTimeUnit());
    }
}
