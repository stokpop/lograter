package nl.stokpop.lograter.gc.jmx.parse;

import lombok.extern.slf4j.Slf4j;
import nl.stokpop.lograter.gc.GcLogParseException;
import nl.stokpop.lograter.gc.jmx.algorithm.GcAlgorithm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

@Slf4j
public enum GcAlgorithmDetector {
    INSTANCE;

    public GcAlgorithm detect(File file) {
        String header = readHeaderLine(file);
        return findGcAlgorithm(header, file);
    }

    private GcAlgorithm findGcAlgorithm(String header, File file) {
        Optional<GcAlgorithm> gcAlgorithm = GcAlgorithm.forPattern(header);
        if (!gcAlgorithm.isPresent()) {
            String errorMessage = "Header line '" + header + "' of file " + file + " does not match with existing patterns.";
            log.error(errorMessage);
            throw new GcLogParseException(errorMessage);
        }

        return gcAlgorithm.get();
    }

    private String readHeaderLine(File file) {
        Optional<String> headerLine;
        try {
            headerLine = Files.lines(file.toPath()).findFirst();
            if (!headerLine.isPresent()) {
                String errorMessage = "File " + file + "is empty.";
                log.error(errorMessage);
                throw new GcLogParseException(errorMessage);
            }
        } catch (IOException e) {
            String errorMessage = "Error while reading file " + file ;
            log.error(errorMessage);
            throw new GcLogParseException(errorMessage, e);
        }
        return headerLine.get();
    }
}