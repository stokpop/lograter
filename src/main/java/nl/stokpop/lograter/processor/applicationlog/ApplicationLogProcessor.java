/*
 * Copyright (C) 2022 Peter Paul Bakker, Stokpop
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
package nl.stokpop.lograter.processor.applicationlog;

import nl.stokpop.lograter.logentry.LogbackLogEntry;
import nl.stokpop.lograter.processor.Processor;
import nl.stokpop.lograter.util.StringUtils;
import nl.stokpop.lograter.util.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationLogProcessor implements Processor<LogbackLogEntry> {

	private static final Logger log = LoggerFactory.getLogger(ApplicationLogProcessor.class);

	private ApplicationLogData data = new ApplicationLogData();
	
	private TimePeriod timePeriodFilter;

	public ApplicationLogProcessor(ApplicationLogConfig config) {
		this.timePeriodFilter = config.getFilterPeriod();
	}

	public ApplicationLogData getData() {
		return data;
	}

	@Override
	public void processEntry(LogbackLogEntry entry) {
		
		long timestamp = entry.getTimestamp();
		
		if (!timePeriodFilter.isWithinTimePeriod(timestamp)) {
			log.debug("Not in time filter range (number {}), skip: {}", data.getFilteredLines(), entry);
			data.incFilteredLines();
			return;
		}

		data.updateLogTime(timestamp);
		data.incTotalCharacters(entry.getLogline() == null ? 0 : entry.getLogline().length() + entry.getNonLogLinesLength());
		data.incLogLines();
		data.incNonLogLines(entry.getNrOfNonLoglines());
		
		final String className = entry.getClassName();
        final String logLevel = StringUtils.isEmptyString(entry.getLogLevel()) ? "Unknown" : entry.getLogLevel();
        final String loggerName = entry.getLogger();
        final String message = entry.getMessage();
        final String threadName = entry.getThreadName();
        final String appName = entry.getField("application");
        final String serviceName = entry.getField("service");

		// fall back to class name
		final String loggerOrClassName = StringUtils.isEmptyString(loggerName) ? className : loggerName;

        final String fullLoggerName = prependAppOrServiceName(threadName, appName, serviceName, loggerOrClassName);

        if ("FATAL".equals(logLevel)) {
			data.addFatal(fullLoggerName, timestamp);
		}
		else if ("ERROR".equals(logLevel)) {
			data.addError(fullLoggerName, timestamp);
		}
		else if ("WARN".equals(logLevel)) {
			data.addWarn(fullLoggerName, timestamp);
		}
		else if ("INFO".equals(logLevel)) {
			data.addInfo(fullLoggerName);
		}
		else if ("DEBUG".equals(logLevel)) {
			data.addDebug(fullLoggerName);
		}
		else if ("TRACE".equals(logLevel)) {
			data.addTrace(fullLoggerName);
		}

		ApplicationsLogDetailsKey key = new ApplicationsLogDetailsKey(fullLoggerName, logLevel);
		data.addDetails(key, message, entry.getNonLogLines());
	}

    private String prependAppOrServiceName(final String threadName, final String appName, final String serviceName, final String filteredClass) {
        final String kindOfThread;
	    if (threadName == null) {
		    kindOfThread = "-";
	    }
	    else if (threadName.contains("WebContainer")) {
            kindOfThread = "WebContainer";
        }
        else if (threadName.toLowerCase().contains("pool")) {
		    kindOfThread = "ThreadPool";
	    }
	    else if (threadName.toLowerCase().contains("quartz")) {
		    kindOfThread = "QuartzThread";
	    }
        else {
            kindOfThread = "Thread";
        }

        final String completeName;
	    if (!StringUtils.isEmptyString(appName) && !StringUtils.isEmptyString(serviceName)) {
			completeName = filteredClass + "[" + kindOfThread + ";" + appName + ";" + serviceName + "]";
		}
        else if (!StringUtils.isEmptyString(appName)) {
            completeName = filteredClass + "[" + kindOfThread + ";" + appName + "]";
        }
        else if (!StringUtils.isEmptyString(serviceName)) {
            completeName = filteredClass + "[" + kindOfThread + ";" + serviceName + "]";
        }
        else {
            completeName = filteredClass + "[" + kindOfThread + "]";
        }
        return completeName;
    }

}
