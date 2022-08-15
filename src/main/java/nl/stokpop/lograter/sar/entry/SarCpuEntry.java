/*
 * Copyright (C) 2022 Peter Paul Bakker, Stokpop
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.stokpop.lograter.sar.entry;

/**
 * Holds CPU info from SAR file.
 */
public class SarCpuEntry extends SarEntry {

    private final String CPU;
    private final double user;
    private final double system;
    private final double iowait;
    private final double idle;
    private final double nice;
    private final double steal;
    private final SarCpuExtended extendedValues;

    public static class SarCpuExtended {

        public static final SarCpuExtended NO_EXTENDED_VALUES = new SarCpuExtended(0, 0, 0, 0);

        private double irq;
        private double soft;
        private double guest;
        private double gnice;

        @Override
        public String toString() {
            return "SarCpuExtended{" +
                    "irq=" + irq +
                    ", soft=" + soft +
                    ", guest=" + guest +
                    ", gnice=" + gnice +
                    '}';
        }

        public SarCpuExtended(double irq, double soft, double guest, double gnice) {
            this.irq = irq;
            this.soft = soft;
            this.guest = guest;
            this.gnice = gnice;
        }

        public double getIrq() {
            return irq;
        }

        public double getSoft() {
            return soft;
        }

        public double getGuest() {
            return guest;
        }

        public double getGnice() {
            return gnice;
        }
    }

    public SarCpuEntry(long timestamp, String CPU, double usr, double nice, double system, double iowait, double steal, double irq, double soft, double guest, double gnice, double idle) {
        super(timestamp);
        this.CPU = CPU;
        this.user = usr;
        this.nice = nice;
        this.system = system;
        this.iowait = iowait;
        this.steal = steal;
        this.idle = idle;
        this.extendedValues = new SarCpuExtended(irq, soft, guest, gnice);

        double total = usr + nice + system + iowait + steal + irq + soft + guest + + gnice + idle;
        if (isNotCloseToOneHundred(total)) {
            throw new SarParserException(String.format("Total CPU does not add up to 100%%: %.2f", total));
        }
    }

    public SarCpuEntry(long timestamp, String CPU, double user, double nice, double system, double iowait, double steal, double idle) {
        super(timestamp);
        this.CPU = CPU;
        this.user = user;
        this.nice = nice;
        this.system = system;
        this.iowait = iowait;
        this.steal = steal;
        this.idle = idle;
        this.extendedValues = SarCpuExtended.NO_EXTENDED_VALUES;

        double total = user + nice + system + iowait + steal + idle;
        if (isNotCloseToOneHundred(total)) {
            throw new SarParserException(String.format("Total CPU does not add up to 100%%: %.2f", total));
        }
    }

    public boolean isNotCloseToOneHundred(double total) {
        return !(total >= 99.8) || !(total <= 100.2);
    }

    public boolean hasExtendedValues() {
        return this.extendedValues != SarCpuExtended.NO_EXTENDED_VALUES;
    }

    public SarCpuExtended getExtendedValues() {
        return this.extendedValues;
    }

    public String getCPU() {
        return CPU;
    }

    public double getUser() {
        return user;
    }

    public double getNice() {
        return nice;
    }

    public double getSystem() {
        return system;
    }

    public double getIowait() {
        return iowait;
    }

    public double getSteal() {
        return steal;
    }

    public double getIdle() {
        return idle;
    }

    public double getNonIdle() {
        return 100.0 - idle;
    }

    @Override
    public String toString() {
        return "SarCpuEntry{" +
                super.toString() +
                ", CPU='" + CPU + '\'' +
                ", user=" + user +
                ", nice=" + nice +
                ", system=" + system +
                ", iowait=" + iowait +
                ", steal=" + steal +
                ", idle=" + idle +
                ", extendedValues=" + extendedValues +
                '}';
    }

	@Override
	public boolean equals(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}
}
