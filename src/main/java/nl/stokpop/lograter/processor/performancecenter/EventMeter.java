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
package nl.stokpop.lograter.processor.performancecenter;

import net.jcip.annotations.Immutable;

/**
 * Represents an EventMeter record for the performance center access or sqlite database.
 */
@Immutable
/* default */ class EventMeter {

	protected static final String EVENT_ID = "Event ID";
	protected static final String END_TIME = "End Time";
	protected static final String VALUE = "Value";
	protected static final String COUNT = "Acount";
	protected static final String VALUE_MAX = "Amaximum";
	protected static final String VALUE_MIN = "Aminimum";
	protected static final String SUM_SQ = "AsumSq";
	protected static final String STATUS_1 = "Status1";
	protected static final String THINK_TIME = "Think Time";

	protected static final String EVENT_METER_TABLE = "Event_meter";

	private final int eventID;
	private final double endTime;
	/** assumption: value is the average of the 'count' values */
	private final int count;
	private final double value;
	private final double valueMin;
	private final double valueMax;
	private final double thinkTime;
	private final double sumSquares;
	private final int status;

	public EventMeter(int eventID, double endTime, double value, double valueMin, double valueMax, double sumSquares, double thinkTime, int count, int status) {
		this.eventID = eventID;
		this.endTime = endTime;
		this.value = value;
		this.valueMin = valueMin;
		this.valueMax = valueMax;
        this.sumSquares = sumSquares;
        this.thinkTime = thinkTime;
        this.count = count;
		this.status = status;
	}

	public int getEventID() {
		return eventID;
	}

	public double getEndTime() {
		return endTime;
	}

	public double getValue() {
		return value;
	}

	public int getCount() {
		return count;
	}

	public int getStatus() {
		return status;
	}

	public boolean isSuccess() {
		return status == 1;
	}

	public double getValueMax() {
		return valueMax;
	}

	public double getValueMin() {
		return valueMin;
	}

	public double getThinkTime() {
		return thinkTime;
	}

	public double getSumSquares() {
		return sumSquares;
	}

    @Override
    public String toString() {
        return "EventMeter{" +
                "eventID=" + eventID +
                ", endTime=" + endTime +
                ", count=" + count +
                ", value(avg)=" + value +
                ", valueMin=" + valueMin +
                ", valueMax=" + valueMax +
                ", sumSquares=" + sumSquares +
                ", thinkTime=" + thinkTime +
                ", status=" + status +
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
}
