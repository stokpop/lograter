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
    
Download the executable jar here: TODO
    
## Dependency

To use LogRater from maven or gradle, find the lograter jar in maven central.


