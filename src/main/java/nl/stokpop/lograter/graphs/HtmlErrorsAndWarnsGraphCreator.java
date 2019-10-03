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
package nl.stokpop.lograter.graphs;

import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.counter.RequestCounter;
import nl.stokpop.lograter.util.FileUtils;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Creates graph for errors and warns in application logs.
 * Create graphs with google graphs: https://google-developers.appspot.com/chart/
 */
public class HtmlErrorsAndWarnsGraphCreator {

	private static final Pattern PATTERN_CHART_SCRIPT_TEMPLATE = Pattern.compile("@CHART_SCRIPT_TEMPLATE");
	private static final Pattern PATTERN_CHART_DIV_TEMPLATE = Pattern.compile("@CHART_DIV_TEMPLATE");
	private static final Pattern PATTERN_CHART_JAVASCRIPT_TEMPLATE = Pattern.compile("@CHART_JAVASCRIPT_TEMPLATE");
	private static final Pattern PATTERN_TABLE = Pattern.compile("@TABLE");
	private static final Pattern PATTERN_TITLE = Pattern.compile("@TITLE");
	private static final Pattern PATTERN_H_AXIS_TITLE = Pattern.compile("@H_AXIS_TITLE");
	private static final Pattern PATTERN_V_AXIS_TITLE = Pattern.compile("@V_AXIS_TITLE");

	private static final Logger log = LoggerFactory.getLogger(HtmlErrorsAndWarnsGraphCreator.class);

	private final static String CHART_SCRIPT_TEMPLATE =
			"    <script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>\n" +
			"    <script type=\"text/javascript\">\n" +
            "    @CHART_JAVASCRIPT_TEMPLATE" +
			"    </script>\n";

    private final static String CHART_JAVASCRIPT_TEMPLATE =
            "      google.load(\"visualization\", \"1\", {packages:[\"corechart\", \"bar\"]});\n" +
			"      google.setOnLoadCallback(drawChart);\n" +
			"      function drawChart() {\n" +
			"        var dataErrors = google.visualization.arrayToDataTable([\n" +
			"          @TABLE\n" +
			"        ]);\n" +
			"\n" +
			"        var optionsErrors = {\n" +
			"          explorer: {},\n" +
			"          title: '@TITLE',\n" +
			"          hAxis: {title: '@H_AXIS_TITLE',  titleTextStyle: {color: '#333'}},\n" +
			"          vAxis: {title: '@V_AXIS_TITLE'}\n" +
			"        };\n" +
			"\n" +
			"        var chartErrors = new google.visualization.LineChart(document.getElementById('chart_div_errors'));\n" +
			"        chartErrors.draw(dataErrors, optionsErrors);\n" +
			"\n" +
			"      }\n";

	private final static String CHART_DIV_TEMPLATE =
			"    <div id=\"chart_div_errors\" style=\"width: 1200px; height: 600px; margin-left: auto; margin-right: auto\"></div>\n";

	private final static String CHART_HTML_TEMPLATE =
			"<html>\n" +
            "  <head>\n" +
			"@CHART_SCRIPT_TEMPLATE\n" +
            "  </head>\n"+
            "  <body>\n" +
			"@CHART_DIV_TEMPLATE\n" +
            "  </body>\n" +
            "</html>";

	private final static String CHART_EMBED_TEMPLATE =
			"@CHART_SCRIPT_TEMPLATE\n@CHART_DIV_TEMPLATE\n";

	public static Element createDocumentElement(Document doc, TimePeriod testPeriod, RequestCounter errorsOverTime, RequestCounter warnsOverTime, long bucketPeriodMillis) {

        Element parentDiv = doc.createElement("div");
        parentDiv.setAttribute("id", "chart_error_and_warns_div");

        String javaScript = createErrorsAndWarnsGraphJavaScript(testPeriod, errorsOverTime, warnsOverTime, bucketPeriodMillis);

        Element script = doc.createElement("script");
        script.setAttribute("type", "text/javascript");
        script.setAttribute("src", "https://www.google.com/jsapi");

        parentDiv.appendChild(script);

        Element scriptNode = doc.createElement("script");
        scriptNode.setAttribute("type", "text/javascript");
        scriptNode.setTextContent(javaScript);

        parentDiv.appendChild(scriptNode);

        Element div = doc.createElement("div");
        div.setAttribute("id", "chart_div_errors");
        div.setAttribute("style", "width: 1200px; height: 600px; margin-left: auto; margin-right: auto");

        parentDiv.appendChild(div);

        return parentDiv;
    }

	public static String createEmbeddedChartHtml(TimePeriod testPeriod, RequestCounter errorsOverTime, RequestCounter warnsOverTime, long bucketPeriodMillis) {
		String template = CHART_EMBED_TEMPLATE;
		template = PATTERN_CHART_SCRIPT_TEMPLATE.matcher(template).replaceFirst(CHART_SCRIPT_TEMPLATE);
		template = PATTERN_CHART_DIV_TEMPLATE.matcher(template).replaceFirst(CHART_DIV_TEMPLATE);
        String javaScript = createErrorsAndWarnsGraphJavaScript(testPeriod, errorsOverTime, warnsOverTime, bucketPeriodMillis);
        return PATTERN_CHART_JAVASCRIPT_TEMPLATE.matcher(template).replaceFirst(javaScript);
	}

