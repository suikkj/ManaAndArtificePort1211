/*
 * Decompiled with CFR 0.152.
 */
package com.mna.events.seasonal;

import java.time.ZonedDateTime;

public class SeasonalHelper {
    private static boolean MatchLocalDate(int day, int month, int beforeDaysTolerance, int afterDaysTolerance) {
        try {
            ZonedDateTime now = ZonedDateTime.now();
            ZonedDateTime min = ZonedDateTime.of(now.getYear(), month, day, 0, 0, 0, 0, now.getZone()).minusDays(beforeDaysTolerance);
            ZonedDateTime max = ZonedDateTime.of(now.getYear(), month, day, 23, 59, 59, 0, now.getZone()).plusDays(afterDaysTolerance);
            return now.isAfter(min) && now.isBefore(max);
        }
        catch (Exception ex) {
            return false;
        }
    }

    public static boolean isHalloween() {
        return SeasonalHelper.MatchLocalDate(31, 10, 7, 1);
    }

    public static boolean isChristmas() {
        return SeasonalHelper.MatchLocalDate(25, 12, 14, 3);
    }
}

