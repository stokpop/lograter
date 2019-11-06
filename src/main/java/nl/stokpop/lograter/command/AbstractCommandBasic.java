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
package nl.stokpop.lograter.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Lists;
import nl.stokpop.lograter.graphs.LogGraphCreator;

import java.util.List;

public abstract class AbstractCommandBasic extends LogRaterCommand {
	
	@Parameter(description = "<List of files to parse, or a file prefix to automatically load a set of files>")
	public List<String> files;
	
	@Parameter(names = {"-fffi", "--filefeederfilter-includes"},
            description = "Regular expression to use in the file feeder. Matches will be included. Matches are made on complete logline.")
	public String fileFeederFilterIncludes;

    @Parameter(names = {"-fffe", "--filefeederfilter-excludes"},
            description = "Regular expression to use in the file feeder. Matches will NOT be included. Matches are made on complete logline.")
    public String fileFeederFilterExcludes;

	@Parameter(names = {"-tps", "--report-transactions-per-second"},
            description = "Calculate and report transactions per second (next to TPM).")
	public boolean reportTPS = false;

	@Parameter(names = {"-sd", "--report-standard-dev"},
            description = "Calculate and report standard deviation of durations.")
	public boolean reportSD = false;

	@Parameter(names = {"-conc", "--report-concurrent-transactions"},
            description = "Calculate and report concurrent calls based on log time and duration. Adds a conc column in the report that shows the max concurrent requests for that particular url or request mapper.")
	public boolean reportConc = false;

	@Parameter(names = {"-ag", "--aggregate-duration"},
            description = "Aggregate graph values per time period in seconds. Aggregation kicks in for graphs with more than " +
			LogGraphCreator.GRAPH_AGGREGATION_CUTOFF_NR_HITS + " points. The graph name will tell that aggregation took place.")
	public int aggregateDurationInSeconds = 5;

	@Parameter(names = {"-gtps", "--graph-with-tps" },
            description = "Use true TPS per second in graphs instead of moving avg. (TPS per minute is default)")
	public boolean graphWithTrueTPS = false;

	@Parameter(names = {"-lp", "--log-pattern"},
            description = "The logback/httpd LogFormat pattern to use. ")
	public String logPattern;

	@Parameter(names = {"-gr", "--graphs-responsetimes" },
            description = "Create response times graphs")
	public boolean graphsResponseTimes = false;

	@Parameter(names = {"-gt", "--graphs-tps" },
            description = "Create TPS graphs")
	public boolean graphsTps = false;

	@Parameter(names = {"-gh", "--graphs-histo" },
            description = "Create histogram graphs")
	public boolean graphsHisto = false;

	@Parameter(names = {"-gp", "--graphs-percentile" },
            description = "Create percentile graphs")
	public boolean graphsPercentile = false;

    @Parameter(names = {"-ghtml", "--graphs-html" },
            description = "Output html google charts style graphs")
    public boolean graphsHtml = false;
    
    @Parameter(names = {"-clickpath" },
            description = "Determine and report click paths (BETA). Set sessionfield for the session id to use.")
    public boolean determineClickpaths = false;

    @Parameter(names = { "--clickpath-report-step-duration" },
            description = "Report the average duration between clickpath steps in millis.")
    public boolean clickpathReportStepDurations = false;

    @Parameter(names = { "--clickpath-end-of-session-snippet" },
            description = "Url's that contain this snippet are used as end of session marker (default: logout)")
    public String clickpathEndOfSessionSnippet = "logout";

    @Parameter(names = {"-session-duration" },
            description = "Determine the average session duration. Set sessionfield for the session id to use.")
	public boolean determineSessionDuration = false;

    @Parameter(names = {"-sessionfield" },
            description = "Name of the session field to use for clickpath and session duration analysis, from logpattern.")
    public String sessionField = null;