	public static ChartFile createErrorAndWarningGraph(File reportDirectory, TimePeriod testPeriod, long bucketPeriodMillis, RequestCounter errorsOverTime, RequestCounter warnsOverTime) {
		String template = CHART_HTML_TEMPLATE;
		template = PATTERN_CHART_SCRIPT_TEMPLATE.matcher(template).replaceFirst(CHART_SCRIPT_TEMPLATE);
		template = PATTERN_CHART_DIV_TEMPLATE.matcher(template).replaceFirst(CHART_DIV_TEMPLATE);
        String javaScript = createErrorsAndWarnsGraphJavaScript(testPeriod, errorsOverTime, warnsOverTime, bucketPeriodMillis);
        template = PATTERN_CHART_JAVASCRIPT_TEMPLATE.matcher(template).replaceFirst(javaScript);
		return createApplicationLogGraphFile(reportDirectory, template);
	}

	private static ChartFile createApplicationLogGraphFile(File reportDir, String htmlPage) {
		File appLogGraphsDir = new File(reportDir, "application-log-graphs");
		if (!appLogGraphsDir.exists() && !appLogGraphsDir.mkdirs()) {
				throw new LogRaterException(String.format("Unable to create report directory [%s].", appLogGraphsDir.getAbsolutePath()));
		}
		String filename = FileUtils.createFilenameWithTimestampFromPathOrUrl("js-application-log-errors-graph-", ".html");
		File graphFile = writeChartToHtmlFile(reportDir, filename, htmlPage);
		return new ChartFile(filename, filename, graphFile, ChartFile.ChartType.HTML);
	}

	private static String createErrorsAndWarnsGraphJavaScript(TimePeriod testPeriod, RequestCounter errorsOverTime, RequestCounter warnsOverTime, long bucketPeriodMillis) {

		Map<Long, long[]> timestampsErrorsAndWarns = createTimestampToErrorsAndWarnsMap(errorsOverTime, warnsOverTime, testPeriod, bucketPeriodMillis);

        StringBuilder dataTable = new StringBuilder();
        dataTable.append("['Time', 'warns', 'errors'],\n");
        List<Long> timestamps = new ArrayList<>(timestampsErrorsAndWarns.keySet());
        Collections.sort(timestamps);
        for (Long timestamp : timestamps) {
            long[] errorsAndWarns = timestampsErrorsAndWarns.get(timestamp);
            if (errorsAndWarns.length != 2) {
                log.warn("Errors and warns long[] does not have two values (errors and warns count) but [{}] values.", errorsAndWarns.length);
            }
            else {
                dataTable.append("[ new Date(").append(timestamp).append("), ");
                dataTable.append(errorsAndWarns[1]).append(",");
                dataTable.append(errorsAndWarns[0]).append("]");
                dataTable.append(",").append("\n");
            }
        }
        // remove last comma
        dataTable.deleteCharAt(dataTable.length() - 2);

        TimePeriod duration = TimePeriod.createExcludingEndTime(0, bucketPeriodMillis);
        String template = CHART_JAVASCRIPT_TEMPLATE;
        template = PATTERN_TABLE.matcher(template).replaceFirst(dataTable.toString());
        template = PATTERN_TITLE.matcher(template).replaceFirst("errors and warnings per " + duration.getHumanReadableDuration());
        template = PATTERN_H_AXIS_TITLE.matcher(template).replaceFirst("time");
        template = PATTERN_V_AXIS_TITLE.matcher(template).replaceFirst("error and warn count");
        return template;
    }

    private static File writeChartToHtmlFile(File dir, String filename, String html) {
        File chartFile = new File(dir, filename);
        try {
            PrintWriter writer = new PrintWriter(chartFile, "UTF-8");
            writer.println(html);
            writer.close();
        } catch (Exception ex) {
            log.error("Could not write chart to file [{}].", filename, ex);
        }
        return chartFile;
    }

	private static Map<Long, long[]> createTimestampToErrorsAndWarnsMap(RequestCounter errorsOverTime, RequestCounter warnsOverTime, TimePeriod testPeriod, long bucketPeriodMillis) {
		Map<Long, long[]> timestampsErrorsAndWarns = new HashMap<>();
		if (errorsOverTime.getHits() > 0 || warnsOverTime.getHits() > 0) {
			TimePeriod errorsTimePeriod = errorsOverTime.getTimePeriod();
			TimePeriod warnsTimePeriod = warnsOverTime.getTimePeriod();
			TimePeriod totalErrorsAndWarnsTimePeriod = TimePeriod.createMaxTimePeriod(errorsTimePeriod, warnsTimePeriod);
			TimePeriod totalTestTimePeriod = TimePeriod.createMaxTimePeriod(testPeriod, totalErrorsAndWarnsTimePeriod);

			long thisIsTheTime = totalTestTimePeriod.getStartTime();

			long endOfTime = totalTestTimePeriod.getEndTime();
			while (thisIsTheTime < endOfTime) {
				TimePeriod thisIsThePeriod = TimePeriod.createExcludingEndTime(thisIsTheTime, thisIsTheTime + bucketPeriodMillis);
				RequestCounter slicedErrors = RequestCounter.safeSlicedCounter(errorsOverTime, thisIsThePeriod);
				RequestCounter slicedWarns = RequestCounter.safeSlicedCounter(warnsOverTime, thisIsThePeriod);
				long middleOfPeriod = thisIsTheTime + (bucketPeriodMillis / 2);
				timestampsErrorsAndWarns.put(middleOfPeriod, new long[]{slicedErrors.getHits(), slicedWarns.getHits()});
				thisIsTheTime = thisIsTheTime + bucketPeriodMillis;
			}
		}
		return timestampsErrorsAndWarns;
	}
}
