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
package nl.stokpop.lograter.graphs;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.painters.TracePainterVerticalBar;
import info.monitorenter.util.Range;
import nl.stokpop.lograter.GraphConfig;
import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.analysis.HistogramData;
import nl.stokpop.lograter.analysis.ResponseTimeAnalyser;
import nl.stokpop.lograter.analysis.ResponseTimeAnalyserFactory;
import nl.stokpop.lograter.analysis.ResponseTimeAnalyserFailureUnaware;
import nl.stokpop.lograter.counter.CounterKey;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.store.*;
import nl.stokpop.lograter.util.FileUtils;
import nl.stokpop.lograter.util.RandomGenerator;
import nl.stokpop.lograter.util.metric.MetricPoint;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LogGraphCreator extends AbstractGraphCreator {

    private static final Logger log = LoggerFactory.getLogger(LogGraphCreator.class);

    public static final int GRAPH_AGGREGATION_CUTOFF_NR_HITS = 10_000;
    
    private static final long GRAPH_DRAW_CUTOFF_NR_HITS = 3;
    private static final long GRAPH_DRAW_CUTOFF_MAX_NR_WARNINGS = 512;

    private final GraphConfig graphConfig;

    private int graphDrawCutoffWarningCount = 0;

    public LogGraphCreator(GraphConfig graphConfig) {
        this.graphConfig = graphConfig;
    }

    private ITrace2D toITrace2D(RequestCounter counter, ITrace2D trace) {

        // workaround to start at xAxis 0
        trace.addPoint(counter.getTimePeriod().getStartTime(), 0L);

        for (TimeMeasurement time : counter) {
            trace.addPoint(time.getTimestamp(), time.getDurationInMillis());
        }

        return trace;
    }

    private ITrace2D toITrace2D(List<MetricPoint> tpsPerTimestamp, ITrace2D trace) {

        for (MetricPoint metricPoint : tpsPerTimestamp) {
            trace.addPoint(metricPoint.getTimestamp(), metricPoint.getHitsPerSecond());
        }

        return trace;
    }

    public File createHtmlChartFile(File dir,
                                    String chartFileName,
                                    Collection<RequestCounterStorePair> counterStores,
                                    TimePeriod filterPeriod) {

        String dateStr = formatTimeToStandardDateString(System.currentTimeMillis());

        File subDirGraphs = new File(dir, "log-rater-graphs-" + dateStr);
        if (!subDirGraphs.exists()) {
            if (!subDirGraphs.mkdirs()) {
	            throw new LogRaterException(String.format("Cannot create directories: %s", subDirGraphs));
            }
        }
        File subDirJsGraphs = new File(dir, "log-rater-jsgraphs-" + dateStr);
	    if (!subDirJsGraphs.exists() && !subDirJsGraphs.mkdirs()) {
            throw new LogRaterException(String.format("Cannot create directories: %s", subDirJsGraphs));
	    }

        List<ChartFile> chartFiles = new ArrayList<>();

        for (RequestCounterStorePair counterStore : counterStores) {

        	RequestCounterStore requestCounterStoreSuccess = counterStore.getRequestCounterStoreSuccess();
        	RequestCounterStore requestCounterStoreFailure = counterStore.getRequestCounterStoreFailure();

	        createRequestCounterStoreGraphs(filterPeriod, subDirGraphs, subDirJsGraphs, chartFiles, requestCounterStoreSuccess, RequestCounterStoreType.success);
	        createRequestCounterStoreGraphs(filterPeriod, subDirGraphs, subDirJsGraphs, chartFiles, requestCounterStoreFailure, RequestCounterStoreType.failure);
        }

	    return createOverallChartFile(dir, chartFileName, chartFiles);

    }

	private void createRequestCounterStoreGraphs(TimePeriod filterPeriod,
                                                 File subDirGraphs,
                                                 File subDirJsGraphs,
                                                 List<ChartFile> chartFiles,
                                                 RequestCounterStore counterStore,
                                                 RequestCounterStoreType counterStoreType) {
		if (counterStore.isEmpty()) {
			log.warn("Skipping empty [{}] requestCounterStore [{}]", counterStoreType, counterStore);
		}
		else {
			log.info("Generate graphs for [{}] requestCounterStore [{}] for period [{}]", counterStoreType, counterStore, filterPeriod);
			RequestCounter totalCounter = counterStore.getTotalRequestCounter();
			produceGraphsForCounter(filterPeriod, subDirGraphs, subDirJsGraphs, chartFiles, totalCounter, counterStoreType);

			for (RequestCounter counter : counterStore) {
				produceGraphsForCounter(filterPeriod, subDirGraphs, subDirJsGraphs, chartFiles, counter, counterStoreType);
			}
		}
	}

	private void produceGraphsForCounter(TimePeriod timePeriodFilter,
                                         File subDirGraphs,
                                         File subDirJsGraphs,
                                         List<ChartFile> chartFiles,
                                         RequestCounter counter,
                                         RequestCounterStoreType counterType) {

        RequestCounter timeSlicedCounter = counter.getTimeSlicedCounter(timePeriodFilter);

        if (!hasCounterSufficientHitsForGraphing(timeSlicedCounter)) return;

        // TODO: check the kind of analyser that is needed
        // now the graphs of success and failure are split, so probably good enough
        final ResponseTimeAnalyser analyser = ResponseTimeAnalyserFactory.createSimpleFailureUnaware(timeSlicedCounter, timePeriodFilter);

        if (graphConfig.isGraphsResponseTimesEnabled()) {
            chartFiles.add(createResponseTimesGraph(timePeriodFilter, subDirGraphs, counterType, timeSlicedCounter, analyser));
        }

        if (graphConfig.isGraphsTpsEnabled()) {
            chartFiles.add(createTpsGraph(timePeriodFilter, subDirGraphs, counterType, timeSlicedCounter, analyser));
        }

        if (graphConfig.isGraphsHistoEnabled()) {
            chartFiles.addAll(createHistoGraphs(timePeriodFilter, subDirGraphs, counterType, timeSlicedCounter, analyser));
        }

        if (graphConfig.isGraphsPercentileEnabled()) {
            chartFiles.add(createPercentileGraph(subDirGraphs, counterType, analyser));
        }

        if (graphConfig.isGraphRequested()) {
            String htmlGraphName = String.format("%s-html-graphs", analyser.getCounterKey());
            log.debug("Starting html graphs: {}", htmlGraphName);
            chartFiles.add(HtmlGraphCreator.writeHtmlGoogleGraphFile(subDirJsGraphs, analyser, graphConfig.getBaseUnit()));
        }
    }

    private ChartFile createPercentileGraph(File subDirGraphs, RequestCounterStoreType counterType, ResponseTimeAnalyser analyser) {
        String percentileGraphName = String.format("%s-%s-percentiles.min(%d).max(%d)", counterType, analyser.getCounterKey(), analyser.min(), analyser.max());
        log.debug("Starting graph: {}", percentileGraphName);
        // get 99th percentile as maximum to avoid extreme max values to hide all other percentiles
        File file = showPercentileGraph(subDirGraphs, percentileGraphName, analyser.percentiles(99));
        return new ChartFile(percentileGraphName, file);
    }

    private List<ChartFile> createHistoGraphs(TimePeriod timePeriodFilter, File subDirGraphs, RequestCounterStoreType counterType, RequestCounter timeSlicedCounter, ResponseTimeAnalyser analyser) {
        List<ChartFile> files = new ArrayList<>();
        String histoGraphName = String.format("%s-%s-histogram.min(%d).max(%d)", counterType, analyser.getCounterKey(), analyser.min(), analyser.max());
        log.debug("Starting graph: {}", histoGraphName);

        HistogramData histogram = analyser.histogramForRelevantValues(ResponseTimeAnalyserFailureUnaware.GRAPH_HISTO_NUMBER_OF_RANGES);

        int nrOfxValues = histogram.getXvalues().length;
        final int minNrOfxValues = 3;

        if (nrOfxValues < minNrOfxValues) {
            log.warn("Too little x values ({}/{}) to create histogram graph for {} ", nrOfxValues, minNrOfxValues, analyser.getCounter().getCounterKey());
        } else {
            File histoGraphFile = showHistoGraph(subDirGraphs, histoGraphName, histogram);
            files.add(new ChartFile(histoGraphName, histoGraphName, histoGraphFile, ChartFile.ChartType.PNG));
        }

        if (graphConfig.isGraphsHistoSimulatorEnabled()) {
            RequestCounter simulatedCounter = new RequestCounter(CounterKey.of("simulatedResponseTimeCounter"), new TimeMeasurementStoreInMemory());

            int numberOfValues = (int) timeSlicedCounter.getHits();
            double[] simulatedValues = new RandomGenerator().generateNormalDistributionSet(numberOfValues, analyser.stdDevHitDuration(), analyser.avgHitDuration(), analyser.min(), analyser.max());

            for (int i = 0; i < numberOfValues; i++) {
                simulatedCounter.incRequests(System.currentTimeMillis(), (int) simulatedValues[i]);
            }
            ResponseTimeAnalyser simulatedValuesAnalyser = ResponseTimeAnalyserFactory.createSimpleFailureUnaware(simulatedCounter, timePeriodFilter);
            HistogramData simHistogramData = simulatedValuesAnalyser.histogramForRelevantValues(ResponseTimeAnalyserFailureUnaware.GRAPH_HISTO_NUMBER_OF_RANGES);
            String simulatedHistogramName = String.format("%s.sim", histoGraphName);
            File file = showHistoGraph(subDirGraphs, simulatedHistogramName, simHistogramData);
            files.add(new ChartFile(simulatedHistogramName, file));
        }
        return files;
    }

    private ChartFile createTpsGraph(TimePeriod timePeriodFilter, File subDirGraphs, RequestCounterStoreType counterType, RequestCounter timeSlicedCounter, ResponseTimeAnalyser analyser) {
        String tpsGraphName = graphConfig.isGraphWithTrueTPSEnabled() ? "perSec(1)" : "movingAvgPerMin(1)";

        List<MetricPoint> metricPoints = analyser.metricPoints();

        String counterNameTps = String.format("%s-%s-tps.%s", counterType, timeSlicedCounter.getCounterKey(), tpsGraphName);

        log.debug("Create graph: {} with {} points", counterNameTps, metricPoints.size());
        long maxTPS = analyser.maxHitsPerDuration(1000).getMaxHitsPerDuration();
        File file = showTpsGraph(subDirGraphs, counterNameTps, metricPoints, timePeriodFilter, maxTPS);
        return new ChartFile(counterNameTps, file);
    }

    private ChartFile createResponseTimesGraph(TimePeriod timePeriodFilter, File subDirGraphs, RequestCounterStoreType counterType, RequestCounter timeSlicedCounter, ResponseTimeAnalyser analyser) {
        RequestCounter reducedCounter = null;
        if (timeSlicedCounter.getHits() > GRAPH_AGGREGATION_CUTOFF_NR_HITS) {
            final String reducedCounterName = String.format("%s-%s-duration.avgPerSec(%d)", counterType, timeSlicedCounter.getCounterKey(), graphConfig.getAggregateDurationInSeconds());
            reducedCounter = new RequestCounter(CounterKey.of(reducedCounterName), new TimeMeasurementStoreInMemory());
            RequestCounter.fillReducedCounter(timeSlicedCounter, reducedCounter, graphConfig.getAggregateDurationInSeconds());
        }

        long maxDurationGraphView = analyser.percentilePlus(99.0) * 5;

        if (reducedCounter == null) {
            String counterNameDuration = String.format("%s-%s-duration",  counterType, timeSlicedCounter.getCounterKey());
            log.debug("Starting graph: {}", counterNameDuration);
            File file = writeResponseGraphFile(subDirGraphs, counterNameDuration, timeSlicedCounter, timePeriodFilter, maxDurationGraphView);
            return new ChartFile(counterNameDuration, file);
        } else {
            CounterKey key = reducedCounter.getCounterKey();
            String graphName = key.getName();
            log.debug("Starting graph: {}", graphName);
            File file = writeResponseGraphFile(subDirGraphs, graphName, reducedCounter, timePeriodFilter, maxDurationGraphView);
            return new ChartFile(graphName, file);
        }
    }

    private boolean hasCounterSufficientHitsForGraphing(RequestCounter timeSlicedCounter) {
        if (timeSlicedCounter.getHits() < GRAPH_DRAW_CUTOFF_NR_HITS) {
            // limit the number of warnings we log since it may result in an OutOfMemoryException in Central
            // when output is processed (logged to Central log)
            graphDrawCutoffWarningCount++;
            if (graphDrawCutoffWarningCount < GRAPH_DRAW_CUTOFF_MAX_NR_WARNINGS) {
                log.warn(
                        "Skip graph: {} (< {}) points in timeSlicedCounter [{}]",
                        timeSlicedCounter.getHits(), GRAPH_DRAW_CUTOFF_NR_HITS,
                        timeSlicedCounter.getCounterKey());
            } else if (graphDrawCutoffWarningCount == GRAPH_DRAW_CUTOFF_MAX_NR_WARNINGS) {
                log.warn(
                        "Skip graph: {} (< {}) points in timeSlicedCounter [{}]. Suppressing further warnings like this ({} warnings logged)",
                        timeSlicedCounter.getHits(), GRAPH_DRAW_CUTOFF_NR_HITS,
                        timeSlicedCounter.getCounterKey(),
                        graphDrawCutoffWarningCount
                );
            }
            return false;
        }
        return true;
    }

    private File createOverallChartFile(File dir, String chartFileName, List<ChartFile> chartFiles) {
        File chartHtmlFile = new File(dir, chartFileName);

        try (PrintWriter out = FileUtils.createBufferedPrintWriterWithUTF8(chartHtmlFile)) {
            out.println("<html>");
            out.println(insertCollapsibleSnippet());
            out.println("<h1>LogRater Graphs</h1>");
            out.println("<button id=\"expand_collapse_all\" class=\"expand-collapse-all-btn\">Expand All</button>");
            out.println("<br/><br/>");
            for (ChartFile chartFile : chartFiles) {
                final String filePath = FileUtils.findRelativePath(dir, chartFile.getFile()).replace('\\', '/');
                if (chartFile.getType() == ChartFile.ChartType.PNG) {
                    String collabsebleContent = String.format("<img src=\"%s\"/>", filePath);
                    String buttonTitle = chartFile.getTitle();
                    out.println(insertCollabsebleButton(buttonTitle, collabsebleContent));
                    out.println("<br/>");
                } else if (chartFile.getType() == ChartFile.ChartType.HTML) {
                    out.println(String.format("<p><a href=\"%s\" style=\"font-size: 15px; padding: 18px;\">Interactive charts for %s</a><br/></p>", filePath, chartFile.getTitle()));
                }
            }
            out.println(insertCollapsebleScript());
            out.println(insertCollapseAllScript());
            out.println("</html>");
        } catch (IOException e) {
            log.error("Cannot write chart file " + chartHtmlFile, e);
        }
        return chartHtmlFile;
    }

    private String insertCollabsebleButton(String buttonTitle, String collabsebleContent) {
        return String.format("<button class=\"collapsible\">%s</button>%n" +
                "<div class=\"content\">%n" +
                "  <p>%s</p>%n" +
                "</div>", buttonTitle, collabsebleContent);
    }

    private String insertCollapsebleScript() {
        return "<script>\n" +
                "var coll = document.getElementsByClassName(\"collapsible\");\n" +
                "var i;\n" +
                "\n" +
                "for (i = 0; i < coll.length; i++) {\n" +
                "    coll[i].addEventListener(\"click\", function() {\n" +
                "        this.classList.toggle(\"active\");\n" +
                "        var content = this.nextElementSibling;\n" +
                "        if (content.style.display === \"block\") {\n" +
                "            content.style.display = \"none\";\n" +
                "        } else {\n" +
                "            content.style.display = \"block\";\n" +
                "        }\n" +
                "    });\n" +
                "}\n" +
                "</script>\n";
    }

    private String insertCollapseAllScript() {
        return "<script>\n" +
                "    let expandCollapseAllButton = document.getElementById(\"expand_collapse_all\");\n" +
                "    let elements = document.getElementsByClassName(\"collapsible\");\n" +
                "\n" +
                "    expandCollapseAllButton.addEventListener(\"click\", function() {\n" +
                "        let buttonText = expandCollapseAllButton.innerText;\n" +
                "        if (buttonText === \"Expand All\") {\n" +
                "            expandCollapseAllButton.innerText = \"Collapse All\";\n" +
                "            for(let i = 0; i < elements.length; i++) {\n" +
                "                let element = elements[i];\n" +
                "                element.classList.toggle(\"active\");\n" +
                "                element.nextElementSibling.style.display = \"block\";\n" +
                "            }\n" +
                "        } else {\n" +
                "            expandCollapseAllButton.innerText = \"Expand All\";\n" +
                "            for(let i = 0; i < elements.length; i++) {\n" +
                "                let element = elements[i];\n" +
                "                element.classList.toggle(\"active\");\n" +
                "                element.nextElementSibling.style.display = \"none\";\n" +
                "            }\n" +
                "        }\n" +
                "    });\n" +
                "</script>";
    }

    private String insertCollapsibleSnippet() {
            return "<head>\n" +
                    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                    "<style>\n" +
                    ".collapsible {\n" +
                    "    background-color: #777;\n" +
                    "    color: white;\n" +
                    "    cursor: pointer;\n" +
                    "    padding: 18px;\n" +
                    "    width: 100%;\n" +
                    "    border: none;\n" +
                    "    text-align: left;\n" +
                    "    outline: none;\n" +
                    "    font-size: 15px;\n" +
                    "}\n" +
                    "\n" +
                    ".active, .collapsible:hover {\n" +
                    "    background-color: #555;\n" +
                    "}\n" +
                    "\n" +
                    ".content {\n" +
                    "    padding: 0 18px;\n" +
                    "    display: none;\n" +
                    "    overflow: hidden;\n" +
                    "    background-color: #f1f1f1;\n" +
                    "}\n" +
                    ".expand-collapse-all-btn {\n" +
                    "     word-wrap: break-word;\n" +
                    "     width: 100px;\n" +
                    "     max-width: 100%;\n" +
                    "     font-size: 14px;\n" +
                    "     color: #fff;\n" +
                    "     display: inline-block;\n" +
                    "     margin-bottom: 0;\n" +
                    "     font-weight: normal;\n" +
                    "     text-align: center;\n" +
                    "     vertical-align: middle;\n" +
                    "     touch-action: manipulation;\n" +
                    "     cursor: pointer;\n" +
                    "     background: #009 none;\n" +
                    "     border: 1px solid transparent;\n" +
                    "     white-space: nowrap;\n" +
                    "     line-height: 1.846;\n" +
                    "     border-radius: 3px;\n" +
                    "     padding: 6px 16px;\n" +
                    "}" +
                    "</style>\n" +
                    "</head>";
    }

    private File writeMetricPointsGraphFile(File dir, String chartName, List<MetricPoint> points, MetricPointGraphDetails details) {
        Chart2D chart = createChart2D(chartName);

        Color lineColor = Color.MAGENTA;
        String yAxisLabel = "metric points - " + details.getName();

        long startTime = points.get(0).getTimestamp();
        long endTime = points.get(points.size() - 1).getTimestamp();
        ITrace2D timeTrace = createTimeTrace(chartName, chart, lineColor, yAxisLabel, startTime, endTime);

        // Add the trace to the chart. This has to be done before adding points
        // (deadlock prevention):
        chart.addTrace(timeTrace);

        for (MetricPoint point : points) {
            timeTrace.addPoint(point.getTimestamp(), details.getValue(point));
        }

        File graphFile = writeChartToPngFile(dir, chartName, chart);

        // disable the listener threads
        chart.destroy();

        return graphFile;
    }

    private File showPercentileGraph(File subDir, String graphname, long[] percentiles) {

        Chart2D chart = createChart2D(graphname);

        chart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(0, (int) percentiles[percentiles.length-1])));

        Color lineColor = Color.GREEN;
        String yAxisLabel = "duration";

        ITrace2D trace = createHistoTrace(graphname, chart, lineColor, yAxisLabel);

        // Add the trace to the chart. This has to be done before adding points
        // (deadlock prevention):
        trace.setTracePainter(new TracePainterVerticalBar(10, chart));
        chart.addTrace(trace);

        toITrace2Dpercentile(percentiles, trace);

        File graphFile = writeChartToPngFile(subDir, graphname, chart);

        chart.destroy();

        return graphFile;
    }

    private File writeResponseGraphFile(File dir, String chartName, RequestCounter avgResponseTimeCounter, TimePeriod timePeriod, long maxDurationGraphView) {
        Chart2D chart = createChart2D(chartName);

        chart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(0, maxDurationGraphView)));

        Color lineColor = Color.RED;
        String yAxisLabel = "response time in " + graphConfig.getBaseUnit().fullName();

        ITrace2D trace = createTimeTrace(chartName, chart, lineColor, yAxisLabel, timePeriod.getStartTime(), timePeriod.getEndTime());

        // Add the trace to the chart. This has to be done before adding points
        // (deadlock prevention):
        chart.addTrace(trace);

        toITrace2D(avgResponseTimeCounter, trace);

        File graphFile = writeChartToPngFile(dir, chartName, chart);

        // disable the listener threads
        chart.destroy();

        return graphFile;
    }

    private File showTpsGraph(File dir, String graphName, List<MetricPoint> tpsPerTimestamp, TimePeriod timePeriod, long maxTPS) {

        Chart2D chart = createChart2D(graphName);

        chart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(0, maxTPS)));

        Color lineColor = Color.BLUE;
        String yAxisLabel = "avg TPS per minute";

        ITrace2D trace = createTimeTrace(graphName, chart, lineColor, yAxisLabel, timePeriod.getStartTime(), timePeriod.getEndTime());

        // Add the trace to the chart. This has to be done before adding points
        // (deadlock prevention):
        chart.addTrace(trace);

        toITrace2D(tpsPerTimestamp, trace);

        File graphFile = writeChartToPngFile(dir, graphName, chart);

        // disable the listener threads
        chart.destroy();

        return graphFile;

    }
    
    private File showHistoGraph(File dir, String graphname, HistogramData histogramData) {

        Chart2D chart = createChart2D(graphname);

        Color lineColor = Color.ORANGE;
        String yAxisLabel = "number of hits";

        ITrace2D trace = createHistoTrace(graphname, chart, lineColor, yAxisLabel);

        // Add the trace to the chart. This has to be done before adding points
        // (deadlock prevention):
        trace.setTracePainter(new TracePainterVerticalBar(10, chart));
        chart.addTrace(trace);
        toITrace2D(histogramData.getXvalues(), histogramData.getYvalues(), trace);
        
        File graphFile = writeChartToPngFile(dir, graphname, chart);

        // disable the listener threads
        chart.destroy();

        return graphFile;

    }

    private ITrace2D createLogNormalLineTrace() {
        ITrace2D trace = new Trace2DSimple();
        trace.setName("log normal function fit");
        trace.setColor(Color.BLUE);
        return trace;
    }

    private ITrace2D createNormalLineTrace() {
        ITrace2D trace = new Trace2DSimple();
        trace.setName("normal function fit");
        trace.setColor(Color.BLACK);
        return trace;
    }

    private ITrace2D createHistoTrace(String counterName, Chart2D chart, Color lineColor, String yAxisLabel) {
        // Create an ITrace:
        ITrace2D trace = new Trace2DSimple();
        trace.setName(counterName);
        trace.setColor(lineColor);

        // We want to use a date format for the x axis.
        // Currently works only this way:
        IAxis<?> xAxis = chart.getAxisX();
        IAxis<?> yAxis = chart.getAxisY();

        // Set a date formatter:
        xAxis.setAxisTitle(new AxisTitle("time range from-to in " + graphConfig.getBaseUnit().fullName()));
        xAxis.setPaintScale(true);

        yAxis.setAxisTitle(new AxisTitle(yAxisLabel));
        yAxis.setPixelYBottom(0);
        yAxis.setPaintGrid(true);

        return trace;
    }
}