	@Parameter(names = { "-mf", "--mapper-file" },
			description = "Mapper file to use. Also used in clickpath analysis.")
	public String mapperFile;

	@Parameter(names = { "--max-no-mapper-count" },
            description = "Maximum number of unique counters before a mapper overflow is used to combine all non-matched mappers" +
            " (named NO_MAPPER_OVERFLOW).")
	public int maxNoMapperCount = 512;

	@Parameter(names = { "-sessionfield-regexp" },
			description = "Regexp to use to get the sessionId from the sessionField. Use a capture group () to specify the sessionId capture.")
	public String sessionFieldRegexp;

	@Parameter(names = { "--clickpath-short-code-length" },
            description = "Length of parts between slashes in clickpath urls, to shorten the path.")
	public int clickPathShortCodeLength = 3;

    @Parameter(names = {"-report-stub-delays" },
            description = "Add stub delay column settings in report.")
    public boolean reportStubDelays = false;

    @Parameter(names = {"-graphs-histo-simulator" },
            description = "If histo graphs are enabled, also merge a simulation of the histogram based on stub delay generator.")
    public boolean graphsHistoSimulator = false;

    @Parameter(names = {"-report-percentiles" },
            description = "List of percentiles to report. These are comma separated double values, for example: 99,99.9,99.995")
    public List<Double> reportPercentiles = Lists.newArrayList(99d);

    @Parameter(names = {"-failure-aware"}, arity = 1,
            description = "Be failure aware if possible. " +
                    "Report on failed hits in each analysis line. If not set the module defaults are used.")
    public Boolean failureAwareAnalysis = null;

    @Parameter(names = {"-include-failed-hits-in-analysis"}, arity = 1,
            description = "Include failed hits in analysis. " +
                    "When false the reported number of failures and failure percentage are the same for each counter, " +
                    "but the other calculated values such as min, max, tps, averaqe, percentiles will not include failed hits. " +
                    "\"Default behaviour can differ for different modules. Most have true, performance center analysis has false.")
    public Boolean includeFailedHitsInAnalysis = null;

    @Override
    public String toString() {
        return "AbstractCommandBasic{" +
                "files=" + files +
                ", fileFeederFilterIncludes='" + fileFeederFilterIncludes + '\'' +
                ", fileFeederFilterExcludes='" + fileFeederFilterExcludes + '\'' +
                ", reportTPS=" + reportTPS +
                ", reportSD=" + reportSD +
                ", reportConc=" + reportConc +
                ", aggregateDurationInSeconds=" + aggregateDurationInSeconds +
                ", graphWithTrueTPS=" + graphWithTrueTPS +
                ", logPattern='" + logPattern + '\'' +
                ", graphsResponseTimes=" + graphsResponseTimes +
                ", graphsTps=" + graphsTps +
                ", graphsHisto=" + graphsHisto +
                ", graphsPercentile=" + graphsPercentile +
                ", graphsHtml=" + graphsHtml +
                ", determineClickpaths=" + determineClickpaths +
                ", clickpathReportStepDurations=" + clickpathReportStepDurations +
                ", clickpathEndOfSessionSnippet=" + clickpathEndOfSessionSnippet +
                ", determineSessionDuration=" + determineSessionDuration +
                ", sessionField='" + sessionField + '\'' +
                ", mapperFile='" + mapperFile + '\'' +
                ", maxNoMapperCount=" + maxNoMapperCount +
                ", sessionFieldRegexp='" + sessionFieldRegexp + '\'' +
                ", clickPathShortCodeLength=" + clickPathShortCodeLength +
                ", reportStubDelays=" + reportStubDelays +
                ", graphsHistoSimulator=" + graphsHistoSimulator +
                ", reportPercentiles=" + reportPercentiles +
                ", failureAwareAnalysis=" + failureAwareAnalysis +
                ", includeFailuresInAnalysis=" + includeFailedHitsInAnalysis +
                '}';
    }
}