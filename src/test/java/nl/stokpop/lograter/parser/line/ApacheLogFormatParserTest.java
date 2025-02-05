/*
 * Copyright (C) 2025 Peter Paul Bakker, Stokpop
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.stokpop.lograter.parser.line;

import nl.stokpop.lograter.counter.HttpMethod;
import nl.stokpop.lograter.logentry.AccessLogEntry;
import nl.stokpop.lograter.logentry.ApacheLogMapperFactory;
import org.junit.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ApacheLogFormatParserTest {

    public static final ZoneId ZONE_ID_AMS = ZoneId.of("Europe/Amsterdam");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private String epochMillisToAmsterdamDateTime(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZONE_ID_AMS).toLocalDateTime().format(DATE_TIME_FORMATTER);
    }

    @Test
	public void parseIndexOutOfBoundsError() {

		String pattern = "\"%{x-Client-IP}i\" %V %t \"%r\" " +
				"%>s %b %D \"%{APP_SESSION}C\" \"%{x-my-host}i\" \"%{Referer}i\" \"%{User-Agent}i\"";

		List<LogbackElement> elements = ApacheLogFormatParser.parse(pattern);
		final int expectedElements = ((pattern.split(" ").length) * 2) + 1;
		// System.out.println(elements);
		assertEquals("Expected every field, plus start and final literals", expectedElements, elements.size());

		Map<String, LogEntryMapper<AccessLogEntry>> mappers = ApacheLogMapperFactory.initializeMappers(elements);
		ApacheLogFormatParser<AccessLogEntry> parser = new ApacheLogFormatParser<>(elements, mappers, AccessLogEntry::new);

		String logline = "\"1.2.3.4\" www.stokpop.nl [02/Jan/2017:17:35:24 +0100] \"GET /customer/?language=en HTTP/1.0\" 200 7635 73206 \"KQdIuYTlckCM\" \"https://entry.stokpop.nl\" \"https://entry.stokpop.nl/customer/?language=en\" \"Mozilla/5.0 (iPad; CPU OS 10_2 like Mac OS X) AppleWebKit/602.3.12 (KHTML, like Gecko) Version/10.0 Mobile/14C92 Safari/602.1\"";
        AccessLogEntry entry = parser.parseLogLine(logline);

		assertEquals("1.2.3.4", entry.getField("x-Client-IP"));
		assertEquals("[02/Jan/2017:17:35:24 +0100]", entry.getField("t"));
        assertEquals("2017-01-02T17:35:24.000", epochMillisToAmsterdamDateTime(entry.getTimestamp()));
        assertEquals(HttpMethod.GET, entry.getHttpMethod());
		assertEquals(200, entry.getHttpStatus());
		assertEquals("/customer/", entry.getUrl());
		assertEquals(logline, entry.getLogline());
		assertEquals("https://entry.stokpop.nl", entry.getField("x-my-host"));
		assertEquals("KQdIuYTlckCM", entry.getField("APP_SESSION"));
		assertEquals("Mozilla/5.0 (iPad; CPU OS 10_2 like Mac OS X) AppleWebKit/602.3.12 (KHTML, like Gecko) Version/10.0 Mobile/14C92 Safari/602.1", entry.getUserAgent());
		//System.out.println(entry);

	}

	@Test
    public void parse() {

        String pattern = "\"%{x-Client-IP}i\" %V %t \"%r\" %>s" +
                " %b %D \"%{x-my-host}i\" \"%{Referer}i\" \"%{User-Agent}i\"";

        List<LogbackElement> elements = ApacheLogFormatParser.parse(pattern);
        final int expectedElements = ((pattern.split(" ").length) * 2) + 1;
        // System.out.println(elements);
        assertEquals("Expected every field, plus start and final literals", expectedElements, elements.size());

        Map<String, LogEntryMapper<AccessLogEntry>> mappers = ApacheLogMapperFactory.initializeMappers(elements);
        ApacheLogFormatParser<AccessLogEntry> parser = new ApacheLogFormatParser<>(elements, mappers, AccessLogEntry::new);

        String logline = "\"127.0.0.1\" www.stokpop.nl [21/Dec/2012:00:01:05 +0100] \"GET /service/order/v1 HTTP/1.0\" 200" +
                " 501 353552 \"https://entry.stokpop.nl\" \"-\" \"Stokpop Mobiel, 3.0.2, Android, 2.3.4, samsung, GT-I9100\"";

        AccessLogEntry entry = parser.parseLogLine(logline);

        assertEquals("127.0.0.1", entry.getField("x-Client-IP"));
        assertEquals("[21/Dec/2012:00:01:05 +0100]", entry.getField("t"));
        assertEquals("2012-12-21T00:01:05.000", epochMillisToAmsterdamDateTime(entry.getTimestamp()));
        assertEquals(HttpMethod.GET, entry.getHttpMethod());
        assertEquals(200, entry.getHttpStatus());
        assertEquals("/service/order/v1", entry.getUrl());
        assertEquals(logline, entry.getLogline());
        assertEquals("https://entry.stokpop.nl", entry.getField("x-my-host"));
        assertEquals("Stokpop Mobiel, 3.0.2, Android, 2.3.4, samsung, GT-I9100", entry.getUserAgent());
        //System.out.println(entry);

    }

    @Test
    public void parseCase20191115() {

        String pattern = "%h:%{local}p %l %u %t \"%>r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" %{for-id}n %Dusec";

        List<LogbackElement> elements = ApacheLogFormatParser.parse(pattern);
        //System.out.println(elements);
        assertEquals("Expected every field, plus start and final literals", 26, elements.size());

        Map<String, LogEntryMapper<AccessLogEntry>> mappers = ApacheLogMapperFactory.initializeMappers(elements);
        ApacheLogFormatParser<AccessLogEntry> parser = new ApacheLogFormatParser<>(elements, mappers, AccessLogEntry::new);

        String logline = "127.0.0.1:20053 - - [08/Nov/2019:12:01:04 +0100] \"POST / HTTP/1.1\" 200 2114 \"-\" \"Client\" asldkfjhasldk 33087usec";

        AccessLogEntry entry = parser.parseLogLine(logline);

        assertEquals("127.0.0.1", entry.getField("h"));
        assertEquals("20053", entry.getField("p"));
        assertEquals("-", entry.getField("l"));
        assertEquals("-", entry.getField("u"));
        assertEquals("[08/Nov/2019:12:01:04 +0100]", entry.getField("t"));
        assertEquals("2019-11-08T12:01:04.000", epochMillisToAmsterdamDateTime(entry.getTimestamp()));
        assertEquals(HttpMethod.POST, entry.getHttpMethod());
        assertEquals(200, entry.getHttpStatus());
        assertEquals("/", entry.getUrl());
        assertEquals("2114", entry.getField("b"));
        assertEquals(logline, entry.getLogline());
        assertEquals("-", entry.getReferrer());
        assertEquals("Client", entry.getUserAgent());
        assertEquals(33087, entry.getDurationInMicros());
        //System.out.println(entry);
    }

    @Test
    public void parseDoublePercentage() {

        String pattern =
                "%h %l %u %t %U" +
                " \"%r\" %>s %b \"%{Referer}i\" [%X-Customer-IP}i]" +
                " [%{X-Forwarded-For}i] [%{stokpopSite}C] [%{C-Control}o] [%{E-Control}o] \"%{User-Agent}i\"" +
                " \"%{Cookie}n\" %{ostream}n/%{istream}n (%{rat}n%%) %T seconds" +
                " / %D microseconds";

        List<LogbackElement> elements = ApacheLogFormatParser.parse(pattern);
        final int expectedElements = 43;
        //System.out.println(elements);
        assertEquals("Expected every field, plus start and final literals", expectedElements, elements.size());

        Map<String, LogEntryMapper<AccessLogEntry>> mappers = ApacheLogMapperFactory.initializeMappers(elements);
        ApacheLogFormatParser<AccessLogEntry> parser = new ApacheLogFormatParser<>(elements, mappers, AccessLogEntry::new);

        String logline =
                "127.0.0.1 - - [05/Mar/2019:10:33:34 +0100] /research/beverages.html" +
                " \"GET /research/beerbeverages.html HTTP/1.1\" 200 8046 \"https://xyz/research/beerbeverages.html\" [127.0.0.1]" +
                " [[-]] [-] [no-c] [-] \"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR)\"" +
                " \"-\" 8028/35850 (22%) 0 seconds" +
                " / 211678 microseconds";

        AccessLogEntry entry = parser.parseLogLine(logline);

        assertEquals("127.0.0.1", entry.getRemoteHost());
        assertEquals("[05/Mar/2019:10:33:34 +0100]", entry.getField("t"));
        assertEquals("2019-03-05T10:33:34.000", epochMillisToAmsterdamDateTime(entry.getTimestamp()));
        assertEquals(HttpMethod.GET, entry.getHttpMethod());
        assertEquals(200, entry.getHttpStatus());
        assertEquals("/research/beerbeverages.html", entry.getUrl());
        assertEquals("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR)", entry.getUserAgent());
        assertEquals("no-c", entry.getField("C-Control"));
        assertEquals(logline, entry.getLogline());

        //System.out.println(entry);

    }

    /**
     * To cope with milliseconds and/or nanoseconds, apache allows %{msec_frac}t and %{usec_frac}t.
     * This is for apache 2.4 and up. Another example: [%{%d/%b/%Y:%H:%M:%S}t.%{msec_frac}t %{%z}t]
     * This will give times like [10/Apr/2012:10:47:22.027 +0000]
     * Note that multiple %t are possible.
     * See: https://httpd.apache.org/docs/trunk/mod/mod_log_config.html
     */
    @Test
    public void parseMillisFracTimestamp() {

        String pattern = "%h  %V  %l %u %{%d/%b/%Y:%T}t.%{msec_frac}t  %{C_ID}x  %{C_STATUS}x  %{C_PROT}x %{C_BAR}x \"%r\" %>s %b %D \"%{Referer}i\" \"%{User-Agent}i\"  %{C_HASH}x";

        List<LogbackElement> elements = ApacheLogFormatParser.parse(pattern);
        final int expectedElements = 35;
        //System.out.println(elements);
        assertEquals("Expected every field, plus start and final literals", expectedElements, elements.size());

        Map<String, LogEntryMapper<AccessLogEntry>> mappers = ApacheLogMapperFactory.initializeMappers(elements);
        ApacheLogFormatParser<AccessLogEntry> parser = new ApacheLogFormatParser<>(elements, mappers, AccessLogEntry::new);

        String logline = "127.0.0.1  www.stokpop.com  - - 23/May/2016:07:15:14.257  C-Site  SUCCESS  foo bar \"POST /request/ HTTP/1.1\" 200 1436 23887 \"-\" \"Apache-HttpClient/4.2.5 (java 1.5)\"  abc";

        AccessLogEntry entry = parser.parseLogLine(logline);

        assertEquals("127.0.0.1", entry.getRemoteHost());
        assertEquals("23/May/2016:07:15:14", entry.getField("t"));
        assertEquals("257", entry.getField("msec_frac"));
        assertEquals("2016-05-23T07:15:14.257", epochMillisToAmsterdamDateTime(entry.getTimestamp()));
        assertEquals(HttpMethod.POST, entry.getHttpMethod());
        assertEquals(200, entry.getHttpStatus());
        assertEquals("/request/", entry.getUrl());
        assertEquals("Apache-HttpClient/4.2.5 (java 1.5)", entry.getUserAgent());
        assertEquals(logline, entry.getLogline());

        //System.out.println(entry);

    }

    @Test
    public void parseVeryLongDuration() {

        String pattern =
                "%h %V %l %u %t \"%r\" %>s %b %D \"%{SESSION}C\" \"%{x-my-host}i\" \"%{Referer}i\" \"%{User-Agent}i\"";

        List<LogbackElement> elements = ApacheLogFormatParser.parse(pattern);
        final int expectedElements = 27;
        //System.out.println(elements);
        assertEquals("Expected every field, plus start and final literals", expectedElements, elements.size());

        Map<String, LogEntryMapper<AccessLogEntry>> mappers = ApacheLogMapperFactory.initializeMappers(elements);
        ApacheLogFormatParser<AccessLogEntry> parser = new ApacheLogFormatParser<>(elements, mappers, AccessLogEntry::new);

        String logline = "1.2.3.4 foo.lograter.stokpop.nl - - [07/Jan/2019:00:56:00 +0100] \"GET ABCdef123/243215/ HTTP/1.1\" 200 4196 2160841008 \"-\" \"http://foo.lograter.stokpop.nl\" \"-\" \"Mozilla/5.0 (Windows; U; MSIE 7.0; Windows NT 6.0; en-US)\"";

        AccessLogEntry entry = parser.parseLogLine(logline);

        assertEquals("1.2.3.4", entry.getRemoteHost());
        assertEquals("[07/Jan/2019:00:56:00 +0100]", entry.getField("t"));
        assertEquals("2019-01-07T00:56:00.000", epochMillisToAmsterdamDateTime(entry.getTimestamp()));
        assertEquals(HttpMethod.GET, entry.getHttpMethod());
        assertEquals(200, entry.getHttpStatus());
        assertEquals("ABCdef123/243215/", entry.getUrl());
        assertEquals("Mozilla/5.0 (Windows; U; MSIE 7.0; Windows NT 6.0; en-US)", entry.getUserAgent());
        assertEquals(2160841008L/1000, entry.getDurationInMillis());
        assertEquals(4196, entry.getBytes());
        assertEquals(logline, entry.getLogline());

        //System.out.println(entry);

    }

    @Test
    public void parseFailurePercentageNeedsVariableException() {

        String pattern =
                "\"%{x-Client-IP}i\" %V %t \"%r\" %>s %b %D \"%{SESSION}C\" \"%{x-my-host}i\" \"%{Referer}i\" \"%{User-Agent}i\"";

        List<LogbackElement> elements = ApacheLogFormatParser.parse(pattern);
        final int expectedElements = 23;
        //System.out.println(elements);
        assertEquals("Expected every field, plus start and final literals", expectedElements, elements.size());

        Map<String, LogEntryMapper<AccessLogEntry>> mappers = ApacheLogMapperFactory.initializeMappers(elements);
        ApacheLogFormatParser<AccessLogEntry> parser = new ApacheLogFormatParser<>(elements, mappers, AccessLogEntry::new);

        String logline =
                "\"-\" lograter.stokpop.nl [05/Apr/2019:00:01:40 +0200] \"GET ABCdef123/servlet/34873?version=5 HTTP/1.1\" 200 31678 9597 \"-\" \"-\" \"-\" \"Mozilla/4.0 (compatible; MSIE 4.01; Windows NT)\"";

        AccessLogEntry entry = parser.parseLogLine(logline);

        assertEquals("-", entry.getField("x-Client-IP"));
        assertEquals("[05/Apr/2019:00:01:40 +0200]", entry.getField("t"));
        assertEquals("2019-04-05T00:01:40.000", epochMillisToAmsterdamDateTime(entry.getTimestamp()));
        assertEquals(HttpMethod.GET, entry.getHttpMethod());
        assertEquals(200, entry.getHttpStatus());
        assertEquals("ABCdef123/servlet/34873", entry.getUrl());
        assertEquals(logline, entry.getLogline());
        assertEquals("Mozilla/4.0 (compatible; MSIE 4.01; Windows NT)", entry.getUserAgent());

        //System.out.println(entry);

    }

    @Test
    public void parseMinimalLine() {

        String pattern =
                "\"%{x-Client-IP}i\" %V %t \"%r\" %>s %b %D \"%{x-my-host}i\" \"%{Referer}i\" \"%Q\" \"%{User-Agent}i\"";

        List<LogbackElement> elements = ApacheLogFormatParser.parse(pattern);
        final int expectedElements = 23;
        //System.out.println(elements);
        assertEquals("Expected every field, plus start and final literals", expectedElements, elements.size());

        Map<String, LogEntryMapper<AccessLogEntry>> mappers = ApacheLogMapperFactory.initializeMappers(elements);
        ApacheLogFormatParser<AccessLogEntry> parser = new ApacheLogFormatParser<>(elements, mappers, AccessLogEntry::new);

        String logline = "\"-\" linux.stokpop.nl [24/May/2019:00:01:02 +0200] \"OPTIONS * HTTP/1.0\" 200 - 106 \"-\" \"-\" \"-\" \"Apache (internal dummy connection)\"";


        AccessLogEntry entry = parser.parseLogLine(logline);

        assertEquals("-", entry.getField("x-Client-IP"));
        assertEquals("linux.stokpop.nl", entry.getField("V"));
        assertEquals("[24/May/2019:00:01:02 +0200]", entry.getField("t"));
        assertEquals("2019-05-24T00:01:02.000", epochMillisToAmsterdamDateTime(entry.getTimestamp()));
        assertEquals(HttpMethod.OPTIONS, entry.getHttpMethod());
        assertEquals(200, entry.getHttpStatus());
        assertEquals("*", entry.getUrl());
        assertEquals(logline, entry.getLogline());
        assertEquals("Apache (internal dummy connection)", entry.getUserAgent());

        //System.out.println(entry);
    }

    @Test
    public void parseFalseFailureLine() {

        String pattern =
                "%h  %V  %l %u %{%d/%b/%Y:%T}t.%{msec_frac}t  %{C_ID}x  %{C_STATUS}x  %{C_PROT}x %{C_BAR}x \"%r\" %>s %b %D \"%{Referer}i\" \"%{User-Agent}i\"  %{C_HASH}x";

        List<LogbackElement> elements = ApacheLogFormatParser.parse(pattern);
        final int expectedElements = 35;
        //System.out.println(elements);
        assertEquals("Expected every field, plus start and final literals", expectedElements, elements.size());

        Map<String, LogEntryMapper<AccessLogEntry>> mappers = ApacheLogMapperFactory.initializeMappers(elements);
        ApacheLogFormatParser<AccessLogEntry> parser = new ApacheLogFormatParser<>(elements, mappers, AccessLogEntry::new);

        String logline = "22.222.222.22  linux.stokpop.nl  - - 17/May/2019:04:21:29.485  servery.acme.nl  SUCCESS  BAR FOO \"POST /vla HTTP/1.1\" 200 448 98796 \"-\" \"Apache-HttpClient/4.5.2 (Java/1.7.0)\"  ABCDEFGH";

        AccessLogEntry entry = parser.parseLogLine(logline);

        assertEquals("22.222.222.22", entry.getField("h"));
        assertEquals("linux.stokpop.nl", entry.getField("V"));
        assertEquals("17/May/2019:04:21:29", entry.getField("t"));
        assertEquals("2019-05-17T04:21:29.485", epochMillisToAmsterdamDateTime(entry.getTimestamp()));
        assertEquals("485", entry.getField("msec_frac"));

        assertEquals(HttpMethod.POST, entry.getHttpMethod());
        assertEquals(200, entry.getHttpStatus());
        assertEquals("/vla", entry.getUrl());
        assertEquals(logline, entry.getLogline());
        assertEquals("Apache-HttpClient/4.5.2 (Java/1.7.0)", entry.getUserAgent());

        //System.out.println(entry);
    }

    @Test
    public void parseDurationWithPostfixLiteral() {

        String pattern = "%Dusec";

        List<LogbackElement> elements = ApacheLogFormatParser.parse(pattern);

        final int expectedElements = 4;
        //.println(elements);

        assertEquals("Expected 4 elements, including start and end elements.", expectedElements, elements.size());
        assertEquals("Expected D directive", "D", ((LogbackDirective) elements.get(1)).getDirective());
        assertEquals("Expected usec literal", "usec", ((LogbackLiteral) elements.get(2)).getLiteral());
    }

    @Test
    public void parseAccessLog() {

        String pattern = "\"%{x-Client-IP}i\" %V %t \"%{x-session-id}i\"  \"%r\" %>s %b %D";

        List<LogbackElement> elements = ApacheLogFormatParser.parse(pattern);

        // also a final "empty" element after final variable (%D)
        final int expectedElements = 17;
        //System.out.println(elements);
        assertEquals("Expected every field, plus start and final literals", expectedElements, elements.size());

        Map<String, LogEntryMapper<AccessLogEntry>> mappers = ApacheLogMapperFactory.initializeMappers(elements);
        ApacheLogFormatParser<AccessLogEntry> parser = new ApacheLogFormatParser<>(elements, mappers, AccessLogEntry::new);

        testCase1(parser);

        testCase2(parser);

    }

    private void testCase2(ApacheLogFormatParser<AccessLogEntry> parser) {

        String logline = " \"11.22.33.44\" windows.stokpop.com [11/Sep/2019:00:32:57 +0200] \"ABCdef123\"  \"POST /foo/bar/v1 HTTP/1.1\" 200 249 3588";

        AccessLogEntry entry = parser.parseLogLine(logline);

        assertEquals("11.22.33.44", entry.getField("x-Client-IP"));
        assertEquals("windows.stokpop.com", entry.getField("V"));
        assertEquals("[11/Sep/2019:00:32:57 +0200]", entry.getField("t"));
        assertEquals("2019-09-11T00:32:57.000", epochMillisToAmsterdamDateTime(entry.getTimestamp()));
        assertEquals(HttpMethod.POST, entry.getHttpMethod());
        assertEquals(200, entry.getHttpStatus());
        assertEquals("/foo/bar/v1", entry.getUrl());
        assertEquals(logline, entry.getLogline());
        assertEquals(3588, entry.getDurationInMicros());
        assertNull(entry.getUserAgent());

    }

    private void testCase1(ApacheLogFormatParser<AccessLogEntry> parser) {
        String logline = " \"10.0.0.123\" windows.stokpop.com [11/Sep/2019:00:30:02 +0200] \"abc\"  \"GET /foo/overview/v1 HTTP/1.1\" 403 207 10107";

        AccessLogEntry entry = parser.parseLogLine(logline);

        assertEquals("10.0.0.123", entry.getField("x-Client-IP"));
        assertEquals("windows.stokpop.com", entry.getField("V"));
        assertEquals("[11/Sep/2019:00:30:02 +0200]", entry.getField("t"));
        assertEquals("2019-09-11T00:30:02.000", epochMillisToAmsterdamDateTime(entry.getTimestamp()));
        assertEquals(HttpMethod.GET, entry.getHttpMethod());
        assertEquals(403, entry.getHttpStatus());
        assertEquals("/foo/overview/v1", entry.getUrl());
        assertEquals(logline, entry.getLogline());
        assertEquals(10107, entry.getDurationInMicros());
        assertNull(entry.getUserAgent());
    }

    @Test
    public void testAfterburnerTcpConnect() {

        String pattern =
                "%{%Y-%m-%d %H:%M:%s}t {\"message\":\"{ '%r':'%{success}x', 'connect-nano-duration':%{duration-nanoseconds}x, 'close-nano-duration':%{close-nano-duration}x, 'host':'%{host}x', 'port':%{port}x }\",\"name\":\"%{name}x\",\"durationInMillis\":%{durationMillis}x";

        List<LogbackElement> elements = ApacheLogFormatParser.parse(pattern);
        final int expectedElements = 19;
        //System.out.println(elements);
        assertEquals("Expected every field, plus start and final literals", expectedElements, elements.size());

        Map<String, LogEntryMapper<AccessLogEntry>> mappers = ApacheLogMapperFactory.initializeMappers(elements);
        ApacheLogFormatParser<AccessLogEntry> parser = new ApacheLogFormatParser<>(elements, mappers, AccessLogEntry::new);

        String logline = "2019-03-15 10:56:05 {\"message\":\"{ 'tpc-connect':'success', 'connect-nano-duration':1790913, 'close-nano-duration':40900, 'host':'database.stokpop.nl', 'port':1433 }\",\"name\":\"Afterburner-One\",\"durationInMillis\":2}\n";

        AccessLogEntry entry = parser.parseLogLine(logline);
        assertEquals("tpc-connect", entry.getUrl());
        assertEquals("2019-03-15T10:56:05.000", epochMillisToAmsterdamDateTime(entry.getTimestamp()));
        assertEquals(logline, entry.getLogline());
        assertEquals(Math.round(1790913.0/1000), entry.getDurationInMicros());

        //System.out.println(entry);
    }

    @Test
    public void testCloudLog() {
        String pattern =
                "%{one}X - %{[yyyy-MM-dd'T'HH:mm:ss.SSSZ]}t \"%r\" %s %{two}X response_time:%T %{three}X";

        List<LogbackElement> elements = ApacheLogFormatParser.parse(pattern);
        final int expectedElements = 15;
        //System.out.println(elements);
        assertEquals("Expected every field, plus start and final literals", expectedElements, elements.size());

        Map<String, LogEntryMapper<AccessLogEntry>> mappers = ApacheLogMapperFactory.initializeMappers(elements);
        ApacheLogFormatParser<AccessLogEntry> parser = new ApacheLogFormatParser<>(elements, mappers, AccessLogEntry::new);

        String logline = "    2020-01-29T16:45:45.29+0100 [RTR/1] OUT afterburner-cpu.xxx.yyy.zzz.nl - [2020-01-29T15:45:44.813+0000] \"GET /delay?duration=200 HTTP/1.1\" 200 0 85 \"-\" \"curl/7.64.1\" \"1.1.1.1:555\" \"2.2.2.2:4444\" x_forwarded_for:\"3.3.3.3\" x_forwarded_proto:\"https\" vcap_request_id:\"xxxx-1d3f-411a-56de-yyyy\" response_time:0.477786246 app_id:\"xxx-6c11-4f8f-a8cc-yyy\" app_index:\"0\" x_client_ip:\"-\" x_session_id:\"-\" x_b3_traceid:\"eorituwerio\" x_b3_spanid:\"weopirtu\" x_b3_parentspanid:\"-\" b3:\"woiertu\"";

        AccessLogEntry entry = parser.parseLogLine(logline);

        assertEquals("/delay", entry.getUrl());
        assertEquals(logline, entry.getLogline());
        assertEquals(477, entry.getDurationInMillis());
        assertEquals(200, entry.getHttpStatus());

        assertEquals("2020-01-29T16:45:44.813", epochMillisToAmsterdamDateTime(entry.getTimestamp()));

    }

    @Test
    public void testCloudLogWithNanoPrecision() {
        String pattern =
            "%{one}X - %{[yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSZ]}t \"%r\" %s %{two}X response_time:%T %{three}X";

        List<LogbackElement> elements = ApacheLogFormatParser.parse(pattern);
        final int expectedElements = 15;
        //System.out.println(elements);
        assertEquals("Expected every field, plus start and final literals", expectedElements, elements.size());

        Map<String, LogEntryMapper<AccessLogEntry>> mappers = ApacheLogMapperFactory.initializeMappers(elements);
        ApacheLogFormatParser<AccessLogEntry> parser = new ApacheLogFormatParser<>(elements, mappers, AccessLogEntry::new);

        String logline = "    2020-01-29T16:45:45.29+0100 [RTR/1] OUT afterburner-cpu.xxx.yyy.zzz.nl - [2020-01-29T15:45:44.123555666Z] \"GET /delay?duration=200 HTTP/1.1\" 200 0 85 \"-\" \"curl/7.64.1\" \"1.1.1.1:555\" \"2.2.2.2:4444\" x_forwarded_for:\"3.3.3.3\" x_forwarded_proto:\"https\" vcap_request_id:\"xxxx-1d3f-411a-56de-yyyy\" response_time:0.477786246 app_id:\"xxx-6c11-4f8f-a8cc-yyy\" app_index:\"0\" x_client_ip:\"-\" x_session_id:\"-\" x_b3_traceid:\"eorituwerio\" x_b3_spanid:\"weopirtu\" x_b3_parentspanid:\"-\" b3:\"woiertu\"";

        AccessLogEntry entry = parser.parseLogLine(logline);

        assertEquals("/delay", entry.getUrl());
        assertEquals(logline, entry.getLogline());
        assertEquals(477, entry.getDurationInMillis());
        assertEquals(200, entry.getHttpStatus());

        assertEquals("2020-01-29T16:45:44.123", epochMillisToAmsterdamDateTime(entry.getTimestamp()));
    }

    @Test
    public void testDirectivesBeforeVariable() {
        String pattern = "%X{one} x_f:\"%X{x_f}\"";

        List<LogbackElement> elements = ApacheLogFormatParser.parse(pattern);
        final int expectedElements = 5;
        System.out.println(elements);
        assertEquals("Expected every field, plus start and final literals", expectedElements, elements.size());

        Map<String, LogEntryMapper<AccessLogEntry>> mappers = ApacheLogMapperFactory.initializeMappers(elements);
        ApacheLogFormatParser<AccessLogEntry> parser = new ApacheLogFormatParser<>(elements, mappers, AccessLogEntry::new);

        String logline = "bob x_f:\"1.2.3.4\"";

        AccessLogEntry entry = parser.parseLogLine(logline);

        assertEquals("bob", entry.getField("one"));
        assertEquals("1.2.3.4", entry.getField("x_f"));
        assertEquals(logline, entry.getLogline());
    }

    @Test
    public void testDirectivesAfterVariable() {
        String pattern = "%{one}X x_f:\"%{x_f}X\"";

        List<LogbackElement> elements = ApacheLogFormatParser.parse(pattern);
        final int expectedElements = 5;
        System.out.println(elements);
        assertEquals("Expected every field, plus start and final literals", expectedElements, elements.size());

        Map<String, LogEntryMapper<AccessLogEntry>> mappers = ApacheLogMapperFactory.initializeMappers(elements);
        ApacheLogFormatParser<AccessLogEntry> parser = new ApacheLogFormatParser<>(elements, mappers, AccessLogEntry::new);

        String logline = "bob x_f:\"1.2.3.4\"";

        AccessLogEntry entry = parser.parseLogLine(logline);

        assertEquals("bob", entry.getField("one"));
        assertEquals("1.2.3.4", entry.getField("x_f"));
        assertEquals(logline, entry.getLogline());
    }



}
