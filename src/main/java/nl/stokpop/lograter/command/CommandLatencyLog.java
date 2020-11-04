package nl.stokpop.lograter.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Parse a log file that contains latency numbers.")
public class CommandLatencyLog extends AbstractCommandBasic {

	private static final String COMMANDNAME = "latency";

    public CommandLatencyLog() {
        logPattern = "%d;%X{sessionId};%X{service};%X{operation};%X{latency}%n";
    }

	@Parameter(names = { "-cf", "--counter-fields" }, description = "Counter fields to use for counting. Comma separated list of field names.")
	public String counterFields = "service,operation";

	@Parameter(names = { "-latency-field" }, description = "Field used for latency. Also specify the latency unit!")
	public String latencyField = "latency";

    @Parameter(names = { "-latency-unit" }, description = "Unit used for latency: seconds, milliseconds, microseconds, nanoseconds. Default is milliseconds.")
    public LatencyUnit latencyUnit = LatencyUnit.milliseconds;

    @Override
    public String toString() {
        return "CommandLatencyLog{" +
                ", counterFields='" + counterFields + '\'' +
                ", latencyField='" + latencyField + '\'' +
                ", latencyUnit='" + latencyUnit + '\'' +
                "} " + super.toString();
    }

	@Override
	public String getCommandName() {
		return COMMANDNAME;
	}

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
