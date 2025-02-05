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
package nl.stokpop.lograter.gc;

import nl.stokpop.lograter.util.time.DateUtils;

public class GcLogEntry {
	
	private int id;
	private int globalId;
	private GcType type;
    private long timestampInMillis;
	private long tenuredFreebytes;
	private long tenuredTotalbytes;
	private long nurseryFreebytes;
	private long nurseryTotalbytes;
    private double exclusiveDurationMs;
	private GcReason gcReason;
    private String sysGcReason;

	private GcLogEntry(int id, GcReason gcReason, String sysGcReason, double exclusiveDurationMs, long nurseryTotalbytes, long nurseryFreebytes, long tenuredTotalbytes, long tenuredFreebytes, long timestampInMillis, int globalId, GcType type) {
		this.id = id;
		this.gcReason = gcReason;
		this.exclusiveDurationMs = exclusiveDurationMs;
		this.nurseryTotalbytes = nurseryTotalbytes;
		this.nurseryFreebytes = nurseryFreebytes;
		this.tenuredTotalbytes = tenuredTotalbytes;
		this.tenuredFreebytes = tenuredFreebytes;
		this.timestampInMillis = timestampInMillis;
		this.globalId = globalId;
		this.type = type;
        this.sysGcReason = sysGcReason;
	}

	public int getId() {
		return id;
	}

	public long getTenuredFreebytes() {
		return tenuredFreebytes;
	}

	public long getTenuredTotalbytes() {
		return tenuredTotalbytes;
	}

	public long getNurseryFreebytes() {
		return nurseryFreebytes;
	}

	public long getNurseryTotalbytes() {
		return nurseryTotalbytes;
	}

    public long getTimestamp() {
        return timestampInMillis;
    }

	public long getTotalUsedBytes() {
		return nurseryTotalbytes - nurseryFreebytes + tenuredTotalbytes - tenuredFreebytes;
	}

    public long getTenuredUsedBytes() {
        return tenuredTotalbytes - tenuredFreebytes;
    }

	public GcType getType() {
		return type;
	}

	public int getGlobalId() {
		return globalId;
	}

    public double getExclusiveDurationMs() {
        return exclusiveDurationMs;
    }

	public GcReason getGcReason() {
		return gcReason;
	}

    public String getSysGcReason() {
        return sysGcReason;
    }

    @Override
    public String toString() {
        return "GcLogEntry{" +
                "id=" + id +
                ", globalId=" + globalId +
                ", type=" + type +
                ", timestamp=" + DateUtils.formatToStandardDateTimeString(timestampInMillis) +
                ", tenuredFreebytes=" + tenuredFreebytes +
                ", tenuredTotalbytes=" + tenuredTotalbytes +
                ", nurseryFreebytes=" + nurseryFreebytes +
                ", nurseryTotalbytes=" + nurseryTotalbytes +
                ", exclusiveDurationMs=" + exclusiveDurationMs +
                ", gcReason=" + gcReason +
                ", sysGcReason='" + sysGcReason + '\'' +
                '}';
    }

	@Override
	public boolean equals(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}

    public static class GcLogEntryBuilder {

        private int id;
        private GcReason gcReason;
        private double exclusiveDurationMs;
        private long nurseryTotalbytes;
        private long nurseryFreebytes;
        private long tenuredTotalbytes;
        private long tenuredFreebytes;
        private long timestampInMillis;
        private int globalId;
        private GcType type;
        private String sysGcReason;
        public GcLogEntryBuilder setId(int id) {
            this.id = id;
            return this;
        }

        public GcLogEntryBuilder setGcReason(GcReason gcReason) {
            this.gcReason = gcReason;
            return this;
        }

        public String getSysGcReason() {
            return sysGcReason;
        }

        public GcLogEntryBuilder setExclusiveDurationMs(double exclusiveDurationMs) {
            this.exclusiveDurationMs = exclusiveDurationMs;
            return this;
        }

        public GcLogEntryBuilder setNurseryTotalbytes(long nurseryTotalbytes) {
            this.nurseryTotalbytes = nurseryTotalbytes;
            return this;
        }

        public GcLogEntryBuilder setNurseryFreebytes(long nurseryFreebytes) {
            this.nurseryFreebytes = nurseryFreebytes;
            return this;
        }

        public GcLogEntryBuilder setTenuredTotalbytes(long tenuredTotalbytes) {
            this.tenuredTotalbytes = tenuredTotalbytes;
            return this;
        }

        public GcLogEntryBuilder setTenuredFreebytes(long tenuredFreebytes) {
            this.tenuredFreebytes = tenuredFreebytes;
            return this;
        }

        public GcLogEntryBuilder setTimestamp(long timestampInMillis) {
            this.timestampInMillis = timestampInMillis;
            return this;
        }

        public GcLogEntryBuilder setGlobalId(int globalId) {
            this.globalId = globalId;
            return this;
        }

        public GcLogEntryBuilder setGcType(GcType type) {
            this.type = type;
            return this;
        }

        public GcLogEntryBuilder setSysGcReason(String sysGcReason) {
            this.sysGcReason = sysGcReason;
            return this;
        }

        public GcLogEntry createGcLogEntry() {
            return new GcLogEntry(id, gcReason, sysGcReason, exclusiveDurationMs, nurseryTotalbytes, nurseryFreebytes, tenuredTotalbytes, tenuredFreebytes, timestampInMillis, globalId, type);
        }

    }
}
