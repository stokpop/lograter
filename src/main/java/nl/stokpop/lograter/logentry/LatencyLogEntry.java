package nl.stokpop.lograter.logentry;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.command.LatencyUnit;
import nl.stokpop.lograter.parser.line.LogEntryMapper;
import nl.stokpop.lograter.parser.line.LogbackElement;
import nl.stokpop.lograter.parser.line.StringEntryMapper;
import nl.stokpop.lograter.processor.latency.LatencyLogConfig;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class LatencyLogEntry extends LogbackLogEntry {

	private int durationInMillis;

    /**
     * The Latency Log mapper for the latency field translates the duration in milliseconds.
     * Otherwise equal to LogBackLogEntry.
     */
	public static Map<String, LogEntryMapper<LatencyLogEntry>> initializeLatencyLogMappers(List<LogbackElement> elements, LatencyLogConfig latencyConfig) {
		Map<String, LogEntryMapper<LatencyLogEntry>> mappers = new HashMap<>();

        LogbackMappers.initializeMappers(elements, mappers);

		final Pattern latencyPattern = findLatencyPattern(elements, latencyConfig.getLatencyField());

		StringEntryMapper<LatencyLogEntry> latencyLogEntryMapper = (value, variable, e) -> {
			String durationAsString = latencyPattern == null ? value : latencyPattern.matcher(value).group(0);
			e.durationInMillis = parseLatencyToMillis(durationAsString, latencyConfig.getLatencyUnit());
		};

        mappers.put(latencyConfig.getLatencyField(), latencyLogEntryMapper);

        return mappers;
	}

	private static int parseLatencyToMillis(String durationAsString, LatencyUnit latencyUnit) {

		switch (latencyUnit) {
			case milliseconds:
				return Integer.parseInt(durationAsString);
			case seconds: {
				double durationAsIs = Double.parseDouble(durationAsString);
				return (int) (durationAsIs * 1000);
			}
			case microseconds: {
				int durationAsIs = Integer.parseInt(durationAsString);
				return durationAsIs / 1000;
			}
			case nanoseconds: {
				long durationAsIs = Long.parseLong(durationAsString);
				return (int) (durationAsIs / 1_000_000);
			}
			default:
				throw new LogRaterException("Calculation for " + latencyUnit + " for " + durationAsString + " is not implemented yet.");
		}
	}

	@Nullable
	private static Pattern findLatencyPattern(List<LogbackElement> elements, String latencyField) {
		Pattern latencyPattern;
		String latencyRegexp = LogbackElement.findVariableForElement(elements, latencyField);
		if (latencyRegexp == null) {
			latencyPattern = null;
		}
		else if (!containsRegexpGroup(latencyRegexp)) {
			throw new LogRaterException("The latency variable of " + latencyField + " field should contain a regexp with 1 group for the latency value, like (.*?)");
		}
		else {
		 	latencyPattern = Pattern.compile(latencyRegexp);
		}
		return latencyPattern;
	}

	private static boolean containsRegexpGroup(String latencyRegexp) {
		return latencyRegexp.contains("(") && latencyRegexp.contains(")");
	}

	public int getDurationInMillis() {
		return durationInMillis;
	}

	public void setDurationInMillis(int durationInMillis) {
		this.durationInMillis = durationInMillis;
	}

	@Override
	public String toString() {
		return "LatencyLogEntry [durationInMillis=" + durationInMillis
				+ ", toString()=" + super.toString() + "]";
	}

}
