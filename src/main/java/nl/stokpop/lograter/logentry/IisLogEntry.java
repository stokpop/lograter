/*
 * Copyright (C) 2022 Peter Paul Bakker, Stokpop
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
package nl.stokpop.lograter.logentry;

import nl.stokpop.lograter.counter.HttpMethod;
import nl.stokpop.lograter.parser.line.LogEntryMapper;
import nl.stokpop.lograter.parser.line.LogbackElement;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IisLogEntry extends AccessLogEntry {
	
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormat.forPattern(DATE_FORMAT);
	
	private String date;
	private String time;
	private String c_ip;
	private String cs_username;
	private String s_sitename;
	private String s_ip;
	private int s_port;
	private int sc_bytes;
	private int cs_bytes;
	private String cs_version;
	private String cs_host;
	private String cs_cookie;

	private void updateDatetime() {
		if (time != null && date != null) {
			setTimestamp(DATE_TIME_FORMAT.parseMillis(date + " " + time));
		}
	}	
	
	public static Map<String, LogEntryMapper<IisLogEntry>> initializeMappers(List<LogbackElement> elements) {

        Map<String, LogEntryMapper<IisLogEntry>> mappers = new HashMap<>();

        mappers.put("date", (value, variable, e) -> {
                    e.date = value;
                    e.updateDatetime();
                }
        );

        mappers.put("time", (value, variable, e) -> {
                    e.time = value;
                    e.updateDatetime();
                }
        );

        mappers.put("c-ip", (value, variable, e) -> e.c_ip = value );

        mappers.put("cs-username", (value, variable, e) -> e.cs_username = value);

        mappers.put("s-sitename", (value, variable, e) -> e.s_sitename = value);

        mappers.put("s-ip", (value, variable, e) -> e.s_ip = value);

        mappers.put("s-port", (value, variable, e) -> e.s_port = Integer.parseInt(value));

        mappers.put("cs-method", (value, variable, e) -> e.setHttpMethod(HttpMethod.valueOf(value)));

        mappers.put("cs-uri-stem", (value, variable, e) -> e.setUrl(value));

        mappers.put("sc-status", (value, variable, e) -> e.setHttpStatus(Integer.parseInt(value)));

        mappers.put("sc-bytes", (value, variable, e) -> e.sc_bytes = Integer.parseInt(value));

        mappers.put("cs-bytes", (value, variable, e) -> e.cs_bytes = Integer.parseInt(value));

        mappers.put("time-taken", (value, variable, e) -> e.setDurationInMillis(Integer.parseInt(value)));

        mappers.put("cs-version", (value, variable, e) -> e.cs_version = value);

        mappers.put("cs-host", (value, variable, e) -> e.cs_host = value);

        mappers.put("cs(User-Agent)", (value, variable, e) -> e.setUserAgent(value));

        mappers.put("cs(Cookie)", (value, variable, e) -> e.cs_cookie = value);

        mappers.put("cs(Referer)", (value, variable, e) -> e.setReferrer(value));

        return mappers;
		}

    @Override
    public String toString() {
        return "IisLogEntry{" +
                "c_ip='" + c_ip + '\'' +
                ", cs_bytes=" + cs_bytes +
                ", cs_cookie='" + cs_cookie + '\'' +
                ", cs_host='" + cs_host + '\'' +
                ", cs_username='" + cs_username + '\'' +
                ", cs_version='" + cs_version + '\'' +
                ", date='" + date + '\'' +
                ", s_ip='" + s_ip + '\'' +
                ", s_port=" + s_port +
                ", s_sitename='" + s_sitename + '\'' +
                ", sc_bytes=" + sc_bytes +
                ", time='" + time + '\'' +
                "} " + super.toString();
    }
}
