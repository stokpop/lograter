package nl.stokpop.lograter.feeder;

import nl.stokpop.lograter.LogRaterException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class InputStreamFeeder implements FeedProcessor {

    private final String sourceName;
    private final InputStream inputStream;
    private final int skipLines;

    public InputStreamFeeder(String sourceName, InputStream inputStream, int skipLines) {
        this.sourceName = sourceName;
        this.inputStream = inputStream;
        this.skipLines = skipLines;
    }

    public InputStreamFeeder(String sourceName, InputStream inputStream) {
        this(sourceName, inputStream, 0);
    }

    public void feed(Feeder feeder) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            br.lines().skip(skipLines).forEach(logLine -> feeder.addLogLine(sourceName, logLine));
        } catch (IOException e) {
            throw new LogRaterException("Cannot process InputStream for " + sourceName, e);
        }
    }
}