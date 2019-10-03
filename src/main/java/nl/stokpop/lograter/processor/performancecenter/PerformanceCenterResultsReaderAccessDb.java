/*
 * Copyright (C) 2019 Peter Paul Bakker, Stokpop Software Solutions
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

import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.counter.CounterStorageType;
import nl.stokpop.lograter.processor.performancecenter.PerformanceCenterAggregationGranularity.AggregationGranularityType;
import nl.stokpop.lograter.store.RequestCounterStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads data from Performance Center Results.mdb: the access database.
 */
public class PerformanceCenterResultsReaderAccessDb extends AbstractPerformanceCenterResultsReader {

	private final static Logger log = LoggerFactory.getLogger(PerformanceCenterResultsReaderAccessDb.class);

	public PerformanceCenterResultsReaderAccessDb(File resultsDatabaseFile) {
		super(resultsDatabaseFile);
	}

	public PerformanceCenterResultsReaderAccessDb(File resultsDatabaseFile, Integer pcAggregationPeriodSec) {
		super(resultsDatabaseFile, pcAggregationPeriodSec);
	}

	@Override
	public PerformanceCenterResultsData readResultsData() {
		try {
			return readResultsData(getResultsDatabaseFile());
		} catch (IOException e) {
			throw new LogRaterException("Unable to process Access database file " + getResultsDatabaseFile(), e);
		}
	}

	private PerformanceCenterResultsData readResultsData(File resultsDatabaseFile) throws IOException {

        RequestCounterStoreFactory factory = new RequestCounterStoreFactory(CounterStorageType.Memory);

        Database database = DatabaseBuilder.open(resultsDatabaseFile);
        PerformanceCenterAggregationGranularity granularity = createPerformanceCenterAggregationGranularity(database);
        PerformanceCenterResultsData data = new PerformanceCenterResultsData(factory, granularity);

        long testStartTimeSecEpoch = getTestStartTimeSecEpoch(database);

        Map<Integer, PerformanceCenterEvent> eventMap = createEventMap(database);

        Table eventMeterTable = database.getTable(EventMeter.EVENT_METER_TABLE);
        for (Row row : eventMeterTable) {
            EventMeter eventMeter = createEventMeter(row);
	        addEventsToResultsData(eventMeter, data, eventMap, testStartTimeSecEpoch, granularity.getGranularityMillis());
        }
        database.close();
        log.info("Finished parsing performance center results: {}", data);
        return data;
    }

    private EventMeter createEventMeter(Row row) {
        Integer eventID = row.getInt(EventMeter.EVENT_ID);
        Double endTime = row.getDouble(EventMeter.END_TIME);
        Double value = row.getDouble(EventMeter.VALUE);
        // COUNT seems defined as Double in AccessDb
        Double count = row.getDouble(EventMeter.COUNT);
        int countInt = count == null ? 0 : count.intValue();
        short status = row.getShort(EventMeter.STATUS_1);
        Double valueMin = row.getDouble(EventMeter.VALUE_MIN);
        Double valueMax = row.getDouble(EventMeter.VALUE_MAX);
        Double thinkTime = row.getDouble(EventMeter.THINK_TIME);
        Double sumSquares = row.getDouble(EventMeter.SUM_SQ);

        // note there is unboxing done for most parameters, watch for NullPointerExceptions
        return new EventMeter(eventID, endTime, value, valueMin, valueMax, sumSquares, thinkTime, countInt, status);
    }

    private PerformanceCenterAggregationGranularity createPerformanceCenterAggregationGranularity(Database database) throws IOException {
        Integer pcAggregationPeriodSec = getPcAggregationPeriodSec();

        AggregationGranularityType granularityType = isAggregationPeriodValid(pcAggregationPeriodSec)
                        ? AggregationGranularityType.LRA_FILE_EXACT
                        : AggregationGranularityType.DATABASE_GUESS;
        long granularityInMillis = isAggregationPeriodValid(pcAggregationPeriodSec)
                ? pcAggregationPeriodSec * 1000
                : determineGranularityInMillis(database);

        return new PerformanceCenterAggregationGranularity(granularityInMillis, granularityType);
    }

