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
package nl.stokpop.lograter.graphs;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import nl.stokpop.lograter.util.FileUtils;
import nl.stokpop.lograter.util.fit.BestFitLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class GcGraphCreator extends AbstractGraphCreator {

    private final static Logger log = LoggerFactory.getLogger(GcGraphCreator.class);

    public File createHtmlChartFile(File dir, double[] xValues, double[] yValues, BestFitLine bestFitLine) {
		
		File graphFile = createGcGraph(dir, xValues, yValues, bestFitLine);
		
		String dateStr = formatTimeToStandardDateString(System.currentTimeMillis());
		
		File subDir = new File(dir, "gc-graphs-" + dateStr);
		if (!subDir.exists()) {
            boolean mkdirs = subDir.mkdirs();
            if (!mkdirs) {
                log.warn("unable to create sub dir: " + subDir);
            }
        }
		
		File gcGraphHtmlFile = new File(dir, "gc-graphs-" + dateStr + ".html");
	
		try (PrintWriter out = FileUtils.createBufferedPrintWriterWithUTF8(gcGraphHtmlFile)) {
            String relativeFileName = FileUtils.findRelativePath(dir, graphFile);
			out.println("<html>");
			out.println("<h3> Gc Graph with fit</h3>");
			out.println("<img src=\"" + relativeFileName.replace('\\', '/') + "\"/><br/>");
			out.println("</html>");
		} catch (IOException e){
			log.error("Problem writing gc graph file: " + gcGraphHtmlFile, e);
		}
		return gcGraphHtmlFile;
	}

	private File createGcGraph(File dir, double[] xValues, double[] yValues, BestFitLine bestFitLine) {
		String graphname = "Gc Fit";
		
		Chart2D chart = createChart2D("Gc Fit");
	    
	    Color lineColor = Color.GREEN;
	    Color lineColorFunction = Color.BLUE;

	    String yAxisLabel = "heap after global gc (MB)";
	    
	    long starttime = (long) xValues[0];
	    long endtime = (long) xValues[xValues.length-1];
	
	    ITrace2D trace = createTimeTrace(graphname, chart, lineColor, yAxisLabel, starttime, endtime);
		
	    // Add the trace to the chart. This has to be done before adding points 
		// (deadlock prevention): 
		chart.addTrace(trace);    

		ITrace2D traceFunction = createTimeTrace(graphname, chart, lineColorFunction, yAxisLabel, starttime, endtime);
	    chart.addTrace(traceFunction);
	    
	    double[] yValuesLine = new double[xValues.length];
	    
	    for (int i = 0; i < xValues.length; i++) {
			yValuesLine[i] = bestFitLine.calculateY(xValues[i]);
		}
	    
	    toITrace2D(xValues, yValues, trace);
	    toITrace2D(xValues, yValuesLine, traceFunction);
	    
		return writeChartToPngFile(dir, graphname, chart);	  
	    	    
	}

}
