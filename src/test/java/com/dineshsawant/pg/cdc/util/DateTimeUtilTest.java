package com.dineshsawant.pg.cdc.util;


import com.google.common.flogger.FluentLogger;
import org.junit.Test;

public class DateTimeUtilTest {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    public DateTimeUtilTest(){};

    @Test
    public void testConvertToEpoch5DigitMillis() {
        final long convertedEpoch = DateTimeUtil.convertToEpoch("2020-03-04 10:11:49.11879+00");
        logger.atInfo().log("Date %s", convertedEpoch);;
    }

    @Test
    public void testConvertToEpoch6DigitMillis() {
        final long convertedEpoch = DateTimeUtil.convertToEpoch("2020-03-02 13:52:27.897547+00");
        logger.atInfo().log("Date %s", convertedEpoch);;
    }

    @Test
    public void testConvertToEpoch1DigitMillis() {
        final long convertedEpoch = DateTimeUtil.convertToEpoch("2020-03-15 02:58:01.1+00");
        logger.atInfo().log("Date %s", convertedEpoch);;
    }

}
