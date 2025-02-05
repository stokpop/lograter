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
package nl.stokpop.lograter.logentry;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.command.FailureFieldType;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LatencyLogEntryTest {

    private static final Pattern ERROR_PATTERN = Pattern.compile("error", Pattern.CASE_INSENSITIVE);

    @Test
    public void successFactorInstanceBool() {
        LogEntrySuccessFactor<LatencyLogEntry> successFactor =
            LatencyLogEntry.successFactorInstance("failure", FailureFieldType.bool, null);

        LatencyLogEntry latencyLogEntrySuccess = new LatencyLogEntry(successFactor);
        latencyLogEntrySuccess.addCustomField("failure", "false");
        assertTrue(latencyLogEntrySuccess.isSuccess());

        LatencyLogEntry latencyLogEntryFailure = new LatencyLogEntry(successFactor);
        latencyLogEntryFailure.addCustomField("failure", "true");
        assertFalse(latencyLogEntryFailure.isSuccess());
    }

    @Test
    public void successFactorInstanceHttp() {
        LogEntrySuccessFactor<LatencyLogEntry> successFactor =
            LatencyLogEntry.successFactorInstance("http-status-code", FailureFieldType.http, null);

        LatencyLogEntry latencyLogEntrySuccess = new LatencyLogEntry(successFactor);
        latencyLogEntrySuccess.addCustomField("http-status-code", "200");
        assertTrue(latencyLogEntrySuccess.isSuccess());

        LatencyLogEntry latencyLogEntryFailure = new LatencyLogEntry(successFactor);
        latencyLogEntryFailure.addCustomField("http-status-code", "500");
        assertFalse(latencyLogEntryFailure.isSuccess());
    }

    @Test
    public void successFactorInstanceRegexp() {
        LogEntrySuccessFactor<LatencyLogEntry> successFactor =
            LatencyLogEntry.successFactorInstance("message", FailureFieldType.regexp, ERROR_PATTERN);

        LatencyLogEntry latencyLogEntrySuccess = new LatencyLogEntry(successFactor);
        latencyLogEntrySuccess.addCustomField("message", "all is fine");
        assertTrue(latencyLogEntrySuccess.isSuccess());

        LatencyLogEntry latencyLogEntryFailure = new LatencyLogEntry(successFactor);
        latencyLogEntryFailure.addCustomField("message", "this is an Error!");
        assertFalse(latencyLogEntryFailure.isSuccess());
    }

    @Test
    public void successFactorInstanceBoolNullField() {
        LogEntrySuccessFactor<LatencyLogEntry> successFactor =
            LatencyLogEntry.successFactorInstance("xyz", FailureFieldType.bool, null);

        LatencyLogEntry latencyLogEntrySuccess = new LatencyLogEntry(successFactor);
        latencyLogEntrySuccess.addCustomField("failure", "false");
        assertTrue(latencyLogEntrySuccess.isSuccess());
    }

    @Test
    public void successFactorInstanceHttpNullField() {
        LogEntrySuccessFactor<LatencyLogEntry> successFactor =
            LatencyLogEntry.successFactorInstance("xyz", FailureFieldType.http, null);

        LatencyLogEntry latencyLogEntrySuccess = new LatencyLogEntry(successFactor);
        latencyLogEntrySuccess.addCustomField("http-status-code", "304");
        assertTrue(latencyLogEntrySuccess.isSuccess());
    }

    @Test
    public void successFactorInstanceHttpNotANumberField() {
        LogEntrySuccessFactor<LatencyLogEntry> successFactor =
            LatencyLogEntry.successFactorInstance("http-status-code", FailureFieldType.http, null);

        LatencyLogEntry latencyLogEntrySuccess = new LatencyLogEntry(successFactor);
        latencyLogEntrySuccess.addCustomField("http-status-code", "bar");
        assertTrue(latencyLogEntrySuccess.isSuccess());
    }

    @Test(expected = LogRaterException.class)
    public void successFactorInstanceRegexpNullRegexp() {
        LogEntrySuccessFactor<LatencyLogEntry> successFactor =
            LatencyLogEntry.successFactorInstance("xyz", FailureFieldType.regexp, null);

        LatencyLogEntry latencyLogEntrySuccess = new LatencyLogEntry(successFactor);
        latencyLogEntrySuccess.addCustomField("message", "all is fine");
        assertTrue(latencyLogEntrySuccess.isSuccess());

    }

    @Test
    public void successFactorInstanceRegexpNullField() {
        LogEntrySuccessFactor<LatencyLogEntry> successFactor =
            LatencyLogEntry.successFactorInstance("xyz", FailureFieldType.regexp, ERROR_PATTERN);

        LatencyLogEntry latencyLogEntrySuccess = new LatencyLogEntry(successFactor);
        latencyLogEntrySuccess.addCustomField("message", "all is fine");
        assertTrue(latencyLogEntrySuccess.isSuccess());
    }
}