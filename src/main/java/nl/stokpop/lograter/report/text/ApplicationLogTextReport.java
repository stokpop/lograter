/*
 * Copyright (C) 2024 Peter Paul Bakker, Stokpop
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
package nl.stokpop.lograter.report.text;

import nl.stokpop.lograter.counter.SimpleCounter;
import nl.stokpop.lograter.processor.applicationlog.ApplicationLogConfig;
import nl.stokpop.lograter.processor.applicationlog.ApplicationLogData;
import nl.stokpop.lograter.processor.applicationlog.ApplicationLogDetails;
import nl.stokpop.lograter.processor.applicationlog.ApplicationsLogDetailsKey;
import nl.stokpop.lograter.util.StringUtils;
import nl.stokpop.lograter.util.time.TimePeriod;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ApplicationLogTextReport extends LogTextReport {
	
	private ApplicationLogData data;
	private ApplicationLogConfig config;
	
	public ApplicationLogTextReport(ApplicationLogData data, ApplicationLogConfig config) {
		this.data = data;
		this.config = config;
	}

	public void report(PrintWriter out, TimePeriod analysisPeriod) {
		// TODO is it necessary to use analysis period?! Might already filtered when reading the data?

		long total = data.getTotalLogLines(); 

		out.println(reportSummaryHeader(data, config));
	
		out.println("Total chars              : " + data.getTotalCharacters() + " or approx. " 
		+ this.nfTwoDecimals.format(data.getTotalMB())  + " MB");
	
		StringBuilder report = new StringBuilder();
		double durationInSec = data.getLogTimePeriod().getDurationInSeconds();
		double charactersPerSecond = data.getTotalCharacters() / durationInSec;
		report.append("Total chars/sec          : ").append(nfTwoDecimals.format(charactersPerSecond)).append(" or approx. ").append(nfTwoDecimals.format(charactersPerSecond / 1024)).append(" kB").append("\n");
		report.append("\n");
		report.append("Nr of Fatal              : ").append(data.getNrOfFatals()).append("\n");
		report.append("Nr of Error              : ").append(data.getNrOfErrors()).append("\n");
		report.append("Nr of Warn               : ").append(data.getNrOfWarns()).append("\n");
		report.append("Nr of Info               : ").append(data.getNrOfInfos()).append("\n");
		report.append("Nr of Debug              : ").append(data.getNrOfDebugs()).append("\n");
		report.append("Nr of Trace              : ").append(data.getNrOfTraces()).append("\n");
		report.append("Nr of nonLog             : ").append(data.getNonLogLines()).append("\n");
		report.append("Total                    : ").append(total).append("\n");
		report.append("\n");
		report.append("Rate Fatal (l/s)         : ").append(nfTwoDecimals.format(data.getNrOfFatals() / durationInSec)).append("\n");
		report.append("Rate Error (l/s)         : ").append(nfTwoDecimals.format(data.getNrOfErrors() / durationInSec)).append("\n");
		report.append("Rate Warn (l/s)          : ").append(nfTwoDecimals.format(data.getNrOfWarns() / durationInSec)).append("\n");
		report.append("Rate Info (l/s)          : ").append(nfTwoDecimals.format(data.getNrOfInfos() / durationInSec)).append("\n");
		report.append("Rate Debug (l/s)         : ").append(nfTwoDecimals.format(data.getNrOfDebugs() / durationInSec)).append("\n");
		report.append("Rate Trace (l/s)         : ").append(nfTwoDecimals.format(data.getNrOfTraces() / durationInSec)).append("\n");
		report.append("Rate NonLog (l/s)        : ").append(nfTwoDecimals.format(data.getNonLogLines() / durationInSec)).append("\n");
		report.append("Total (l/s, ex NonLog)   : ").append(nfTwoDecimals.format(total / durationInSec)).append("\n");
		report.append("\n");
		
		Map<String,SimpleCounter> sortedFatals = sortMap(data.getFatals());
		Map<String,SimpleCounter> sortedErrors = sortMap(data.getErrors());
		Map<String,SimpleCounter> sortedWarns = sortMap(data.getWarns());
		Map<String,SimpleCounter> sortedInfos = sortMap(data.getInfos());
		Map<String,SimpleCounter> sortedDebugs = sortMap(data.getDebugs());
		Map<String,SimpleCounter> sortedTraces = sortMap(data.getTraces());
		
		report.append("== FATALS ==").append("\n");
		listMapInReport(report, sortedFatals);
		report.append("\n");
	
		report.append("== ERRORS ==").append("\n");
		listMapInReport(report, sortedErrors);
		report.append("\n");
		
		report.append("== WARNS ==").append("\n");
		listMapInReport(report, sortedWarns);
		report.append("\n");
		
		report.append("== INFOS ==").append("\n");
		listMapInReport(report, sortedInfos);
		report.append("\n");
		
		report.append("== DEBUGS ==").append("\n");
		listMapInReport(report, sortedDebugs);
		report.append("\n");
	
		report.append("== TRACES ==").append("\n");
		listMapInReport(report, sortedTraces);
		report.append("\n");

		report.append("== Details top 20 unique per type ==");
		report.append("\n");

		Map<ApplicationLogDetails, SimpleCounter> countPerLogDetails = data.getCountPerLogDetails();

		for (ApplicationsLogDetailsKey key : data.applicationsLogDetailsKeys()) {
			List<ApplicationLogDetails> detailsList = data.findApplicationLogDetails(key);
			report.append("\n-- ").append(key.getLogLevel()).append(" -- ").append(key.getFullLoggerName()).append(" --");
			report.append("\n");
			for (ApplicationLogDetails details : detailsList) {
				long count = countPerLogDetails.get(details).getCount();
				report.append("Message (").append(count).append("): ");
				report.append(details.getMessage());
				report.append("\n");

				String nonLogLines = details.getDetails();
				if (!StringUtils.isEmptyString(nonLogLines)) {
					report.append(">> Details:");
					report.append("\n");
					report.append(nonLogLines);
					report.append("\n");
				}
			}
		}
		report.append("-- details end --");

		out.println(report.toString());
					
	}

	private static class ValueComparator implements Comparator<String> {    
		private Map<String, SimpleCounter> base;   
		public ValueComparator(Map<String, SimpleCounter> base) {
			this.base = base;
		}
		public int compare(String a, String b) {
			int compare = this.base.get(b).compareTo(this.base.get(a));
			if (compare == 0) {
				return a.compareTo(b);
			}
			return compare;
		} 
	}

	private Map<String, SimpleCounter> sortMap(Map<String, SimpleCounter> mapToSort) {
		ValueComparator bvc =  new ValueComparator(mapToSort);
		TreeMap<String, SimpleCounter> sorted_map = new TreeMap<>(bvc);
		sorted_map.putAll(mapToSort);
		return sorted_map;
	}
	
	private static void listMapInReport(StringBuilder report, Map<String, SimpleCounter> sortedMap) {
		for (Map.Entry<String, SimpleCounter> entry : sortedMap.entrySet()) {
			report.append(entry.getValue().getCount()).append(";").append(entry.getKey()).append("\n");
		}
	}

}
