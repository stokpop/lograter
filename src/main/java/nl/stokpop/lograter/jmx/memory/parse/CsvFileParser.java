package nl.stokpop.lograter.jmx.memory.parse;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import nl.stokpop.lograter.gc.GcLogParseException;
import nl.stokpop.lograter.jmx.memory.MemoryMetrics;
import nl.stokpop.lograter.jmx.memory.algorithm.GcAlgorithm;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.stream.Stream;

@Slf4j
public enum CsvFileParser {
    INSTANCE;

    private static final char SEPARATOR = ',';

    public Stream<MemoryMetrics> parse(File file) {
        Reader fileReader = getFileReader(file);

        GcAlgorithm gcAlgorithm = GcAlgorithmDetector.INSTANCE.detect(file);

        return new CsvToBeanBuilder<MemoryMetrics>(fileReader)
                .withType(gcAlgorithm.getBeanType())
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
            throw new GcLogParseException(errorMessage, e);
        }
    }
}