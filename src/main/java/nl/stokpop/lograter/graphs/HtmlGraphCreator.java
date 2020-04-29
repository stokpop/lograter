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

import nl.stokpop.lograter.analysis.HistogramData;
import nl.stokpop.lograter.analysis.ResponseTimeAnalyser;
import nl.stokpop.lograter.command.BaseUnit;
import nl.stokpop.lograter.util.FileUtils;
import nl.stokpop.lograter.util.metric.MetricPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Create graphs with google graphs: https://google-developers.appspot.com/chart/
 */
public class HtmlGraphCreator {

    private final static Logger log = LoggerFactory.getLogger(HtmlGraphCreator.class);

	private final static Pattern PATTERN_TABLE_PERCENTILES = Pattern.compile("###TABLE_PERCENTILES###");
	private final static Pattern PATTERN_TABLE = Pattern.compile("###TABLE###");
	private final static Pattern PATTERN_TITLE = Pattern.compile("###TITLE###");
	private final static Pattern PATTERN_H_AXIS_TITLE = Pattern.compile("###H_AXIS_TITLE###");
	private final static Pattern PATTERN_V_AXIS_TITLE = Pattern.compile("###V_AXIS_TITLE###");
	private final static Pattern PATTERN_V_AXIS_MAX = Pattern.compile("###V_AXIS_MAX###");
	private final static Pattern PATTERN_TABLE2 = Pattern.compile("###TABLE2###");
	private final static Pattern PATTERN_TITLE2 = Pattern.compile("###TITLE2###");
	private final static Pattern PATTERN_H_AXIS_TITLE2 = Pattern.compile("###H_AXIS_TITLE2###");
	private final static Pattern PATTERN_V_AXIS_TITLE2 = Pattern.compile("###V_AXIS_TITLE2###");
	private final static Pattern PATTERN_TABLE_HISTO = Pattern.compile("###TABLE_HISTO###");
	private final static Pattern PATTERN_BASE_UNIT_SHORT = Pattern.compile("###BU_SHORT###");
	private final static Pattern PATTERN_BASE_UNIT_FULL = Pattern.compile("###BU_FULL###");

