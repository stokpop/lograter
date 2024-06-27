/*
 * Copyright (C) 2024 Peter Paul Bakker, Stokpop
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

import nl.stokpop.lograter.command.LatencyUnit;
import nl.stokpop.lograter.logentry.LatencyLogEntry;
import nl.stokpop.lograter.processor.latency.LatencyLogConfig;
import nl.stokpop.lograter.processor.latency.LatencyLogReader;
import nl.stokpop.lograter.util.linemapper.LineMapperSection;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class LatencyParserTest {
    
    @Test
    public void testParse() {
        String pattern = "%X{one} - %d{[yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSZ]} \"%X{http-method} %X{operation} %X{http-version}\" %X{http-code} %X{http-something} %X{http-bytes} \"%X{unknown1}\" \"%X{http-referer}\" \"%X{remote-ip}:%X{remote-port}\" \"%X{local-ip}:%X{local-port}\" x_forwarded_for:\"%X{x_forwarded_for}\" x_forwarded_proto:\"%X{x_forwarded_proto}\" vcap_request_id:\"%X{vcap_request_id}\" response_time:%X{response_time} gorouter_time:%X{gorouter_time} app_id:\"%X{app_id}\" app_index:\"%X{app_index}\" x_cf_routererror:\"%X{x_cf_routererror}\" x_client_ip:\"%X{x_client_ip}\" x_session_id:\"%X{x_session_id}\" x_b3_traceid:\"%X{x_b3_traceid}\" x_b3_spanid:\"%X{x_b3_spanid}\" x_b3_parentspanid:\"%X{x_b3_parentspanid}\" b3:\"%X{b3}\"%X{two}";

        String logline = "xxx.apps.yyy.nl - [2021-06-23T15:07:22.980439601Z] \"GET /whatever/nl/ HTTP/1.0\" 304 0 0 \"-\" \"Mozilla/5.0 (Linux)\" \"1.2.3.4:3168\" \"2.2.2.2:61070\" x_forwarded_for:\"2.3.4.5\" x_forwarded_proto:\"https\" vcap_request_id:\"76bd0e9c-0eee-404f-4877-8559\" response_time:0.018021 gorouter_time:0.000346 app_id:\"2b6e9a9e-340a-4469-bf96-ed5c\" app_index:\"6\" x_cf_routererror:\"-\" x_client_ip:\"5.5.5.2\" x_session_id:\"16Iqz82\" x_b3_traceid:\"ff1f\" x_b3_spanid:\"ff1f\" x_b3_parentspanid:\"-\" b3:\"ff1f-ff1f\"";

        LatencyLogConfig config = new LatencyLogConfig();
        config.setCounterFields("operation");
        config.setLatencyField("response_time");
        config.setLatencyUnit(LatencyUnit.seconds);
        LogbackParser<LatencyLogEntry> latencyParser = LatencyLogReader.createLatencyLogEntryLogbackParser(pattern, config);

		LatencyLogEntry entry = latencyParser.parseLogLine(logline);

        assertEquals(logline, entry.getLogline());
        assertEquals("/whatever/nl/", entry.getField("operation"));
        assertEquals("304", entry.getField("http-code"));
        assertEquals(18, entry.getDurationInMillis());
    }

    @Test
    public void testParseLatency() {

        String pattern = "\"%d{MMM dd, yyyy @ HH:mm:ss.SSS}\",\"%X{duration}\",%X{status},%{method},\"%X{path}\",%{size}";

        String logline = "\"Sep 30, 2022 @ 17:00:57.000\",\"654,275,032\",200,POST,\"/api/xyz-abc-xup/bar/\",36";

        LatencyLogConfig config = new LatencyLogConfig();

        LineMapperSection lineMapperSection = new LineMapperSection("test");
        lineMapperSection.addMapperRule("/api/(.*)/bar/", "api bar");
        List<LineMapperSection> mappers = List.of(lineMapperSection);
        config.setLineMappers(mappers);
        config.setCounterFields("path");
        config.setLatencyField("duration");
        config.setLatencyUnit(LatencyUnit.nanoseconds);
        LogbackParser<LatencyLogEntry> latencyParser = LatencyLogReader.createLatencyLogEntryLogbackParser(pattern, config);

		LatencyLogEntry entry = latencyParser.parseLogLine(logline);

        assertEquals(logline, entry.getLogline());
        assertEquals("/api/xyz-abc-xup/bar/", entry.getField("path"));
        assertEquals("200", entry.getField("status"));
        assertEquals(654, entry.getDurationInMillis());
    }
}