# LogRater

LogRater analyses performance behaviour of your applications and services.

Feed logs from your web and application servers to generate performance reports.

LogRater can be run stand-alone from the command line, or be included as jar in any Java project.

LogRater is sponsored by Rabobank.

# Uses

## Incoming requests

Get logs from your production servers and find out which requests have the highest hits per second.
For each request see the average response time and the percentiles. 

For each type of request, see how many concurrent requests there are.

The following web logs are supported:
* apache 
* nginx
* iis

## Outgoing requests

If logs are available for outgoing requests, you can find out how the same for these outgoing requests.

## Compare production to test

If you run load tests on a performance test environment, compare the reports to the reports of production
to check if the load test covers the same requests and behaves the same in response times as production.

## Automate analysis 

For automatic tests also automate the analysis of the rest results. 

## Map names

Use mappers to map technical requests (urls) to human readable names. Also use 
mappers to group urls that are the same actually the same request. 
For instance because the url contains a unique id that needs to be filtered.

## Load test logs

LogRater can make similar analysis for both JMeter logs and Performance Center result zip files.

## Application logs

For logback style Java application logging, LogRater can parse application logs to find
common errors and stacktraces. It also gives an indication of 'logging' pressure under load:
how many lines per minute are logged.

## CPU

When you have sar files from Linux servers, LogRater can determine the average CPU usage
and show CPU graphs. For instance during a load test.

## Time period

For most LogRater analysis you can specify a start and end time to zoom into the period that is
of interest. For example a very busy period on production or a period that is (part of) a load test.

## Create graphs

Show graphs per request type to easily identify issues or unexpected behaviour.
The following graphs are available: 

* response times over time
* load over time
* histogram of response times
* percentiles of response times

Both available as png and as interactive javascript/html graphs with popups. 

## Sessions 

When session identifiers are available in the logs, LogRater can determine session duration times
and also common paths that users follow through the application.

## Extend LogRater

For custom logs you can easily extend LogRater to parse these logs as well and
produce similar reports and graphs.
  
### Example run

To run from the command line, use the executable jar which contains all references.
For instance to parse apache access logs:

    java -jar lograter-exec.jar access access.log

To see all options:

    java -jar lograter-exec.jar -h
    
Download the executable jar here: https://github.com/stokpop/lograter/releases
    
## Dependency

To use LogRater from maven or gradle, find the lograter jar in maven central.

