## LogRater Release Notes

## version 1.5.5

March 2025

- Upgrade of dependencies
- Fixes use of external SQLite database to save total needed memory
  - JournalMode.OFF and SynchronousMode.OFF
  - Added missing index on measurement table
  - Use `nl.stokpop.lograter.store.TimeMeasurementStoreSqLite.BUFFER_SIZE` to change buffer size for SQLite counters (default 2048)
- Optimized regex matching of patterns, first checking for partial match before doing full match

## version 1.5.4

Feb 2025

- Enable storing counters to disk for latency and iis reports

## version 1.5.3

October 2024

- Fix for LoadRunner daylight savings time issue [#13](https://github.com/stokpop/lograter/issues/13)
- Update dependencies

Thanks to: [madkroll](https://github.com/madkroll) for the contribution.