    private final static String CHART_HTML_TEMPLATE = "<html>\n" +
            "  <head>\n" +
            "    <script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>\n" +
            "    <script type=\"text/javascript\">\n" +
            "      google.load(\"visualization\", \"1\", {packages:[\"corechart\", \"bar\"]});\n" +
            "      google.setOnLoadCallback(drawChart);\n" +
            "      function drawChart() {\n" +
            "        var dataResponseTimes = google.visualization.arrayToDataTable([\n" +
            "          ###TABLE###\n" +
            "        ]);\n" +
            "\n" +
            "        var optionsResponseTimes = {\n" +
//            "          explorer: {},\n" +   // messes with format of axis: https://code.google.com/p/google-visualization-api-issues/issues/detail?id=1590
            "          title: '###TITLE###',\n" +
            "          hAxis: {title: '###H_AXIS_TITLE###',  titleTextStyle: {color: '#333'}, format: 'dd-MM HH:mm', minorGridlines: {count: 10, format: 'mm'} },\n" +
            "          vAxis: {title: '###V_AXIS_TITLE###', viewWindow: {max: ###V_AXIS_MAX###} }\n" +
            "        };\n" +
            "\n" +
            "        var chartResponseTimes = new google.visualization.LineChart(document.getElementById('chart_div_response_times'));\n" +
            "        chartResponseTimes.draw(dataResponseTimes, optionsResponseTimes);\n" +
            "\n" +
            "        var dataTps = google.visualization.arrayToDataTable([\n" +
            "          ###TABLE2###\n" +
            "        ]);\n" +
            "\n" +
            "        var optionsTps = {\n" +
        //    "          explorer: {},\n" +
            "          title: '###TITLE2###',\n" +
            "          hAxis: {title: '###H_AXIS_TITLE2###',  titleTextStyle: {color: '#333'}},\n" +
            "          vAxis: {title: '###V_AXIS_TITLE2###', viewWindow: {min: 0}}\n" +
            "        };\n" +
            "\n" +
            "        var chartTps = new google.visualization.LineChart(document.getElementById('chart_div_tps'));\n" +
            "        chartTps.draw(dataTps, optionsTps);\n" +
            "\n" +
            "        var dataPercentiles = new google.visualization.arrayToDataTable([\n" +
            "          ###TABLE_PERCENTILES###\n" +
            "        ]);\n" +
            "\n" +
            "        var optionsPercentile = {\n" +
            "          title: 'Percentiles',\n" +
            "          width: 1200,\n" +
            "          legend: { position: 'none' },\n" +
            "          chart: { subtitle: 'response time in ###BU_SHORT### per percentile' },\n" +
            "          axes: {\n" +
            "            x: {\n" +
            "              0: { label: 'percentile'}\n" +
            "            }\n" +
            "          },\n" +
            "          bar: { groupWidth: \"90%\" },\n" +
		    "          colors: ['green']\n" +
            "        };\n" +
            "\n" +
            "        var chartPercentiles = new google.visualization.ColumnChart(document.getElementById('chart_div_percentiles'));\n" +
            "        chartPercentiles.draw(dataPercentiles, optionsPercentile);\n" +
            "\n" +
            "        var optionsHisto = {\n" +
            "          title: 'Histogram',\n" +
            "          width: 1200,\n" +
            "          legend: { position: 'none' },\n" +
            "          chart: { subtitle: 'Histogram of response times in ###BU_SHORT###' },\n" +
            "          axes: {\n" +
            "             x: {\n" +
            "               0: { label: 'bucket in ###BU_SHORT###'}\n" +
            "             }\n" +
            "          },\n" +
            "          bar: { groupWidth: \"90%\" },\n" +
            "          colors: ['orange']\n" +
            "        };\n" +
            "\n" +
            "        var dataHisto = google.visualization.arrayToDataTable([\n" +
            "          ###TABLE_HISTO###\n" +
            "        ]);\n" +
            "\n" +
            "        var chartHisto = new google.visualization.ColumnChart(document.getElementById('chart_div_histo'));\n" +
            "        chartHisto.draw(dataHisto, optionsHisto);" +
            "      }\n" +
            "    </script>\n" +
            "  </head>\n" +
            "  <body>\n" +
            "    <div id=\"chart_div_response_times\" style=\"width: 1200px; height: 600px; margin-left: auto; margin-right: auto\"></div>\n" +
            "    <div id=\"chart_div_tps\" style=\"width: 1200px; height: 600px; margin-left: auto; margin-right: auto\"></div>\n" +
            "    <div id=\"chart_div_percentiles\" style=\"width: 1200px; height: 600px; margin-left: auto; margin-right: auto\"></div>\n" +
            "    <div id=\"chart_div_histo\" style=\"width: 1200px; height: 600px; margin-left: auto; margin-right: auto\"></div>\n" +
            "  </body>\n" +
            "</html>";

    public static ChartFile writeHtmlGoogleGraphFile(File dir, ResponseTimeAnalyser analyser, BaseUnit baseUnit) {

        String title = analyser.getCounterKey();
        List<MetricPoint> points = analyser.metricPoints();

        String template = CHART_HTML_TEMPLATE;

	    int size = points.size();
	    if (size < 3) {
	        String message = String.format("Not enough points ([%d]<3) to create html graph for [%s].", size, title);
            log.warn(message);
            template = "<p>" + message + "<p>";
        }
        else {
            int vMax = (int) analyser.percentileHitDuration(99) * 5;
            template = createResponseTimesTable(title, points, template, baseUnit, vMax);

            template = createHitsPerSecondTable(title, points, template);

            template = createPercentileColumnChart(title, analyser.percentiles(), template, baseUnit);

	        int numberOfRanges = Math.min((int)analyser.totalHits(), 100);
	        template = createHistoTable(title, analyser.histogramForRelevantValues(numberOfRanges), template, baseUnit);
        }

        String filename = FileUtils.createFilenameWithTimestampFromPathOrUrl(title, ".html");
        File graphFile = writeChartToHtmlFile(dir, "js-graph-" + filename, template);

        return new ChartFile(title, title, graphFile, ChartFile.ChartType.HTML);
    }

    private static String createPercentileColumnChart(String title, long[] percentiles, String template, BaseUnit baseUnit) {
        StringBuilder table = new StringBuilder();
        table.append("['Percentile', 'Response ").append(baseUnit.shortName()).append("'],\n");
        // skip 100th percentile to avoid extremes and no visible percentiles
        for (int i = 1; i < percentiles.length; i++) {
            table.append("[ ").append(i).append( ", ");
            table.append(percentiles[i - 1]).append("]");
            table.append(",").append("\n");
        }
        // remove last ,
        table.deleteCharAt(table.length() - 2);

	    return PATTERN_TABLE_PERCENTILES.matcher(template).replaceFirst(table.toString());
    }

