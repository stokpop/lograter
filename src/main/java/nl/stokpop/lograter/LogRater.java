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
package nl.stokpop.lograter;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import nl.stokpop.lograter.command.AbstractCommandBasic;
import nl.stokpop.lograter.command.CommandAccessLog;
import nl.stokpop.lograter.command.CommandAccessLogToCsv;
import nl.stokpop.lograter.command.CommandApplicationLog;
import nl.stokpop.lograter.command.CommandGcVerboseLog;
import nl.stokpop.lograter.command.CommandIisLog;
import nl.stokpop.lograter.command.CommandJMeter;
import nl.stokpop.lograter.command.CommandLargeAllocationsToCsv;
import nl.stokpop.lograter.command.CommandMain;
import nl.stokpop.lograter.command.CommandPerformanceCenterResults;
import nl.stokpop.lograter.command.LogRaterCommand;
import nl.stokpop.lograter.counter.CounterStorageType;
import nl.stokpop.lograter.processor.BasicCounterLogConfig;
import nl.stokpop.lograter.report.LogReport;
import nl.stokpop.lograter.report.ReportWriter;
import nl.stokpop.lograter.reportcreator.AccessLogReportCreator;
import nl.stokpop.lograter.reportcreator.AccessLogToCsvReportCreator;
import nl.stokpop.lograter.reportcreator.ApplicationLogReportCreator;
import nl.stokpop.lograter.reportcreator.GcVerboseLogReportCreator;
import nl.stokpop.lograter.reportcreator.IisLogReportCreator;
import nl.stokpop.lograter.reportcreator.JMeterReportCreator;
import nl.stokpop.lograter.reportcreator.LargeAllocationsReportCreator;
import nl.stokpop.lograter.reportcreator.PerformanceCenterResultsReportCreator;
import nl.stokpop.lograter.reportcreator.ReportCreator;
import nl.stokpop.lograter.util.DatabaseBootstrap;
import nl.stokpop.lograter.util.StringUtils;
import nl.stokpop.lograter.util.time.DateUtils;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

public class LogRater {

    private static final String PROPERTY_FILE_NAME = "lograter.properties";
    private static final String LOGBACK_CONFIGURATION_FILE = "logback.configurationFile";

    // Do not make static to prevent static methods from calling log without being initialized.
    private Logger log;

	private PrintStream outputStream;

    public LogRater(PrintStream outputStream) {
        this.outputStream = outputStream;
    }

	public static void main(String[] args) throws Exception {
        new LogRater(System.out).startLogRater(args);
	}

    /**
     * Use other name than standard logback.xml so projects with a dependency on lograter.jar
     * will not complain about multiple logback.xml's on the classpath.
     */
    private static void initLogbackLogger() {
        if (System.getProperty(LOGBACK_CONFIGURATION_FILE) == null) {
            System.setProperty(LOGBACK_CONFIGURATION_FILE, "logback-lograter.xml");
        }
    }

