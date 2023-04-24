/*
 * Copyright (C) 2023 Peter Paul Bakker, Stokpop
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
import nl.stokpop.lograter.command.*;
import nl.stokpop.lograter.counter.CounterStorageType;
import nl.stokpop.lograter.processor.BasicCounterLogConfig;
import nl.stokpop.lograter.report.LogReport;
import nl.stokpop.lograter.report.ReportWriter;
import nl.stokpop.lograter.reportcreator.*;
import nl.stokpop.lograter.util.DatabaseBootstrap;
import nl.stokpop.lograter.util.FileUtils;
import nl.stokpop.lograter.util.StringUtils;
import nl.stokpop.lograter.util.time.DateUtils;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.time.ZoneId;
import java.util.*;

public class LogRater {

    public static final Locale DEFAULT_LOCALE = Locale.US;
    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();

    private static final String PROPERTY_FILE_NAME = "lograter.properties";
    private static final String LOGBACK_CONFIGURATION_FILE = "logback.configurationFile";

    private final Logger log;

	private final PrintWriter printWriter;

    public LogRater(PrintWriter printWriter) {
        this.printWriter = printWriter;
        // logger cannot be static here, because we want to init logging before LogRater starts
        log = LoggerFactory.getLogger(LogRater.class);
    }

	public static void main(String[] args) throws Exception {
        boolean debug = Arrays.asList(args).contains("-debug");
        initLogbackLogger(debug);
        new LogRater(FileUtils.createBufferedPrintWriterWithUTF8(System.out)).startLogRater(args);
	}

    /**
     * Use other name than standard logback.xml so projects with a dependency on lograter.jar
     * will not complain about multiple logback.xml's on the classpath.
     *
     * Needs to be called before any logger is created!
     */
    private static void initLogbackLogger(boolean debug) {
        String alreadyDefinedLogbackConfigFile = System.getProperty(LOGBACK_CONFIGURATION_FILE);
        if (alreadyDefinedLogbackConfigFile == null) {
            String logbackFile = debug ? "logback-lograter-debug.xml" : "logback-lograter.xml";
            System.setProperty(LOGBACK_CONFIGURATION_FILE, logbackFile);
        }
        else {
            if (debug) {
                System.err.println("WARN: -debug flag was found but will be ignored, because " + LOGBACK_CONFIGURATION_FILE + " is set to " + alreadyDefinedLogbackConfigFile);
            }
            else {
                System.err.println("INFO: " + LOGBACK_CONFIGURATION_FILE + " is set to " + alreadyDefinedLogbackConfigFile);
            }
        }
    }

    private Properties readPropertiesFile() {
        log.debug("Reading properties file: {}", PROPERTY_FILE_NAME);
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

        CommandMain cmdMain = new CommandMain();

        Properties properties = readPropertiesFile();
        String version = properties.getProperty("project.version", "Unknown");
        cmdMain.setLogFileRaterVersion(version);

        printWriter.println("LogRater version: " + cmdMain.getLogFileRaterVersion());

		String commandLine = StringUtils.recreateCommandLine(args);
		printWriter.println("Command line: " + commandLine);

		Map<LogRaterCommand, ReportCreator> reportCreators = new HashMap<>(extraCommands);

		final CommandAccessLog commandAccessLog = new CommandAccessLog();
        final CommandAccessLogToCsv commandAccessLogToCsv = new CommandAccessLogToCsv();
        final CommandApplicationLog commandApplicationLog = new CommandApplicationLog();
        final CommandIisLog commandIisLog = new CommandIisLog();
        final CommandLargeAllocationsToCsv commandLargeAllocationsToCsv = new CommandLargeAllocationsToCsv();
        final CommandGcVerboseLog commandGcVerboseLog = new CommandGcVerboseLog();
        final CommandPerformanceCenterResults commandPerformanceCenterResults = new CommandPerformanceCenterResults();
        final CommandJMeter commandJMeter = new CommandJMeter();
        final CommandLatencyLog commandLatency = new CommandLatencyLog();

        reportCreators.put(commandAccessLog, (o, c) -> new AccessLogReportCreator().createReport(o, c, commandAccessLog));
        reportCreators.put(commandAccessLogToCsv, (o, c) -> new AccessLogToCsvReportCreator().createReport(o, c, commandAccessLogToCsv));
        reportCreators.put(commandApplicationLog, (o, c) -> new ApplicationLogReportCreator().createReport(o, c, commandApplicationLog));
        reportCreators.put(commandIisLog, (o, c) -> new IisLogReportCreator().createReport(o, c, commandIisLog));
        reportCreators.put(commandLargeAllocationsToCsv, (o, c) -> new LargeAllocationsReportCreator().createReport(o, c, commandLargeAllocationsToCsv));
        reportCreators.put(commandGcVerboseLog, (o, c) -> new GcVerboseLogReportCreator().createReport(o, c, commandGcVerboseLog));
        reportCreators.put(commandPerformanceCenterResults, (o, c) -> new PerformanceCenterResultsReportCreator().createReport(o, c, commandPerformanceCenterResults));
        reportCreators.put(commandJMeter, (o, c) -> new JMeterReportCreator().createReport(o, c, commandJMeter));
        reportCreators.put(commandLatency, (o, c) -> new LatencyLogReportCreator().createReport(o, c, commandLatency));
        // not implemented?? reportCreators.put(new CommandSarLog(), new S...);

        JCommander jc = new JCommander();
        jc.addObject(cmdMain);
        jc.setProgramName(LogRater.class.getCanonicalName());
        reportCreators.keySet().forEach(k -> jc.addCommand(k.getCommandName(), k));
        Set<String> commandNamesSet = jc.getCommands().keySet();

        try {
            jc.parse(args);
        } catch (ParameterException e) {
            final String message = "Unknown command: " + e.getMessage();
            System.out.println(message);
            simpleUsage(commandNamesSet);
            throw new LogRaterException(message, e);
        }

        if (cmdMain.storage == CounterStorageType.Database) {
            DatabaseBootstrap.instance().bootstrapDatabase(cmdMain.clearDb);
        }

        String parsedCommand = jc.getParsedCommand();

        if (parsedCommand == null) {
            simpleUsage(commandNamesSet);
            if (cmdMain.help) {
                jc.usage();
            }
            return;
        }
        if (cmdMain.help) {
            jc.findCommandByAlias(parsedCommand).usage();
            return;
        }

        long timestamp = System.currentTimeMillis();
        cmdMain.reportDirectory = DateUtils.replaceTimestampMarkerInFilename(cmdMain.reportDirectory, timestamp);
        cmdMain.outputFilename = DateUtils.replaceTimestampMarkerInFilename(cmdMain.outputFilename, timestamp);

        File reportDirectory = new File(cmdMain.reportDirectory);
        if (!reportDirectory.exists()) {
            log.info("Report directory not found, create directory: {}", reportDirectory.getAbsolutePath());
            if (!reportDirectory.mkdir()) {
                throw new LogRaterException("Failed to create directory: " + reportDirectory.getAbsolutePath());
            }
        }
        if (!reportDirectory.canWrite()) {
            throw new LogRaterException("Unable to write to directory: " + reportDirectory.getAbsolutePath());
        }

        Optional<Map.Entry<LogRaterCommand, ReportCreator>> entry = reportCreators.entrySet().stream()
            .filter(k -> k.getKey().getCommandName().equalsIgnoreCase(parsedCommand))
            .findFirst();
        if (entry.isPresent()) {
            Map.Entry<LogRaterCommand, ReportCreator> creatorLightEntry = entry.get();
            ReportCreator reportCreatorLight = creatorLightEntry.getValue();
            reportCreatorLight.createReport(printWriter, cmdMain);
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
		System.out.println("Available commands: " + commandsToUse.subSequence(0, commandsToUse.length() - 2) +
				"\nUse [options] [command] [command-options]." +
                "\nUse --help [command] for command specific options." +
                "\nUse --help for generic options.");
	}

	public static void populateBasicCounterLogSettings(AbstractCommandBasic commandBasic, BasicCounterLogConfig config) {
		config.setCalculateConcurrentCalls(commandBasic.reportConc);
		config.setCalculateHitsPerSecond(commandBasic.reportTPS);
		config.setCalculateStdDev(commandBasic.reportSD);
        config.setCalculateStubDelays(commandBasic.reportStubDelays);
        config.setReportPercentiles(commandBasic.reportPercentiles == null ? new Double[]{} : commandBasic.reportPercentiles.toArray(new Double[0]));
        config.setMaxUniqueRequests(commandBasic.maxUniqueCounters);
        // only override the default values of failure awareness if explicitly set
        if (commandBasic.failureAwareAnalysis != null) { config.setFailureAwareAnalysis(commandBasic.failureAwareAnalysis); }
        if (commandBasic.includeFailedHitsInAnalysis != null) { config.setIncludeFailedHitsInAnalysis(commandBasic.includeFailedHitsInAnalysis); }
    }

	public static void writeReport(LogReport report, String outputFilename, File reportDirectory, PrintWriter out, TimePeriod analysisPeriod) throws IOException {
		if (outputFilename != null) {
			ReportWriter.write(outputFilename, reportDirectory, report, analysisPeriod);
		}
		else {
			ReportWriter.write(out, report, analysisPeriod);
		}
	}

}