## Lograter command line options

    LogRater version: 1.3.3

    Use one of the following commands after the [options] below: iis, access, pc, application, alloc, gc, accessToCsv, jmeter
    Use --help [command] for command specific help.
    Usage: <main class> [options]
      Options:
        -et, --endtime
          The end time of filter: yyyyMMddTHHmmss
        -o, --output.file
          Write to this file. If not set writes to std out. Use {ts} to include a 
          timestamp in your filename.
        -r, --report.dir
          The directory for the reports (an html file with graphs in this 
          release). Use {ts} to include a timestamp in your directory. Defaults to 
          reports in current dir.
          Default: reports
        -st, --starttime
          The start time of filter: yyyyMMddTHHmmss
        -base-unit
          What base unit to use in reports. Choose from: milliseconds, 
          microseconds. Default is milliseconds.
          Default: milliseconds
          Possible Values: [microseconds, milliseconds]
        -clear-db
          Clear the database.
          Default: false
        -debug
          Print stacktraces with errors.
          Default: false
        -runid
          A run id to identify a test run with a report. Is displayed in reports.
          Default: 1b605c60-f571-4fe9-a6ae-00d5e9667fdd
        -storage
          The type of storage to use. Options: mem (in memory, default), 
          externalsort or database (in sqlite database)
          Default: Memory
          Possible Values: [Memory, Database, ExternalSort]
        -storage.dir
          Where to store files for externalsort or database (in sqlite database). 
          Defaults to working directory.
          Default: .
        -use-db
          Only use database input, skip file parsing.
          Default: false
    
    Usage: nl.stokpop.lograter.LogRater [options] [command] [command options]
      Options:
        -et, --endtime
          The end time of filter: yyyyMMddTHHmmss
        -o, --output.file
          Write to this file. If not set writes to std out. Use {ts} to include a 
          timestamp in your filename.
        -r, --report.dir
          The directory for the reports (an html file with graphs in this 
          release). Use {ts} to include a timestamp in your directory. Defaults to 
          reports in current dir.
          Default: reports
        -st, --starttime
          The start time of filter: yyyyMMddTHHmmss
        -base-unit
          What base unit to use in reports. Choose from: milliseconds, 
          microseconds. Default is milliseconds.
          Default: milliseconds
          Possible Values: [microseconds, milliseconds]
        -clear-db
          Clear the database.
          Default: false
        -debug
          Print stacktraces with errors.
          Default: false
        -runid
          A run id to identify a test run with a report. Is displayed in reports.
          Default: 0e1b16ba-e39d-49e6-b738-1dd38394d188
        -storage
          The type of storage to use. Options: mem (in memory, default), 
          externalsort or database (in sqlite database)
          Default: Memory
          Possible Values: [Memory, Database, ExternalSort]
        -storage.dir
          Where to store files for externalsort or database (in sqlite database). 
          Defaults to working directory.
          Default: .
        -use-db
          Only use database input, skip file parsing.
          Default: false
      Commands:
        iis      Parse a IIS log file.
          Usage: iis [options] <List of files to parse, or a file prefix to 
                automatically load a set of files>
            Options:
              -ag, --aggregate-duration
                Aggregate graph values per time period in seconds. Aggregation 
                kicks in for graphs with more than 10000 points. The graph name 
                will tell that aggregation took place.
                Default: 5
              --clickpath-end-of-session-snippet
                Url's that contain this snippet are used as end of session marker 
                (default: logout)
                Default: logout
              --clickpath-report-step-duration
                Report the average duration between clickpath steps in millis.
                Default: false
              --clickpath-short-code-length
                Length of parts between slashes in clickpath urls, to shorten the 
                path. 
                Default: 3
              -multi-hit, --count-multiple-hits-in-mapper
                Will count all hits in the mapper file, otherwise only the first 
                hit will be counted.
                Default: false
              -fffe, --filefeederfilter-excludes
                Regular expression to use in the file feeder. Matches will NOT be 
                included. Matches are made on complete logline.
              -fffi, --filefeederfilter-includes
                Regular expression to use in the file feeder. Matches will be 
                included. Matches are made on complete logline.
              -gtps, --graph-with-tps
                Use true TPS per second in graphs instead of moving avg. (TPS per 
                minute is default)
                Default: false
              -gh, --graphs-histo
                Create histogram graphs
                Default: false
              -ghtml, --graphs-html
                Output html google charts style graphs
                Default: false
              -gp, --graphs-percentile
                Create percentile graphs
                Default: false
              -gr, --graphs-responsetimes
                Create response times graphs
                Default: false
              -gt, --graphs-tps
                Create TPS graphs
                Default: false
              -imm, --ignore-multi-and-no-matches
                Ignore multi match and no match warnings. Do not display a list of 
                the no matches.
                Default: false
              -regexp, --include-mapper-regexp-column
                Include the mapper regexp column in the iis and access log report.
                Default: false
              -lp, --log-pattern
                The logback/httpd LogFormat pattern to use.
              -mf, --mapper-file
                Mapper file to use. Also used in clickpath analysis.
              --max-unique-counters
                Maximum number of unique counters before an overflow counter is 
                used that combines all further counters (named OVERFLOW_COUNTER).
                Default: 512
              -nompr, --nomappers
                Exclude mappers in access logs.
                Default: false
              -ref, --referers
                Include referers in iis and access logs.
                Default: false
              -rpu, --remove-params-from-url
                Remove the parameters before parsing the access log urls (split at 
                question mark)
                Default: false
              -conc, --report-concurrent-transactions
                Calculate and report concurrent calls based on log time and 
                duration. Adds a conc column in the report that shows the max 
                concurrent requests for that particular url or request mapper.
                Default: false
              -sd, --report-standard-dev
                Calculate and report standard deviation of durations.
                Default: false
              -tps, --report-transactions-per-second
                Calculate and report transactions per second (next to TPM).
                Default: false
              -ua, --useragents
                Include agents in iis and access logs.
                Default: false
              -clickpath
                Determine and report click paths (BETA). Set sessionfield for the 
                session id to use.
                Default: false
              -count-no-mapper-as-one
                Will count all no-mappers as one line, default count all 
                no-mappers separately.
                Default: false
              -failure-aware
                Be failure aware if possible. Report on failed hits in each 
                analysis line. If not set the module defaults are used.
              -graphs-histo-simulator
                If histo graphs are enabled, also merge a simulation of the 
                histogram based on stub delay generator.
                Default: false
              -group-by-fields
                Group by the given comma separated fields as specified in the used 
                logformat. Url will be the mapped url.
                Default: []
              -group-by-http-method
                Group by http method (POST, GET, ...) by adding it to the counter 
                name and adding a http method column in text report.
                Default: false
              -group-by-http-status
                Group by http status code by adding it to the counter name and 
                adding a http status column in text report.
                Default: false
              -include-failed-hits-in-analysis
                Include failed hits in analysis. When false the reported number of 
                failures and failure percentage are the same for each counter, but 
                the other calculated values such as min, max, tps, averaqe, 
                percentiles will not include failed hits. "Default behaviour can 
                differ for different modules. Most have true, performance center 
                analysis has false.
              -report-percentiles
                List of percentiles to report. These are comma separated double 
                values, for example: 99,99.9,99.995
                Default: [99.0]
              -report-stub-delays
                Add stub delay column settings in report.
                Default: false
              -session-duration
                Determine the average session duration. Set sessionfield for the 
                session id to use.
                Default: false
              -sessionfield
                Name of the session field to use for clickpath and session 
                duration analysis, from logpattern.
              -sessionfield-regexp
                Regexp to use to get the sessionId from the sessionField. Use a 
                capture group () to specify the sessionId capture.
              -urls
                Include basic urls in iis and access logs.
                Default: false
    
        access      Parse an access log file.
          Usage: access [options] <List of files to parse, or a file prefix to 
                automatically load a set of files>
            Options:
              -ag, --aggregate-duration
                Aggregate graph values per time period in seconds. Aggregation 
                kicks in for graphs with more than 10000 points. The graph name 
                will tell that aggregation took place.
                Default: 5
              --clickpath-end-of-session-snippet
                Url's that contain this snippet are used as end of session marker 
                (default: logout)
                Default: logout
              --clickpath-report-step-duration
                Report the average duration between clickpath steps in millis.
                Default: false
              --clickpath-short-code-length
                Length of parts between slashes in clickpath urls, to shorten the 
                path. 
                Default: 3
              -multi-hit, --count-multiple-hits-in-mapper
                Will count all hits in the mapper file, otherwise only the first 
                hit will be counted.
                Default: false
              -fffe, --filefeederfilter-excludes
                Regular expression to use in the file feeder. Matches will NOT be 
                included. Matches are made on complete logline.
              -fffi, --filefeederfilter-includes
                Regular expression to use in the file feeder. Matches will be 
                included. Matches are made on complete logline.
              -gtps, --graph-with-tps
                Use true TPS per second in graphs instead of moving avg. (TPS per 
                minute is default)
                Default: false
              -gh, --graphs-histo
                Create histogram graphs
                Default: false
              -ghtml, --graphs-html
                Output html google charts style graphs
                Default: false
              -gp, --graphs-percentile
                Create percentile graphs
                Default: false
              -gr, --graphs-responsetimes
                Create response times graphs
                Default: false
              -gt, --graphs-tps
                Create TPS graphs
                Default: false
              -imm, --ignore-multi-and-no-matches
                Ignore multi match and no match warnings. Do not display a list of 
                the no matches.
                Default: false
              -regexp, --include-mapper-regexp-column
                Include the mapper regexp column in the iis and access log report.
                Default: false
              -lp, --log-pattern
                The logback/httpd LogFormat pattern to use.
              -mf, --mapper-file
                Mapper file to use. Also used in clickpath analysis.
              --max-unique-counters
                Maximum number of unique counters before an overflow counter is 
                used that combines all further counters (named OVERFLOW_COUNTER).
                Default: 512
              -nompr, --nomappers
                Exclude mappers in access logs.
                Default: false
              -ref, --referers
                Include referers in iis and access logs.
                Default: false
              -rpu, --remove-params-from-url
                Remove the parameters before parsing the access log urls (split at 
                question mark)
                Default: false
              -conc, --report-concurrent-transactions
                Calculate and report concurrent calls based on log time and 
                duration. Adds a conc column in the report that shows the max 
                concurrent requests for that particular url or request mapper.
                Default: false
              -sd, --report-standard-dev
                Calculate and report standard deviation of durations.
                Default: false
              -tps, --report-transactions-per-second
                Calculate and report transactions per second (next to TPM).
                Default: false
              -ua, --useragents
                Include agents in iis and access logs.
                Default: false
              -clickpath
                Determine and report click paths (BETA). Set sessionfield for the 
                session id to use.
                Default: false
              -count-no-mapper-as-one
                Will count all no-mappers as one line, default count all 
                no-mappers separately.
                Default: false
              -failure-aware
                Be failure aware if possible. Report on failed hits in each 
                analysis line. If not set the module defaults are used.
              -graphs-histo-simulator
                If histo graphs are enabled, also merge a simulation of the 
                histogram based on stub delay generator.
                Default: false
              -group-by-fields
                Group by the given comma separated fields as specified in the used 
                logformat. Url will be the mapped url.
                Default: []
              -group-by-http-method
                Group by http method (POST, GET, ...) by adding it to the counter 
                name and adding a http method column in text report.
                Default: false
              -group-by-http-status
                Group by http status code by adding it to the counter name and 
                adding a http status column in text report.
                Default: false
              -include-failed-hits-in-analysis
                Include failed hits in analysis. When false the reported number of 
                failures and failure percentage are the same for each counter, but 
                the other calculated values such as min, max, tps, averaqe, 
                percentiles will not include failed hits. "Default behaviour can 
                differ for different modules. Most have true, performance center 
                analysis has false.
              -log-type
                Type of log file: apache (default) or nginx. Use apache logformat 
                (e.g. %t, %D, etc...) or use nginx style log format (e.g. with 
                $request, $status, ...
                Default: apache
                Possible Values: [apache, nginx]
              -report-percentiles
                List of percentiles to report. These are comma separated double 
                values, for example: 99,99.9,99.995
                Default: [99.0]
              -report-stub-delays
                Add stub delay column settings in report.
                Default: false
              -session-duration
                Determine the average session duration. Set sessionfield for the 
                session id to use.
                Default: false
              -sessionfield
                Name of the session field to use for clickpath and session 
                duration analysis, from logpattern.
              -sessionfield-regexp
                Regexp to use to get the sessionId from the sessionField. Use a 
                capture group () to specify the sessionId capture.
              -urls
                Include basic urls in iis and access logs.
                Default: false
    
        pc      Parse a performance center results database.
          Usage: pc [options] <List of files to parse, or a file prefix to 
                automatically load a set of files>
            Options:
              -ag, --aggregate-duration
                Aggregate graph values per time period in seconds. Aggregation 
                kicks in for graphs with more than 10000 points. The graph name 
                will tell that aggregation took place.
                Default: 5
              --clickpath-end-of-session-snippet
                Url's that contain this snippet are used as end of session marker 
                (default: logout)
                Default: logout
              --clickpath-report-step-duration
                Report the average duration between clickpath steps in millis.
                Default: false
              --clickpath-short-code-length
                Length of parts between slashes in clickpath urls, to shorten the 
                path. 
                Default: 3
              -fffe, --filefeederfilter-excludes
                Regular expression to use in the file feeder. Matches will NOT be 
                included. Matches are made on complete logline.
              -fffi, --filefeederfilter-includes
                Regular expression to use in the file feeder. Matches will be 
                included. Matches are made on complete logline.
              -gtps, --graph-with-tps
                Use true TPS per second in graphs instead of moving avg. (TPS per 
                minute is default)
                Default: false
              -gh, --graphs-histo
                Create histogram graphs
                Default: false
              -ghtml, --graphs-html
                Output html google charts style graphs
                Default: false
              -gp, --graphs-percentile
                Create percentile graphs
                Default: false
              -gr, --graphs-responsetimes
                Create response times graphs
                Default: false
              -gt, --graphs-tps
                Create TPS graphs
                Default: false
              -lp, --log-pattern
                The logback/httpd LogFormat pattern to use.
              -mf, --mapper-file
                Mapper file to use. Also used in clickpath analysis.
              --max-unique-counters
                Maximum number of unique counters before an overflow counter is 
                used that combines all further counters (named OVERFLOW_COUNTER).
                Default: 512
              -conc, --report-concurrent-transactions
                Calculate and report concurrent calls based on log time and 
                duration. Adds a conc column in the report that shows the max 
                concurrent requests for that particular url or request mapper.
                Default: false
              -sd, --report-standard-dev
                Calculate and report standard deviation of durations.
                Default: false
              -tps, --report-transactions-per-second
                Calculate and report transactions per second (next to TPM).
                Default: false
              -clickpath
                Determine and report click paths (BETA). Set sessionfield for the 
                session id to use.
                Default: false
              -failure-aware
                Be failure aware if possible. Report on failed hits in each 
                analysis line. If not set the module defaults are used.
              -graphs-histo-simulator
                If histo graphs are enabled, also merge a simulation of the 
                histogram based on stub delay generator.
                Default: false
              -include-failed-hits-in-analysis
                Include failed hits in analysis. When false the reported number of 
                failures and failure percentage are the same for each counter, but 
                the other calculated values such as min, max, tps, averaqe, 
                percentiles will not include failed hits. "Default behaviour can 
                differ for different modules. Most have true, performance center 
                analysis has false.
              -report-percentiles
                List of percentiles to report. These are comma separated double 
                values, for example: 99,99.9,99.995
                Default: [99.0]
              -report-stub-delays
                Add stub delay column settings in report.
                Default: false
              -session-duration
                Determine the average session duration. Set sessionfield for the 
                session id to use.
                Default: false
              -sessionfield
                Name of the session field to use for clickpath and session 
                duration analysis, from logpattern.
              -sessionfield-regexp
                Regexp to use to get the sessionId from the sessionField. Use a 
                capture group () to specify the sessionId capture.
    
        application      Parse an (Java logback) application log file.
          Usage: application [options] <List of files to parse, or a file prefix 
                to automatically load a set of files>
            Options:
              -ag, --aggregate-duration
                Aggregate graph values per time period in seconds. Aggregation 
                kicks in for graphs with more than 10000 points. The graph name 
                will tell that aggregation took place.
                Default: 5
              --clickpath-end-of-session-snippet
                Url's that contain this snippet are used as end of session marker 
                (default: logout)
                Default: logout
              --clickpath-report-step-duration
                Report the average duration between clickpath steps in millis.
                Default: false
              --clickpath-short-code-length
                Length of parts between slashes in clickpath urls, to shorten the 
                path. 
                Default: 3
              -fffe, --filefeederfilter-excludes
                Regular expression to use in the file feeder. Matches will NOT be 
                included. Matches are made on complete logline.
              -fffi, --filefeederfilter-includes
                Regular expression to use in the file feeder. Matches will be 
                included. Matches are made on complete logline.
              -gtps, --graph-with-tps
                Use true TPS per second in graphs instead of moving avg. (TPS per 
                minute is default)
                Default: false
              -gh, --graphs-histo
                Create histogram graphs
                Default: false
              -ghtml, --graphs-html
                Output html google charts style graphs
                Default: false
              -gp, --graphs-percentile
                Create percentile graphs
                Default: false
              -gr, --graphs-responsetimes
                Create response times graphs
                Default: false
              -gt, --graphs-tps
                Create TPS graphs
                Default: false
              -lp, --log-pattern
                The logback/httpd LogFormat pattern to use.
              -mf, --mapper-file
                Mapper file to use. Also used in clickpath analysis.
              --max-unique-counters
                Maximum number of unique counters before an overflow counter is 
                used that combines all further counters (named OVERFLOW_COUNTER).
                Default: 512
              -conc, --report-concurrent-transactions
                Calculate and report concurrent calls based on log time and 
                duration. Adds a conc column in the report that shows the max 
                concurrent requests for that particular url or request mapper.
                Default: false
              -sd, --report-standard-dev
                Calculate and report standard deviation of durations.
                Default: false
              -tps, --report-transactions-per-second
                Calculate and report transactions per second (next to TPM).
                Default: false
              -clickpath
                Determine and report click paths (BETA). Set sessionfield for the 
                session id to use.
                Default: false
              -failure-aware
                Be failure aware if possible. Report on failed hits in each 
                analysis line. If not set the module defaults are used.
              -graphs-histo-simulator
                If histo graphs are enabled, also merge a simulation of the 
                histogram based on stub delay generator.
                Default: false
              -include-failed-hits-in-analysis
                Include failed hits in analysis. When false the reported number of 
                failures and failure percentage are the same for each counter, but 
                the other calculated values such as min, max, tps, averaqe, 
                percentiles will not include failed hits. "Default behaviour can 
                differ for different modules. Most have true, performance center 
                analysis has false.
              -report-percentiles
                List of percentiles to report. These are comma separated double 
                values, for example: 99,99.9,99.995
                Default: [99.0]
              -report-stub-delays
                Add stub delay column settings in report.
                Default: false
              -session-duration
                Determine the average session duration. Set sessionfield for the 
                session id to use.
                Default: false
              -sessionfield
                Name of the session field to use for clickpath and session 
                duration analysis, from logpattern.
              -sessionfield-regexp
                Regexp to use to get the sessionId from the sessionField. Use a 
                capture group () to specify the sessionId capture.
    
        alloc      Parse WebSphere application server logs with large allocation 
                traces. 
          Usage: alloc [options] List of files to parse. Normally the 
                native_stderr.log of Websphere Application Server.
            Options:
              --csv-file
                Csv file to write output to. Defaults to 
                large-allocations-log-{ts}.csv in current dir.
                Default: large-allocations-log-{ts}.csv
    
        gc      Parse WebSphere verbose garbage collection log files.
          Usage: gc [options] List of gc verbose files to parse. Websphere 
                Application Server 7 or 8 style supported. OpenJ9 gc verbose might 
                work too.
            Options:
              -et-analysis, --endtime-analysis
                The end time of analysis period: yyyyMMddTHHmmss
              -et-memory-fit, --endtime-memory-fit
                The end time of memory fit period: yyyyMMddTHHmmss
              --report-file
                Text file to write output to. Defaults to 
                verbose-gc-report-{ts}.txt in current dir.
                Default: verbose-gc-report-{ts}.txt
              -st-analysis, --starttime-analysis
                The start time of analysis period: yyyyMMddTHHmmss
              -st-memory-fit, --starttime-memory-fit
                The start time of memory fit period: yyyyMMddTHHmmss
    
        accessToCsv      Transform an access log to a csv file.
          Usage: accessToCsv [options] <List of files to parse, or a file prefix 
                to automatically load a set of files>
            Options:
              -ag, --aggregate-duration
                Aggregate graph values per time period in seconds. Aggregation 
                kicks in for graphs with more than 10000 points. The graph name 
                will tell that aggregation took place.
                Default: 5
              --clickpath-end-of-session-snippet
                Url's that contain this snippet are used as end of session marker 
                (default: logout)
                Default: logout
              --clickpath-report-step-duration
                Report the average duration between clickpath steps in millis.
                Default: false
              --clickpath-short-code-length
                Length of parts between slashes in clickpath urls, to shorten the 
                path. 
                Default: 3
              --csv-file
                Csv file to write output to. Defaults to access-log-{ts}.csv in 
                current dir.
                Default: access-log-{ts}.csv
              -fffe, --filefeederfilter-excludes
                Regular expression to use in the file feeder. Matches will NOT be 
                included. Matches are made on complete logline.
              -fffi, --filefeederfilter-includes
                Regular expression to use in the file feeder. Matches will be 
                included. Matches are made on complete logline.
              -gtps, --graph-with-tps
                Use true TPS per second in graphs instead of moving avg. (TPS per 
                minute is default)
                Default: false
              -gh, --graphs-histo
                Create histogram graphs
                Default: false
              -ghtml, --graphs-html
                Output html google charts style graphs
                Default: false
              -gp, --graphs-percentile
                Create percentile graphs
                Default: false
              -gr, --graphs-responsetimes
                Create response times graphs
                Default: false
              -gt, --graphs-tps
                Create TPS graphs
                Default: false
              -lp, --log-pattern
                The logback/httpd LogFormat pattern to use.
              -mf, --mapper-file
                Mapper file to use. Also used in clickpath analysis.
              --max-unique-counters
                Maximum number of unique counters before an overflow counter is 
                used that combines all further counters (named OVERFLOW_COUNTER).
                Default: 512
              -conc, --report-concurrent-transactions
                Calculate and report concurrent calls based on log time and 
                duration. Adds a conc column in the report that shows the max 
                concurrent requests for that particular url or request mapper.
                Default: false
              -sd, --report-standard-dev
                Calculate and report standard deviation of durations.
                Default: false
              -tps, --report-transactions-per-second
                Calculate and report transactions per second (next to TPM).
                Default: false
              -clickpath
                Determine and report click paths (BETA). Set sessionfield for the 
                session id to use.
                Default: false
              -failure-aware
                Be failure aware if possible. Report on failed hits in each 
                analysis line. If not set the module defaults are used.
              -graphs-histo-simulator
                If histo graphs are enabled, also merge a simulation of the 
                histogram based on stub delay generator.
                Default: false
              -include-failed-hits-in-analysis
                Include failed hits in analysis. When false the reported number of 
                failures and failure percentage are the same for each counter, but 
                the other calculated values such as min, max, tps, averaqe, 
                percentiles will not include failed hits. "Default behaviour can 
                differ for different modules. Most have true, performance center 
                analysis has false.
              -report-percentiles
                List of percentiles to report. These are comma separated double 
                values, for example: 99,99.9,99.995
                Default: [99.0]
              -report-stub-delays
                Add stub delay column settings in report.
                Default: false
              -session-duration
                Determine the average session duration. Set sessionfield for the 
                session id to use.
                Default: false
              -sessionfield
                Name of the session field to use for clickpath and session 
                duration analysis, from logpattern.
              -sessionfield-regexp
                Regexp to use to get the sessionId from the sessionField. Use a 
                capture group () to specify the sessionId capture.
    
        jmeter      Parse jtl file from a jMeter run.
          Usage: jmeter [options] <List of files to parse, or a file prefix to 
                automatically load a set of files>
            Options:
              -ag, --aggregate-duration
                Aggregate graph values per time period in seconds. Aggregation 
                kicks in for graphs with more than 10000 points. The graph name 
                will tell that aggregation took place.
                Default: 5
              --clickpath-end-of-session-snippet
                Url's that contain this snippet are used as end of session marker 
                (default: logout)
                Default: logout
              --clickpath-report-step-duration
                Report the average duration between clickpath steps in millis.
                Default: false
              --clickpath-short-code-length
                Length of parts between slashes in clickpath urls, to shorten the 
                path. 
                Default: 3
              -multi-hit, --count-multiple-hits-in-mapper
                Will count all hits in the mapper file, otherwise only the first 
                hit will be counted.
                Default: false
              -fffe, --filefeederfilter-excludes
                Regular expression to use in the file feeder. Matches will NOT be 
                included. Matches are made on complete logline.
              -fffi, --filefeederfilter-includes
                Regular expression to use in the file feeder. Matches will be 
                included. Matches are made on complete logline.
              -gtps, --graph-with-tps
                Use true TPS per second in graphs instead of moving avg. (TPS per 
                minute is default)
                Default: false
              -gh, --graphs-histo
                Create histogram graphs
                Default: false
              -ghtml, --graphs-html
                Output html google charts style graphs
                Default: false
              -gp, --graphs-percentile
                Create percentile graphs
                Default: false
              -gr, --graphs-responsetimes
                Create response times graphs
                Default: false
              -gt, --graphs-tps
                Create TPS graphs
                Default: false
              -imm, --ignore-multi-and-no-matches
                Ignore multi match and no match warnings. Do not display a list of 
                the no matches.
                Default: false
              -regexp, --include-mapper-regexp-column
                Include the mapper regexp column in the iis and access log report.
                Default: false
              -lp, --log-pattern
                The logback/httpd LogFormat pattern to use.
              -mf, --mapper-file
                Mapper file to use. Also used in clickpath analysis.
              --max-unique-counters
                Maximum number of unique counters before an overflow counter is 
                used that combines all further counters (named OVERFLOW_COUNTER).
                Default: 512
              -nompr, --nomappers
                Exclude mappers in access logs.
                Default: false
              -ref, --referers
                Include referers in iis and access logs.
                Default: false
              -rpu, --remove-params-from-url
                Remove the parameters before parsing the access log urls (split at 
                question mark)
                Default: false
              -conc, --report-concurrent-transactions
                Calculate and report concurrent calls based on log time and 
                duration. Adds a conc column in the report that shows the max 
                concurrent requests for that particular url or request mapper.
                Default: false
              -sd, --report-standard-dev
                Calculate and report standard deviation of durations.
                Default: false
              -tps, --report-transactions-per-second
                Calculate and report transactions per second (next to TPM).
                Default: false
              -ua, --useragents
                Include agents in iis and access logs.
                Default: false
              -clickpath
                Determine and report click paths (BETA). Set sessionfield for the 
                session id to use.
                Default: false
              -count-no-mapper-as-one
                Will count all no-mappers as one line, default count all 
                no-mappers separately.
                Default: false
              -failure-aware
                Be failure aware if possible. Report on failed hits in each 
                analysis line. If not set the module defaults are used.
              -graphs-histo-simulator
                If histo graphs are enabled, also merge a simulation of the 
                histogram based on stub delay generator.
                Default: false
              -group-by-fields
                Group by the given comma separated fields as specified in the used 
                logformat. Url will be the mapped url.
                Default: []
              -group-by-http-method
                Group by http method (POST, GET, ...) by adding it to the counter 
                name and adding a http method column in text report.
                Default: false
              -group-by-http-status
                Group by http status code by adding it to the counter name and 
                adding a http status column in text report.
                Default: false
              -include-failed-hits-in-analysis
                Include failed hits in analysis. When false the reported number of 
                failures and failure percentage are the same for each counter, but 
                the other calculated values such as min, max, tps, averaqe, 
                percentiles will not include failed hits. "Default behaviour can 
                differ for different modules. Most have true, performance center 
                analysis has false.
              -report-percentiles
                List of percentiles to report. These are comma separated double 
                values, for example: 99,99.9,99.995
                Default: [99.0]
              -report-stub-delays
                Add stub delay column settings in report.
                Default: false
              -session-duration
                Determine the average session duration. Set sessionfield for the 
                session id to use.
                Default: false
              -sessionfield
                Name of the session field to use for clickpath and session 
                duration analysis, from logpattern.
              -sessionfield-regexp
                Regexp to use to get the sessionId from the sessionField. Use a 
                capture group () to specify the sessionId capture.
              -urls
                Include basic urls in iis and access logs.
                Default: false

   
