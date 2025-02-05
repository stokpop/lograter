/*
 * Copyright (C) 2025 Peter Paul Bakker, Stokpop
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
import info.monitorenter.gui.chart.IRangePolicy;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.labelformatters.LabelFormatterDate;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.util.Range;
import nl.stokpop.lograter.LogRater;
import nl.stokpop.lograter.LogRaterException;
import nl.stokpop.lograter.util.FileUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AbstractGraphCreator {
    private static final Logger log = LoggerFactory.getLogger(AbstractGraphCreator.class);

	private static final DateTimeFormatter STANDARD_DATE_FORMATTER = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss");
	public static final String STANDARD_TIME_FORMAT_STRING = "yyyyMMddTHHmmss";

	public static File writeChartToPngFile(File dir, String name, Chart2D chart) {
        String filename = FileUtils.createFilenameWithTimestampFromPathOrUrl(name, ".png");
        File chartFile = new File(dir, filename);

        try {
            BufferedImage bi = chart.snapShot();
            ImageIO.write(bi, "PNG", chartFile);
        } catch (Exception ex) {
            log.error("Could not write chart to file: " + filename, ex);
        }
        return chartFile;
    }

    protected ITrace2D toITrace2Dpercentile(long[] percentiles, ITrace2D trace) {
		
		for (int i = 0; i < percentiles.length; i++) {
			trace.addPoint(i+1, percentiles[i]);
		}
			
		return trace;
	}

	protected static Chart2D createChart2D(String name) {
		Chart2D chart = new Chart2D();
	    chart.setUseAntialiasing(true);
	    chart.setName(name);
	    chart.setSize(1200, 500);
		return chart;
	}

	public AbstractGraphCreator() {
		super();
	}

	protected ITrace2D toITrace2D(Map<Long, Long> histogram, ITrace2D trace) {

		List<Long> list = new ArrayList<>(histogram.keySet());
		Collections.sort(list);
		
		for (Long period : list) {
			trace.addPoint(period, histogram.get(period));
		}
			
		return trace;
		
	}

	protected ITrace2D toITrace2D(double[] xvalues, double[] yvalues, ITrace2D trace) {
		
		if (xvalues.length != yvalues.length) {
            throw new LogRaterException("x and y arrays not of same length: " + xvalues.length + " " + yvalues.length);
        }
		
		for (int i = 0; i < yvalues.length; i++) {
			trace.addPoint(xvalues[i], yvalues[i]);
		}
		return trace;
		
	}
	
	public String formatTimeToStandardDateString(long time) {
	    return STANDARD_DATE_FORMATTER.print(time);
	}

	protected ITrace2D createTimeTrace(String counterName, Chart2D chart, Color lineColor, String yAxisLabel, long startTime, long endTime) {

        // Create an ITrace:
        ITrace2D trace = new Trace2DSimple();
        trace.setName(counterName);
        trace.setColor(lineColor);

        // We want to use a date format for the x axis.
        // Currently works only this way:
        IAxis<?> xAxis = chart.getAxisX();
        IAxis<?> yAxis = chart.getAxisY();

        // Set a date formatter
		@SuppressWarnings("PMD.AvoidSimpleDateFormat") // SimpleDateFormat is needed by API
		final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss", LogRater.DEFAULT_LOCALE);
        xAxis.setFormatter(new LabelFormatterDate(dateFormat));
        xAxis.setAxisTitle(new AxisTitle("absolute time"));
        xAxis.setPaintScale(true);

        IRangePolicy rangePolicy = new RangePolicyFixedViewport(new Range(startTime, endTime));
        chart.getAxisX().setRangePolicy(rangePolicy);

        xAxis.setMajorTickSpacing(60 * 1000 * 10);

        yAxis.setAxisTitle(new AxisTitle(yAxisLabel));
        yAxis.setPaintGrid(true);

        return trace;
    }

}