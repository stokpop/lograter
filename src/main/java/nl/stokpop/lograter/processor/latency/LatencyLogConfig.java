package nl.stokpop.lograter.processor.latency;

import nl.stokpop.lograter.command.LatencyUnit;
import nl.stokpop.lograter.processor.BasicCounterLogConfig;

public class LatencyLogConfig extends BasicCounterLogConfig {
	
	private String counterFields;
	private String logPattern;
	private boolean determineClickpaths;
	private String mapperFile;
	private String sessionField;
	private int clickPathShortCodeLength;
	private String latencyField;
	private LatencyUnit latencyUnit;

	public String getCounterFields() {
		return counterFields;
	}

	public void setCounterFields(String counterFields) {
		this.counterFields = counterFields;
	}

	public String getLogPattern() {
		return logPattern;
	}

	public void setLogPattern(String logPattern) {
		this.logPattern = logPattern;
	}

	public boolean isDetermineClickpaths() {
		return determineClickpaths;
	}

	public void setDetermineClickpaths(boolean determineClickpaths) {
		this.determineClickpaths = determineClickpaths;
	}

	public String getMapperFile() {
		return mapperFile;
	}

	public void setMapperFile(String mapperFile) {
		this.mapperFile = mapperFile;
	}

	public String getSessionField() {
		return sessionField;
	}

	public void setSessionField(String sessionField) {
		this.sessionField = sessionField;
	}

	public int getClickPathShortCodeLength() {
		return clickPathShortCodeLength;
	}

	public void setClickPathShortCodeLength(int clickPathShortCodeLength) {
		this.clickPathShortCodeLength = clickPathShortCodeLength;
	}

	@Override
	public boolean equals(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}

	public String getLatencyField() {
		return latencyField;
	}

	public void setLatencyField(String latencyField) {
		this.latencyField = latencyField;
	}

	public LatencyUnit getLatencyUnit() {
		return latencyUnit;
	}

	public void setLatencyUnit(LatencyUnit latencyUnit) {
		this.latencyUnit = latencyUnit;
	}
}