    private Map<Integer, PerformanceCenterEvent> createEventMap(Database database) throws IOException {
        Table eventMapTable = database.getTable("Event_map");
        Map<Integer, PerformanceCenterEvent> eventMap = new HashMap<>();
        for (Row row : eventMapTable) {
            Integer eventID = row.getInt("Event ID");
            String eventType = row.getString("Event Type");
            String eventName = row.getString("Event Name");
            PerformanceCenterEvent event = new PerformanceCenterEvent(eventID, eventType, eventName);
            eventMap.put(eventID, event);
            log.debug("Added event: {}", event);
        }
        return eventMap;
    }

    private long getTestStartTimeSecEpoch(Database database) throws IOException {
        long testStartTimeSecEpoch = -1;
	    long timeZoneOffset = 0;
        Table resultTable = database.getTable("Result");

        for (Row row : resultTable) {
            testStartTimeSecEpoch = row.getInt("Start Time");
	        timeZoneOffset = row.getInt("Time Zone");
            log.info("Column 'Start Time' in table 'Result' has value: {}", testStartTimeSecEpoch);
        }

        if (testStartTimeSecEpoch == -1) {
            throw new LogRaterException("Start time was not found in Result table in database " + database);
        }
        return PerformanceCenterCalculator.calculateLocalStartTimeSecEpoch(testStartTimeSecEpoch, timeZoneOffset);
    }

	private long determineGranularityInMillis(Database database) throws IOException {

	    // this seems pretty broken: end times seem to vary a lot, records are not completely in order...
        // best to filter all rows for one particular eventId, order all endTimes, and determine the average, or most
        // common, lowest diff between endTimes which are not 0...

		// determine the granularity by looking at two records somewhere in the future (e.g. with enough load)
		Table eventMeter = database.getTable(EventMeter.EVENT_METER_TABLE);

		// fast forward into the dataset so (more) load is present

        // the getRowCount() call is broken: gets it from the table metadata, which can
        // be incorrect (e.g. 0)
		// int rows = eventMeter.getRowCount();
        int rows = determineRowsInTableWorkaround(eventMeter);

		if (rows < 2) {
			throw new LogRaterException("Not enough rows in the Event_meter table to calculate granularity. There are " + rows + " rows.");
		}
		int forwardRowsCount = rows > ROW_FETCH_COUNT_FOR_GRANULARITY_DETERMINATION ? ROW_FETCH_COUNT_FOR_GRANULARITY_DETERMINATION : (rows - 1);
		Row row;
		for (int i = 0; i < forwardRowsCount; i++) {
			eventMeter.getNextRow();
		}
		row = eventMeter.getNextRow();

		Integer firstEventID = row.getInt(EventMeter.EVENT_ID);
		Double endTimeFirst = row.getDouble(EventMeter.END_TIME);

		// the second measurement should be of same event_id
        double granularitySec;
        Integer secondEventID;
		do {
            row = eventMeter.getNextRow();
            secondEventID = row.getInt(EventMeter.EVENT_ID);
            Double endTimeSecond = row.getDouble(EventMeter.END_TIME);
            granularitySec = PerformanceCenterCalculator.calculateGranularitySec(endTimeFirst, endTimeSecond);
        } while (firstEventID.equals(secondEventID) || granularitySec < 0.001d);

		log.info("Found PROBABLY FAULTY ESTIMATION of granularity in access database: [{}] seconds.", granularitySec);
		return (long) (granularitySec * 1000.0);

	}

    private int determineRowsInTableWorkaround(Table eventMeter) throws IOException {
        Cursor cursor = eventMeter.getDefaultCursor();
        cursor.reset();
        int rows = 0;
        while(cursor.moveToNextRow()) {
            rows++;
        }
        cursor.reset();
        return rows;
    }

}
