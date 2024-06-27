/*
 * Copyright (C) 2024 Peter Paul Bakker, Stokpop
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
package nl.stokpop.lograter.feeder;

import nl.stokpop.lograter.LogRaterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Chain multiple feeders to add additional functions to the chain.
 * Possible steps in the chains are filters or transformers.
 */
public abstract class ChainedFeeder implements Feeder {
    private static final Logger log = LoggerFactory.getLogger(ChainedFeeder.class);

    private Feeder next;
    private final String name;

    public ChainedFeeder(String name) {
        this.name = name;
    }

    public void addFeeder(Feeder feeder) {
        if (next == null) {
            next = feeder;
        }
        else {
            if (next instanceof ChainedFeeder) {
                ((ChainedFeeder) next).addFeeder(feeder);
            }
            else {
                String msg = String.format("Unable to add feeder %s to %s", feeder, this);
                throw new LogRaterException(msg);
            }
        }
    }

    @Override
    public void addLogLine(final String filename, final String logLine) {

        String nextLogLine = executeFeeder(filename, logLine);

        if (next != null && nextLogLine != null) {
            next.addLogLine(filename, nextLogLine);
        }
        else {
            if (next == null) {
                log.debug("No call from feeder {} to next feeder because there in no next feeder.", name);
            }
            else if (nextLogLine == null) {
                log.debug("No call to from feeder {} to next feeder {} because the executeFeeder call returned null.", name, next);
            }
        }
    }

    /**
     * Put the specific logic for this chained feeder in this method.
     *
     * @return the existing or a new (transformed) logLine, or null to avoid further calls up the chain
     */
    public abstract String executeFeeder(final String filename, final String logLine);

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ChainedFeeder{" + "name='" + name + '\'' + '}';
    }
}
