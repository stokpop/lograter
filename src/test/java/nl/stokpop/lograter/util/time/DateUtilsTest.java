package nl.stokpop.lograter.util.time;

import junit.framework.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DateUtilsTest {

    @Test
    public void testIsValidDateTimeString() throws Exception {
        Assert.assertTrue("is valid date string", DateUtils.isValidDateTimeString("20150301T111213"));
        Assert.assertFalse("is not a valid date string, without seconds", DateUtils.isValidDateTimeString("20150301T1112"));
        Assert.assertFalse("T is missing", DateUtils.isValidDateTimeString("20150301111213"));
        Assert.assertFalse("not a valid date", DateUtils.isValidDateTimeString("2015 maart april"));
    }

    @Test
    public void testStrftimePatternToDateTimeFormatterPattern() throws Exception {
        Assert.assertEquals("dd/MMM/yyyy:HH:mm:ss", DateUtils.convertStrfTimePatternToDateTimeFormatterPattern("%d/%b/%Y:%H:%M:%S"));
        Assert.assertEquals("dd/MMM/yyyy:HH:mm:ss", DateUtils.convertStrfTimePatternToDateTimeFormatterPattern("%d/%b/%Y:%T"));
    }

    @Test
    public void parseISOTimeTest() {
        assertEquals(1572877119000L, DateUtils.parseISOTime("2019-11-04T15:18:39"));
    }

}