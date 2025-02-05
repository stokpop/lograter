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
package nl.stokpop.lograter.processor.performancecenter;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.counter.CounterStorageType;
import nl.stokpop.lograter.store.RequestCounterStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import static nl.stokpop.lograter.processor.performancecenter.PerformanceCenterAggregationGranularity.AggregationGranularityType;

/**
 * Reads data from Performance Center Results.db: the sqlite database.
 */
public class PerformanceCenterResultsReaderSqlite extends AbstractPerformanceCenterResultsReader {

	private static final Logger log = LoggerFactory.getLogger(PerformanceCenterResultsReaderSqlite.class);

	public PerformanceCenterResultsReaderSqlite(File resultsDatabaseFile) {
		super(resultsDatabaseFile);
	}

	public PerformanceCenterResultsReaderSqlite(File resultsDatabaseFile, Integer pcAggregationPeriodInSeconds) {
		super(resultsDatabaseFile, pcAggregationPeriodInSeconds);
	}

	protected Connection getDatabaseConnection(File sqliteDatabaseFile) {
        final String databaseName = "jdbc:sqlite:" + sqliteDatabaseFile.getAbsolutePath();
        try {
            SQLiteConfig config = new SQLiteConfig();
            config.setReadOnly(true);
            config.setSharedCache(true);
            Connection con = DriverManager.getConnection(databaseName, config.toProperties());
            con.setAutoCommit(false);
            con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            return con;
        } catch (SQLException e) {
            throw new LogRaterException("Cannot get database connection to: " + databaseName, e);
        }
    }

	public Map<Integer, PerformanceCenterEvent> createEventMap(Connection connection) throws SQLException {

		try (
			PreparedStatement preparedStatement = connection.prepareStatement("select [Event ID], [Event Type], [Event Name] from Event_map");
			ResultSet resultSet = preparedStatement.executeQuery()
		) {
			Map<Integer, PerformanceCenterEvent> eventMap = new HashMap<>();
			while (resultSet.next()) {
				Integer eventID = resultSet.getInt("Event ID");
				String eventType = resultSet.getString("Event Type");
				String eventName = resultSet.getString("Event Name");
				PerformanceCenterEvent event = new PerformanceCenterEvent(eventID, eventType, eventName);
				eventMap.put(eventID, event);
				log.debug("Added event: {}", event);
			}
			return eventMap;
		}
	}

	@Override
	public PerformanceCenterResultsData readResultsData(int maxUniqueCounters) {
		File resultsDatabaseFile = getResultsDatabaseFile();
		if (!resultsDatabaseFile.exists()) {
            throw new LogRaterException("Sqlite database file not found [" + resultsDatabaseFile + "]");
        }
		try {
			return readResultsData(resultsDatabaseFile, maxUniqueCounters);
		} catch (Exception e) {
			throw new LogRaterException("Unable to process sqlite database file [" + resultsDatabaseFile + "]", e);
		}
	}

	public PerformanceCenterResultsData readResultsData(File resultsDatabaseFile, int maxUniqueCounters) throws SQLException {

        RequestCounterStoreFactory factory = new RequestCounterStoreFactory(CounterStorageType.Memory);

        Connection connection = getDatabaseConnection(resultsDatabaseFile);
		PerformanceCenterAggregationGranularity granularity = createPerformanceCenterAggregationGranularity(connection);

		PerformanceCenterResultsData data = new PerformanceCenterResultsData(factory, granularity, maxUniqueCounters);

        long testStartTimeInSecondsEpoch =
				PerformanceCenterCalculator.calculateStartTimeSecEpoch(resultsDatabaseFile);

        Map<Integer, PerformanceCenterEvent> eventMap = createEventMap(connection);

        try (
        	PreparedStatement preparedStatement = connection.prepareStatement("select [Event ID], [End Time], [Value], [Acount], [Amaximum], [Aminimum], [AsumSq], [Think Time], [Status1] from Event_meter order by [End Time]");
        	ResultSet resultSet = preparedStatement.executeQuery()
		) {
			while (resultSet.next()) {
				// note there is unboxing done for most parameters, watch for NullPointerExceptions
				int eventID = resultSet.getInt(EventMeter.EVENT_ID);
				double endTime = resultSet.getDouble(EventMeter.END_TIME);
				double value = resultSet.getDouble(EventMeter.VALUE);
				int count = (int) resultSet.getDouble(EventMeter.COUNT);
				double maxValue = resultSet.getDouble(EventMeter.VALUE_MAX);
				double minValue = resultSet.getDouble(EventMeter.VALUE_MIN);
				double thinkTime = resultSet.getDouble(EventMeter.THINK_TIME);
				double sumSq = resultSet.getDouble(EventMeter.SUM_SQ);
				int status = resultSet.getInt(EventMeter.STATUS_1);

				EventMeter eventMeter = new EventMeter(eventID, endTime, value, minValue, maxValue, sumSq, thinkTime, count, status);
				addEventsToResultsData(eventMeter, data, eventMap, testStartTimeInSecondsEpoch, granularity.getGranularityMillis());
			}

		}
        log.info("Finished parsing performance center results: {}", data);
        return data;
    }

	private long determineGranularityInMillis(Connection connection) throws SQLException {
		// determine the granularity by looking at two records somewhere in the future (e.g. with enough load)
		final String sql = String.format(
				"select distinct [End Time] from Event_meter order by [End Time] limit 2 offset %d",
				ROW_FETCH_COUNT_FOR_GRANULARITY_DETERMINATION);
		try (
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			ResultSet resultSet = preparedStatement.executeQuery()
		) {
			double granularityInSeconds = granularityInSeconds(sql, resultSet);

			log.info("Found PROBABLY FAULTY ESTIMATION of granularity in access database: [{}] seconds.", granularityInSeconds);

			return (long) (granularityInSeconds * 1000.0);
		}

	}

	private double granularityInSeconds(
			final String sql,
			final ResultSet resultSet) throws SQLException, LogRaterException {

		boolean hasNextEndTime = resultSet.next();
		if (!hasNextEndTime) {
			throw new LogRaterException(String.format("No next record found in query result of [%s]", sql));
		}
		Double endTimeFirst = resultSet.getDouble("End Time");
		hasNextEndTime = resultSet.next();
		if (!hasNextEndTime) throw new LogRaterException(String.format("No next record found in query result of [%s]", sql));
		Double endTimeSecond = resultSet.getDouble("End Time");

		return endTimeSecond - endTimeFirst;
	}

	private PerformanceCenterAggregationGranularity createPerformanceCenterAggregationGranularity(Connection connection) throws SQLException {
		Integer pcAggregationPeriodSec = getPcAggregationPeriodSec();

		AggregationGranularityType granularityType = isAggregationPeriodValid(pcAggregationPeriodSec)
				? AggregationGranularityType.LRA_FILE_EXACT
                : AggregationGranularityType.DATABASE_GUESS;
		long granularityInMillis = isAggregationPeriodValid(pcAggregationPeriodSec)
				? pcAggregationPeriodSec * 1000
				: determineGranularityInMillis(connection);

		return new PerformanceCenterAggregationGranularity(granularityInMillis, granularityType);
	}
}
