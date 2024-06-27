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
package nl.stokpop.lograter.processor.jmeter;

import nl.stokpop.lograter.logentry.LogEntry;

public class JMeterLogEntry extends LogEntry {

    private int durationInMillis;
    private int code;
    private boolean success;
    private String url;
    private JMeterLogLineType logLineType;

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(final boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(final int code) {
        this.code = code;
        this.addField(HTTP_STATUS, String.valueOf(code));
    }

    public int getDurationInMillis() {
        return durationInMillis;
    }

    public void setDurationInMillis(final int durationInMillis) {
        this.durationInMillis = durationInMillis;
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        throw new UnsupportedOperationException();
    }

    public JMeterLogLineType getLogLineType() {
        return logLineType;
    }

    public void setLogLineType(JMeterLogLineType logLineType) {
        this.logLineType = logLineType;
    }
}