    private Properties readPropertiesFile() {
        final InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTY_FILE_NAME);
        if (resourceAsStream == null) {
            log.error("Property file not found on classpath: " + PROPERTY_FILE_NAME + " Properties will not be present.");
        }
        Properties properties = new Properties();
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            log.error("Not possible to read property file from classpath: " + PROPERTY_FILE_NAME + " Properties will not be present.");
        } finally {
            if (resourceAsStream != null) {
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
		}
        return properties;
    }

    public void startLogRater(String[] args) throws IOException {
        this.startLogRater(args, Collections.emptyMap());
    }

    public void startLogRater(String[] args, Map<LogRaterCommand, ReportCreator> extraCommands) throws IOException {
        initLogbackLogger();

        CommandMain cmdMain = new CommandMain();

        Properties properties = readPropertiesFile();
        String version = properties.getProperty("project.version", "Unknown");
        cmdMain.setLogFileRaterVersion(version);

        outputStream.println("LogRater version: " + cmdMain.getLogFileRaterVersion());

		String commandLine = StringUtils.recreateCommandLine(args);
		outputStream.println("Command line: " + commandLine);

		Map<LogRaterCommand, ReportCreator> reportCreators = new HashMap<>(extraCommands);

		final CommandAccessLog commandAccessLog = new CommandAccessLog();
        final CommandAccessLogToCsv commandAccessLogToCsv = new CommandAccessLogToCsv();
        final CommandApplicationLog commandApplicationLog = new CommandApplicationLog();
        final CommandIisLog commandIisLog = new CommandIisLog();
        final CommandLargeAllocationsToCsv commandLargeAllocationsToCsv = new CommandLargeAllocationsToCsv();
        final CommandGcVerboseLog commandGcVerboseLog = new CommandGcVerboseLog();
        final CommandPerformanceCenterResults commandPerformanceCenterResults = new CommandPerformanceCenterResults();
        final CommandJMeter commandJMeter = new CommandJMeter();

        reportCreators.put(commandAccessLog, (o, c) -> new AccessLogReportCreator().createReport(o, c, commandAccessLog));
        reportCreators.put(commandAccessLogToCsv, (o, c) -> new AccessLogToCsvReportCreator().createReport(o, c, commandAccessLogToCsv));
        reportCreators.put(commandApplicationLog, (o, c) -> new ApplicationLogReportCreator().createReport(o, c, commandApplicationLog));
        reportCreators.put(commandIisLog, (o, c) -> new IisLogReportCreator().createReport(o, c, commandIisLog));
        reportCreators.put(commandLargeAllocationsToCsv, (o, c) -> new LargeAllocationsReportCreator().createReport(o, c, commandLargeAllocationsToCsv));
        reportCreators.put(commandGcVerboseLog, (o, c) -> new GcVerboseLogReportCreator().createReport(o, c, commandGcVerboseLog));
        reportCreators.put(commandPerformanceCenterResults, (o, c) -> new PerformanceCenterResultsReportCreator().createReport(o, c, commandPerformanceCenterResults));
        reportCreators.put(commandJMeter, (o, c) -> new JMeterReportCreator().createReport(o, c, commandJMeter));
        // not implemented?? commandAndCreate.put(new CommandSarLog(), new S);
        
        JCommander jc = new JCommander();
        jc.addObject(cmdMain);
        jc.setProgramName(LogRater.class.getCanonicalName());
        reportCreators.keySet().forEach(k -> jc.addCommand(k.getCommandName(), k));

        Set<String> commandNamesSet = jc.getCommands().keySet();

        try {
            jc.parse(args);
        } catch (ParameterException e) {
            final String message = "Unknown command: " + e.getMessage();
            outputStream.println(message);
            simpleUsage(commandNamesSet);
            throw new LogRaterException(message, e);
        }

        if (cmdMain.debug) {
            System.err.println("WARN: Please use debug configuration of your logging framework to show TRACE or DEBUG in logging.");
        }

        log = LoggerFactory.getLogger(LogRater.class);

        if (cmdMain.storage == CounterStorageType.Database) {
            DatabaseBootstrap.instance().bootstrapDatabase(cmdMain.clearDb);
        }

        String parsedCommand = jc.getParsedCommand();


        if (parsedCommand == null) {
            simpleUsage(commandNamesSet);
            return;
        }

        if (cmdMain.help) {
            simpleUsage(commandNamesSet);
            jc.usage();
            return;
        }

        long timestamp = System.currentTimeMillis();
        cmdMain.reportDirectory = DateUtils.replaceTimestampMarkerInFilename(cmdMain.reportDirectory, timestamp);
        cmdMain.outputFilename = DateUtils.replaceTimestampMarkerInFilename(cmdMain.outputFilename, timestamp);

        Optional<Map.Entry<LogRaterCommand, ReportCreator>> entry = reportCreators.entrySet().stream()
                .filter(k -> k.getKey().getCommandName().equalsIgnoreCase(parsedCommand))
                .findFirst();

        if (entry.isPresent()) {
            Map.Entry<LogRaterCommand, ReportCreator> creatorLightEntry = entry.get();
            ReportCreator reportCreatorLight = creatorLightEntry.getValue();
            reportCreatorLight.createReport(outputStream, cmdMain);
        } else {
            throw new LogRaterException("No implementation for command: " + parsedCommand);
        }
    }

    private void simpleUsage(Collection<String> commands) {
		JCommander jcSimple = new JCommander();
		jcSimple.addObject(new CommandMain());
		StringBuilder commandsToUse = new StringBuilder();
		for (String commandName :  commands) {
			commandsToUse.append(commandName).append(", ");
		}
		outputStream.println("Use one of the following commands after the [options] below: " + commandsToUse.subSequence(0, commandsToUse.length() - 2) +
				"\nUse --help [command] for command specific help.");
		jcSimple.usage();
	}

	public static void populateBasicCounterLogSettings(AbstractCommandBasic commandBasic, BasicCounterLogConfig config) {
		config.setCalculateConcurrentCalls(commandBasic.reportConc);
		config.setCalculateHitsPerSecond(commandBasic.reportTPS);
		config.setCalculateStdDev(commandBasic.reportSD);
        config.setCalculateStubDelays(commandBasic.reportStubDelays);
        config.setReportPercentiles(commandBasic.reportPercentiles == null ? new Double[]{} : commandBasic.reportPercentiles.toArray(new Double[0]));
        config.setMaxNoMapperCount(commandBasic.maxNoMapperCount);
        // only override the default values of failure awareness if explicitly set
        if (commandBasic.failureAwareAnalysis != null) { config.setFailureAwareAnalysis(commandBasic.failureAwareAnalysis); }
        if (commandBasic.includeFailedHitsInAnalysis != null) { config.setIncludeFailedHitsInAnalysis(commandBasic.includeFailedHitsInAnalysis); }

    }

	public static void writeReport(LogReport report, String outputFilename, File reportDirectory, PrintStream out, TimePeriod analysisPeriod) throws IOException {
		if (outputFilename != null) {
			ReportWriter.write(outputFilename, reportDirectory, report, analysisPeriod);
		}
		else {
			ReportWriter.write(out, report, analysisPeriod);
		}
	}

}
