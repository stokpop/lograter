package nl.stokpop.lograter.reportcreator;

import nl.stokpop.lograter.GraphConfig;
import nl.stokpop.lograter.LogRater;
import nl.stokpop.lograter.clickpath.ClickPathCollector;
import nl.stokpop.lograter.clickpath.ClickPathReport;
import nl.stokpop.lograter.command.CommandLatencyLog;
import nl.stokpop.lograter.command.CommandMain;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.graphs.LogGraphCreator;
import nl.stokpop.lograter.processor.latency.LatencyLogConfig;
import nl.stokpop.lograter.processor.latency.LatencyLogDataBundle;
import nl.stokpop.lograter.processor.latency.LatencyLogReader;
import nl.stokpop.lograter.report.text.LatencyLogTextReport;
import nl.stokpop.lograter.store.RequestCounterStorePair;
import nl.stokpop.lograter.util.FileUtils;
import nl.stokpop.lograter.util.time.DateUtils;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

/**
 * Generate a latency log report.
 */
public class LatencyLogReportCreator implements ReportCreatorWithCommand<CommandLatencyLog> {

    private static final Logger log = LoggerFactory.getLogger(LatencyLogReportCreator.class);

    @Override
    public void createReport(PrintWriter outputStream, CommandMain cmdMain, CommandLatencyLog cmdLatency) throws IOException {
        LatencyLogConfig config = new LatencyLogConfig();
        config.setRunId(cmdMain.runId);
        config.setFilterPeriod(DateUtils.createFilterPeriod(cmdMain.startTimeStr, cmdMain.endTimeStr));
        LogRater.populateBasicCounterLogSettings(cmdLatency, config);
        config.setCounterFields(cmdLatency.counterFields);
        config.setLatencyField(cmdLatency.latencyField);
        config.setLatencyUnit(cmdLatency.latencyUnit);
        config.setLogPattern(cmdLatency.logPattern);
        config.setCounterStorage(cmdMain.storage);
        config.setDetermineClickpaths(cmdLatency.determineClickpaths);
        config.setMapperFile(cmdLatency.mapperFile);
        config.setSessionField(cmdLatency.sessionField);
        config.setClickPathShortCodeLength(cmdLatency.clickPathShortCodeLength);
        config.setFileFeederFilterExcludes(cmdLatency.fileFeederFilterExcludes);
        config.setFileFeederFilterIncludes(cmdLatency.fileFeederFilterIncludes);
        config.setFailureAwareAnalysis(true);
        config.setIncludeFailedHitsInAnalysis(true);

        List<File> files = FileUtils.findFilesThatMatchFilenames(cmdLatency.files);
        LatencyLogReader LatencyLogReader = new LatencyLogReader();
        LatencyLogDataBundle dataBundle = LatencyLogReader.readAndProcessLatencyLogs(config, files);

        RequestCounter totalRequestCounter = dataBundle.getTotalRequestCounterStorePair().getRequestCounterStoreSuccess().getTotalRequestCounter();
        TimePeriod analysisPeriod = totalRequestCounter.getTimePeriod().createFilterTimePeriodIfFilterIsSet(config.getFilterPeriod());

        LatencyLogTextReport report = new LatencyLogTextReport(dataBundle);
        LogRater.writeReport(report, cmdMain.outputFilename, new File(cmdMain.reportDirectory), outputStream, analysisPeriod);

        ClickPathCollector clickPathCollector = dataBundle.getClickPathCollector();
        if (clickPathCollector != null) {
            File dir = new File(cmdMain.reportDirectory);
            File clickpathFile = new File(dir, "clickpath-report-" + System.currentTimeMillis() + ".csv");
            ClickPathReport.reportClickpaths(clickPathCollector, clickpathFile, cmdLatency.clickpathReportStepDurations);
            log.info("The click path report: {}", clickpathFile.getPath());
        }

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.setAggregateDurationInSeconds(cmdLatency.aggregateDurationInSeconds);
        graphConfig.setGraphsHistoEnabled(cmdLatency.graphsHisto);
        graphConfig.setGraphsHistoSimulatorEnabled(cmdLatency.graphsHistoSimulator);
        graphConfig.setGraphsPercentileEnabled(cmdLatency.graphsPercentile);
        graphConfig.setGraphsResponseTimesEnabled(cmdLatency.graphsResponseTimes);
        graphConfig.setGraphsTpsEnabled(cmdLatency.graphsTps);
        graphConfig.setGraphsHtmlEnabled(cmdLatency.graphsHtml);
        graphConfig.setGraphWithTrueTPSEnabled(cmdLatency.graphWithTrueTPS);
        graphConfig.setBaseUnit(cmdMain.baseUnit);

        if (graphConfig.isGraphRequested()) {
            LogGraphCreator logGraphCreator = new LogGraphCreator(graphConfig);
            Collection<RequestCounterStorePair> stores = dataBundle.getRequestCounterStorePairs();

            File graphFile = logGraphCreator.createHtmlChartFile(new File(cmdMain.reportDirectory), "performance-log-rater-charts.html", stores, analysisPeriod);
            log.info("Check out graphs: {}", graphFile);
        }

    }
}
