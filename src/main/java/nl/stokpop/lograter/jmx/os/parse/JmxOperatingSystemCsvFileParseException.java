package nl.stokpop.lograter.jmx.os.parse;

import nl.stokpop.lograter.LogRaterException;

public class JmxOperatingSystemCsvFileParseException extends LogRaterException {

    public JmxOperatingSystemCsvFileParseException(String message) {
        super(message);
    }

    public JmxOperatingSystemCsvFileParseException(String message, Exception e) {
        super(message, e);
    }

}
