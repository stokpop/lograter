package nl.stokpop.lograter.jmx.os.parse;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import nl.stokpop.lograter.jmx.os.JmxOperatingSystemMetrics;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.stream.Stream;


@Slf4j
public enum JmxOperatingSystemCsvFileParser {
    INSTANCE;

    private static final char SEPARATOR = ',';

    public Stream<JmxOperatingSystemMetrics> parse(File file) {
        Reader fileReader = getFileReader(file);

        return new CsvToBeanBuilder<JmxOperatingSystemMetrics>(fileReader)
                .withType(JmxOperatingSystemMetrics.class)
                .withIgnoreLeadingWhiteSpace(true)
                .withSeparator(SEPARATOR)
                .build()
                .stream();
    }

    private Reader getFileReader(File file) {
        try {
            return Files.newBufferedReader(file.toPath());
        } catch (IOException e) {
            String errorMessage = "Cannot open input file " + file;
            log.error(errorMessage);
            throw new JmxOperatingSystemCsvFileParseException(errorMessage, e);
        }
    }
}
