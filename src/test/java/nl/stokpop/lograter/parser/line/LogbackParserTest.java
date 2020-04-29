/*
 * Copyright (C) 2020 Peter Paul Bakker, Stokpop Software Solutions
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

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.logentry.LogbackLogEntry;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LogbackParserTest {
    
    @Test
    public void testParse() {

		String logbackpattern = "[%d{dd-MM-yyyy HH:mm:ss.SSS}] [%t] [%p][%marker] [Session=%X{Session}] [Level=%X{Level}] [Type=%X{Type}] [Id=%X{Id}] [Customer=%X{customer}] [Service=%X{serviceId}] [App=%X{AppName}] %c - %m%n";

        LogbackParser<LogbackLogEntry> parser = LogbackParser.createLogbackParser(logbackpattern);

        String logline = "[12-06-2019 04:15:49.764] [myThread] [WARN][] [Session=] [Level=] [Type=] [Id=] [Customer=myId123] [Service=myService12] [App=myApp12] nl.stokpop.ExceptionResolver - Exception occurred in /foo/bar/app12 App. Message: null.";

        LogbackLogEntry entry = parser.parseLogLine(logline);

        assertEquals("myThread", entry.getThreadName());
        assertEquals("myId123", entry.getField("customer"));
    }

    @Test
    public void testParseWithSpaceInDate() {

        // this works ok because of the " [" literal between %d and %thread
        String logbackpattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [reqId=%X{reqId}] [Session=%X{Session}] %logger{36} - %msg%n";

        LogbackParser<LogbackLogEntry> parser = LogbackParser.createLogbackParser(logbackpattern);

        String logline = "2019-03-22 23:29:51.878 [WebContainer : 206419] INFO  [reqId=abc123] [Session=qwe456] e.s.n.Mapping - mapping not found 123";

        LogbackLogEntry entry = parser.parseLogLine(logline);

        assertEquals("WebContainer : 206419", entry.getThreadName());
        assertEquals("qwe456", entry.getField("Session"));
        assertEquals("2019-03-22 23:29:51.878", entry.getField("d"));
    }

    @Test
    public void testParseFormattingSyntax() {
        String logbackpattern = "%d;%t;%p;%marker;%X{Session};%X{Level};%X{Type};%X{Id};%X{boo};%.-10X{EID};%X{CID};%X{service};%X{AppName};%X{AppId};%c;%m%n %xEx{short}";

        LogbackParser<LogbackLogEntry> parser = LogbackParser.createLogbackParser(logbackpattern);

        String logline = "2019-04-06 20:31:00,159;myThread-3;INFO;;;;;;;;;;;;nl.stokpop.ProviderLocator;No services found.\n";
        LogbackLogEntry entry = parser.parseLogLine(logline);

        assertEquals("myThread-3", entry.getThreadName());
        assertEquals("", entry.getField("boo"));

    }

    @Test
	public void testParseFormattingFromSomeProject() {
    	// d{ISO8601}        date ISO8601 e.g. 2006-10-20 14:06:49,812
		// thread            name of the thread
		// p                 level of the logging event
		// C{0}              class name caller without package name prefix
		// X{correlationId}  MDC (mapped diagnostic context) {key:-defaultVal}
		// m                 application-supplied message
		// n                 platform dependent line separator char or chars
		// xEx{short}        short stack trace of exception associated with logging event annotated with packaging info
    	String logbackpattern = "[%d{ISO8601}] [%thread][%p][%C{0}] transactionId=%X{correlationId} - %m%n%xEx{short}";
		LogbackParser<LogbackLogEntry> parser = LogbackParser.createLogbackParser(logbackpattern);

		String logline = "[2019-04-06T20:31:00.159+02:00] [myThread-3][ERROR][Classname] transactionId=mdcCorrId - app message\n" +
				"mainPackage.foo.bar.TestException: Houston we have a problem\n" +
				"  at mainPackage.foo.bar.TestThrower.fire(TestThrower.java:22)) ~[wombat-1.3.jar:1.3]";
		LogbackLogEntry entry = parser.parseLogLine(logline);

		assertEquals("myThread-3", entry.getThreadName());
		assertEquals("ERROR", entry.getLogLevel());
	}

    @Test
    public void testParseFormattingFailure() {
        String logbackpattern = "%d;%p;%marker;%X{Session};%X{Level};%X{Type};%X{Id};%X{boo};%X{service};%X{AppName};%X{AppId};%c;%m%n %xEx{full}";
        LogbackParser<LogbackLogEntry> parser = LogbackParser.createLogbackParser(logbackpattern);
        String logline = "2019-04-05 16:21:12,108;INFO;;s123;l5;BOO;id123;id123;;cdata;1234;nl.stokpop.DataImpl;Inside DataImpl - fetchDetails";
        LogbackLogEntry entry = parser.parseLogLine(logline);

        assertEquals("2019-04-05 16:21:12,108", entry.getField("d"));
        assertEquals("INFO", entry.getField("p"));
    }

	@Test
	public void testParseWithFullMdcDump() {
		String logbackpattern = "%d;%X;%msg%n";
        LogbackParser<LogbackLogEntry> parser = LogbackParser.createLogbackParser(logbackpattern);
		String logline = "2019-02-07 21:32:42,843;Session=session321, cache=myCache5, event=MISS, myID=54321, AppName=stp-service, customer=cust123;67605";
        LogbackLogEntry entry = parser.parseLogLine(logline);

		String expectedSession = "session321";
		assertEquals("2019-02-07 21:32:42,843", entry.getField("d"));
		assertEquals("myCache5", entry.getField("cache"));
		assertEquals("MISS", entry.getField("event"));
		assertEquals(expectedSession, entry.getField("Session"));
		assertEquals("67605", entry.getMessage());
	}

	@Test(expected = LogRaterException.class)
	public void testParseWithBrokenMdcDump() {
		String logbackpattern = "%d;%X;%msg%n";
        LogbackParser<LogbackLogEntry> parser = LogbackParser.createLogbackParser(logbackpattern);
		String logline = "2019-02-07 21:32:42,843;Session, cache=myCache5, event=MISS;67605";
		parser.parseLogLine(logline);
	}

	@Test
	public void testParseWithSpacesInMdcDump() {
		String logbackpattern = "%d;%X;%msg%n";
        LogbackParser<LogbackLogEntry> parser = LogbackParser.createLogbackParser(logbackpattern);
		String logline = "2019-02-07 21:32:42,843;Session = 1, cache= myCache5, event=MISS  ,;67605";
        LogbackLogEntry entry = parser.parseLogLine(logline);

		String expectedSession = "1";
		assertEquals("2019-02-07 21:32:42,843", entry.getField("d"));
		assertEquals("myCache5", entry.getField("cache"));
		assertEquals("MISS", entry.getField("event"));
		assertEquals(expectedSession, entry.getField("Session"));
		assertEquals("67605", entry.getMessage());

	}


	/**
	 * This throws an exception due to a the customer field in the comma separated MDC list contains
	 * a comma itself. That comma should better be escaped (submit a fix for logback?!).
	 */
	@Test(expected = LogRaterException.class)
	public void testParseWithSpacesInMdcDump2() {
		String logbackpattern = "%d;%X;%msg%n";
        LogbackParser<LogbackLogEntry> parser = LogbackParser.createLogbackParser(logbackpattern);
		String logline = "2019-01-19 11:56:21,044;Session=session567, cache=FooService, event=MISS, myID=x, service=WH SXY, AppName=stp-service, customer=cust1234,xyz;121856";
        LogbackLogEntry entry = parser.parseLogLine(logline);
        
		assertEquals("2019-01-19 11:56:21,044", entry.getField("d"));
		assertEquals("FooService", entry.getField("cache"));
		assertEquals("MISS", entry.getField("event"));
		assertEquals("WH SXY", entry.getField("service"));
		assertEquals("session567", entry.getField("Session"));
		assertEquals("121856", entry.getMessage());
	}

	@Test
	public void testParseWithEmptyMdcDump() {
		String logbackpattern = "%d;%X;%msg%n";
        LogbackParser<LogbackLogEntry> parser = LogbackParser.createLogbackParser(logbackpattern);
		String logline = "2019-02-07 21:32:42,843;;67605";
        LogbackLogEntry entry = parser.parseLogLine(logline);

		assertEquals("2019-02-07 21:32:42,843", entry.getField("d"));
		assertEquals("67605", entry.getMessage());
	}

	@Test
	public void testApplicationLogNullPointerWhileParsing() {
		String pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [reqId=%X{reqId}] [Session=%X{Session}] %logger{36} - %msg%n";
        LogbackParser<LogbackLogEntry> parser = LogbackParser.createLogbackParser(pattern);
        String logline = "2019-03-21 21:00:00.031 [WebContainer : 3691] INFO  [reqId=xyz] [Session=1969] s.n.VlaHelper - Something is not complete.";
		String logline2 = "2019-03-21 21:00:00.039 [WebContainer : 3691] INFO  [reqId=xyz] [Session=1969] s.n.VlaHelper - Set vlaId 6 to the session 1969";
		LogbackLogEntry entry = parser.parseLogLine(logline);
		LogbackLogEntry entry2 = parser.parseLogLine(logline2);

		assertEquals("2019-03-21 21:00:00.031", entry.getField("d"));
		assertNotNull(entry.getMessage());
		assertNotNull(entry.getLogLevel());
		assertNotNull(entry.getThreadName());
		// there is no classname... only a logger
		assertNull(entry.getClassName());
		assertNull(entry.getMarker());
		assertNotNull(entry.getNonLogLines());
		String[] fields = entry.getFields();
		for (String field: fields) {
			assertNotNull(field + " should not be null", entry.getField(field));
		}

		assertEquals("2019-03-21 21:00:00.039", entry2.getField("d"));
	}

	@Test
	public void testApplicationLogLineMessageWithLinebreak() {
		String pattern = "%d;%t;%p;%marker;%X{x-s};%X{Le};%X{Type};%X{Id};%X{boo};%X{service};%X{fName};%X{fType};%X{myID};%c;%m;%myMDC{x-s,Le,Ty,Id,boo}%n%xEx{full}";
		String line = "2019-04-06 20:56:57,844;myThread104;ERROR;;idABC;l5;BOO;id123;id123;;getbatch;service;;nl.stokpop.ExceptionResponse;toResponse: return 500, e=technical error\n" +
				"causeMessage: execution failed\n";

		LogbackParser<LogbackLogEntry> parser = LogbackParser.createLogbackParser(pattern);

        LogbackLogEntry entry = parser.parseLogLine(line);

		assertNotNull(entry.getMessage());

	}
}