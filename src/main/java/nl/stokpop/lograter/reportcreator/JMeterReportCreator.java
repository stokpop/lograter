/*
 * Copyright (C) 2020 Peter Paul Bakker, Stokpop Software Solutions
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
package nl.stokpop.lograter.reportcreator;

import nl.stokpop.lograter.GraphConfig;
import nl.stokpop.lograter.LogRater;
import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.command.CommandJMeter;
import nl.stokpop.lograter.command.CommandMain;
import nl.stokpop.lograter.graphs.LogGraphCreator;
import nl.stokpop.lograter.processor.jmeter.JMeterConfig;
import nl.stokpop.lograter.processor.jmeter.JMeterDataBundle;
import nl.stokpop.lograter.processor.jmeter.JMeterLogReader;
import nl.stokpop.lograter.report.text.JMeterTextReport;
import nl.stokpop.lograter.util.linemapper.LineMapperSection;
import nl.stokpop.lograter.util.linemapper.LineMapperUtils;
import nl.stokpop.lograter.util.time.DateUtils;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Create reports for jMeter jtl files.
 */
public class JMeterReportCreator implements ReportCreatorWithCommand<CommandJMeter> {
	
	private static final Logger log = LoggerFactory.getLogger(JMeterReportCreator.class);

	@Override
	public void createReport(PrintWriter outputStream, CommandMain cmdMain, CommandJMeter cmdJMeter) throws IOException {
        List<String> files = cmdJMeter.files;
        if (files.isEmpty()) {
            throw new LogRaterException("Please supply at least one jMeter jtl file.");
        }

        if (!files.stream().allMatch(f -> f.endsWith("jtl"))) {
            throw new LogRaterException("Only jMeter jtl files expected.");
        }

        processFile(outputStream, files, cmdMain, cmdJMeter);

    }

    private void processFile(final PrintWriter outputStream, final List<String> files, final CommandMain cmdMain, final CommandJMeter cmdJMeter) throws IOException {
        log.info("Start file processing {}.", files);

        if (files.isEmpty()) {
            log.warn("No jmeter files to process, list of file names is empty!");
            return;
        }

        JMeterConfig config = createJMeterConfig(cmdMain, cmdJMeter);

        JMeterLogReader jMeterLogReader = new JMeterLogReader();
        JMeterDataBundle dataBundle = jMeterLogReader.readAndProcessJMeterLogs(config, files);

        JMeterTextReport report = new JMeterTextReport(dataBundle);

        TimePeriod analysisPeriod = dataBundle.getTotalRequestCounterStorePair().totalTimePeriod().createFilterTimePeriodIfFilterIsSet(config.getFilterPeriod());

        LogRater.writeReport(report, cmdMain.outputFilename, new File(cmdMain.reportDirectory), outputStream, analysisPeriod);

        File reportDir = new File(cmdMain.reportDirectory);

        GraphConfig graphConfig = new GraphConfig();
        graphConfig.setAggregateDurationInSeconds(cmdJMeter.aggregateDurationInSeconds);
        graphConfig.setGraphsHistoEnabled(cmdJMeter.graphsHisto);
        graphConfig.setGraphsHistoSimulatorEnabled(cmdJMeter.graphsHistoSimulator);
        graphConfig.setGraphsPercentileEnabled(cmdJMeter.graphsPercentile);
        graphConfig.setGraphsResponseTimesEnabled(cmdJMeter.graphsResponseTimes);
        graphConfig.setGraphsTpsEnabled(cmdJMeter.graphsTps);
        graphConfig.setGraphsHtmlEnabled(cmdJMeter.graphsHtml);
        graphConfig.setGraphWithTrueTPSEnabled(cmdJMeter.graphWithTrueTPS);
        graphConfig.setBaseUnit(cmdMain.baseUnit);

        if (graphConfig.isGraphRequested()) {
            LogGraphCreator graphCreator = new LogGraphCreator(graphConfig);
            File graphFile = graphCreator.createHtmlChartFile(reportDir, "jmeter-log-rater-charts.html", dataBundle.getRequestCounterStorePairs(), analysisPeriod);
            log.info("Check out graphs: {}", graphFile);
        }
    }

    private JMeterConfig createJMeterConfig(CommandMain cmdMain, CommandJMeter cmdJMeter) throws IOException {

        List<LineMapperSection> lineMapper = cmdJMeter.useSingleMapper ? LineMapperSection.SINGLE_MAPPER : LineMapperUtils.createLineMapper(cmdJMeter.mapperFile);

	    JMeterConfig config = new JMeterConfig();
        LogRater.populateBasicCounterLogSettings(cmdJMeter, config);
        config.setRunId(cmdMain.runId);
        config.setFilterPeriod(DateUtils.createFilterPeriod(cmdMain.startTimeStr, cmdMain.endTimeStr));
        config.setDoCountMultipleMapperHits(cmdJMeter.doCountMultipleMapperHits);
        config.setDoFilterOnHttpStatus(cmdJMeter.doGroupByHttpStatus);
        config.setIgnoreMultiAndNoMatches(cmdJMeter.ignoreMultiAndNoMatches);
        config.setLineMappers(lineMapper);
        config.setIncludeMapperRegexpColumn(cmdJMeter.includeMapperRegexpColumn);
        config.setLogPattern(cmdJMeter.logPattern);
        config.setCounterStorage(cmdMain.storage);
        config.setCounterStorageDir(cmdMain.storageDir);
        config.setFileFeederFilterIncludes(cmdJMeter.fileFeederFilterIncludes);
        config.setFileFeederFilterExcludes(cmdJMeter.fileFeederFilterExcludes);

        return config;
    }
}
