package nl.stokpop.lograter;

import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

public class LogRaterAsLibTest {

    public static final PrintWriter DEV_NULL_WRITER = new PrintWriter(new PrintStream(new OutputStream() {
        @Override
        public void write(int b) {
            // cat b > /dev/null
        }
    }));

    @Test
    public void lograterAsLib() throws IOException {
        LogRater logRater = new LogRater(DEV_NULL_WRITER);
        String[] args = {"-debug", "access", "src/test/resources/access-log/access.log"};
        // should not fail (fixed issue: NullPointerException on log field)
        logRater.startLogRater(args);
    }

}
