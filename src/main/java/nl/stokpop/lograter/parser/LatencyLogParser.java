package nl.stokpop.lograter.parser;

import nl.stokpop.lograter.logentry.LatencyLogEntry;
import nl.stokpop.lograter.parser.line.LogbackParser;
import nl.stokpop.lograter.processor.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class LatencyLogParser implements LogFileParser<LatencyLogEntry> {
		
	private final Logger log = LoggerFactory.getLogger(LatencyLogParser.class);

	private final LogbackParser<LatencyLogEntry> logbackParser;
	private final List<Processor<LatencyLogEntry>> processors = new ArrayList<>();
	
	public LatencyLogParser(LogbackParser<LatencyLogEntry> parser) {
		super();
		logbackParser = parser;
	}

	public void addProcessor(Processor<LatencyLogEntry> processor) {
		processors.add(processor);
	}
		
	public void addLogLine(final String logfileName, final String logLine) {
		
		if (logLine.isEmpty()) {
			log.debug("nonlog: {}", logLine);
			return;
		}

		LatencyLogEntry entry = logbackParser.parseLogLine(logLine);
		
		for (Processor<LatencyLogEntry> processor : processors) {
			processor.processEntry(entry);
		}
	}
}
