package jp.azisaba.lgw.rankingdisplay.util;

import jp.azisaba.lgw.rankingdisplay.TestSize;
import jp.azisaba.lgw.rankingdisplayer.util.DateFormatUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag(TestSize.MEDIUM)
public class DateFormatUtilsTest {
    @Test
    public void FormatCalenderBySingle() {
        HashMap<Long, String> testcases = new HashMap<>();

        // Testcases
        testcases.put(1000L, "1秒");
        testcases.put(1000L*60, "1分");
        testcases.put(1000L*60*60, "1時間");
        testcases.put(1000L*60*60*24, "1日");
        // TODO 'ヵ月'のテストケース追加
//        testcases.put(1000L*60*60*24*365, "1年");

        // Execute
        testcases.forEach((timeMill, expectation) -> {
            Calendar calendar = new Calendar.Builder().setInstant(0).build();
            calendar.setTimeInMillis(timeMill);
            assertEquals(expectation, DateFormatUtils.format(calendar));
        });
    }
}