    private static String createResponseTimesTable(String title, List<MetricPoint> points, String template, BaseUnit baseUnit, int vMax) {
        StringBuilder table = new StringBuilder();
        table.append("['Time', 'average', '95 percentile', '99 percentile', 'max'],\n");
        for (MetricPoint point : points) {
            table.append("[ new Date(").append(point.getTimestamp()).append("), ");
            table.append(point.getAverageResponseTimeMillis()).append(", ");
            table.append(point.getPercentile95ResponseTimeMillis()).append(", ");
            table.append(point.getPercentile99ResponseTimeMillis()).append(", ");
            table.append(point.getMaxResponseTimeMillis()).append("]");
            table.append(",").append("\n");
        }
        // remove last ,
        table.deleteCharAt(table.length() - 2);

	    template = PATTERN_TABLE.matcher(template).replaceFirst(Matcher.quoteReplacement(table.toString()));
        template = PATTERN_TITLE.matcher(template).replaceFirst(Matcher.quoteReplacement(title + " response times"));
        template = PATTERN_H_AXIS_TITLE.matcher(template).replaceFirst("time");
        template = PATTERN_V_AXIS_TITLE.matcher(template).replaceFirst(Matcher.quoteReplacement("response time in " + baseUnit.fullName()));
        template = PATTERN_BASE_UNIT_FULL.matcher(template).replaceAll(Matcher.quoteReplacement(baseUnit.fullName()));
        template = PATTERN_BASE_UNIT_SHORT.matcher(template).replaceAll(Matcher.quoteReplacement(baseUnit.shortName()));
        template = PATTERN_V_AXIS_MAX.matcher(template).replaceAll(String.valueOf(vMax));
        return template;
    }

    private static String createHitsPerSecondTable(String title, List<MetricPoint> points, String template) {
        DecimalFormat nfThreeDecimals = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        nfThreeDecimals.applyPattern("#0.000");

        StringBuilder table2 = new StringBuilder();
        table2.append("['Time', 'hits per second'],\n");
        for (MetricPoint point : points) {
            table2.append("[ new Date(").append(point.getTimestamp()).append("), ");
            table2.append(nfThreeDecimals.format(point.getHitsPerSecond())).append("]");
            table2.append(",").append("\n");
        }
        // remove last ,
        table2.deleteCharAt(table2.length() - 2);

        template = PATTERN_TABLE2.matcher(template).replaceFirst(table2.toString());
        template = PATTERN_TITLE2.matcher(template).replaceFirst(Matcher.quoteReplacement(title + " hits per second"));
        template = PATTERN_H_AXIS_TITLE2.matcher(template).replaceFirst("time");
        template = PATTERN_V_AXIS_TITLE2.matcher(template).replaceFirst("hits per second");
        return template;
    }

    private static String createHistoTable(String title, HistogramData points, String template, BaseUnit baseUnit) {

        DecimalFormat nfThousands = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        nfThousands.applyPattern("#,##0");

        StringBuilder tableHisto = new StringBuilder();
        tableHisto.append("['DurationBucket', 'count'],\n");
        long previousTime = 0;
        long previousCount = 0;
        double[] xValues = points.getXvalues();
        double[] yValues = points.getYvalues();
        long numberOfPoints = xValues.length;
        boolean first = true;
        for (int i = 0; i < numberOfPoints; i++) {
            long currentTime = (long) xValues[i];
            long count = (long) yValues[i];
            if (!first) {
                // check: for low ms times the ranges were 1 off (e.g. '2 to 3 ms' was actually '1 to 2 ms')
                String bucketName = nfThousands.format(previousTime) + " to " + nfThousands.format(currentTime) + " " + baseUnit.shortName();
                tableHisto.append("[ '").append(bucketName).append("', ");
                tableHisto.append(previousCount).append("]");
                tableHisto.append(",").append("\n");
            }
            previousTime = currentTime;
            previousCount = count;
            first = false;
        }
        String bucketName = previousTime + " and higher " + baseUnit.shortName();
        tableHisto.append("[ '").append(bucketName).append("', ");
        tableHisto.append(previousCount).append("]");

	    return PATTERN_TABLE_HISTO.matcher(template).replaceFirst(tableHisto.toString());
    }

    private static File writeChartToHtmlFile(File dir, String filename, String html) {
        File chartFile = new File(dir, filename);

        try {
            PrintWriter writer = new PrintWriter(chartFile, "UTF-8");
            writer.println(html);
            writer.close();
        } catch (Exception ex) {
            log.error("Could not write chart to file: " + filename, ex);
        }
        return chartFile;
    }

}
