/*
 * Copyright (C) 2020 Peter Paul Bakker, Stokpop Software Solutions
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
package nl.stokpop.lograter;

import nl.stokpop.lograter.command.BaseUnit;

public class GraphConfig {

    private boolean graphsHistoEnabled = false;
    private boolean graphsHistoSimulatorEnabled = false;
    private boolean graphsPercentileEnabled = false;
    private boolean graphsResponseTimesEnabled = true;
    private boolean graphsTpsEnabled = true;
    private boolean graphWithTrueTPSEnabled = false;
    private boolean graphsHtmlEnabled = true;
    private int aggregateDurationInSeconds = 5;
    private BaseUnit baseUnit = BaseUnit.milliseconds;

    public boolean isGraphsHistoEnabled() {
        return graphsHistoEnabled;
    }

    public void setGraphsHistoEnabled(boolean graphsHistoEnabled) {
        this.graphsHistoEnabled = graphsHistoEnabled;
    }


    public boolean isGraphsHistoSimulatorEnabled() {
        return graphsHistoSimulatorEnabled;
    }

    public void setGraphsHistoSimulatorEnabled(boolean graphsHistoSimulatorEnabled) {
        this.graphsHistoSimulatorEnabled = graphsHistoSimulatorEnabled;
    }

    public boolean isGraphsPercentileEnabled() {
        return graphsPercentileEnabled;
    }

    public void setGraphsPercentileEnabled(boolean graphsPercentileEnabled) {
        this.graphsPercentileEnabled = graphsPercentileEnabled;
    }

    public boolean isGraphsResponseTimesEnabled() {
        return graphsResponseTimesEnabled;
    }

    public void setGraphsResponseTimesEnabled(boolean graphsResponseTimesEnabled) {
        this.graphsResponseTimesEnabled = graphsResponseTimesEnabled;
    }

    public boolean isGraphsTpsEnabled() {
        return graphsTpsEnabled;
    }

    public void setGraphsTpsEnabled(boolean graphsTpsEnabled) {
        this.graphsTpsEnabled = graphsTpsEnabled;
    }

    public boolean isGraphWithTrueTPSEnabled() {
        return graphWithTrueTPSEnabled;
    }

    public void setGraphWithTrueTPSEnabled(boolean graphWithTrueTPSEnabled) {
        this.graphWithTrueTPSEnabled = graphWithTrueTPSEnabled;
    }

    public int getAggregateDurationInSeconds() {
        return aggregateDurationInSeconds;
    }

    public void setAggregateDurationInSeconds(int aggregateDurationInSeconds) {
        this.aggregateDurationInSeconds = aggregateDurationInSeconds;
    }

    public boolean isGraphRequested() {
        return graphsHistoEnabled
                || graphsPercentileEnabled
                || graphsResponseTimesEnabled
                || graphsTpsEnabled
                || graphsHtmlEnabled;
    }

    public void setGraphsHtmlEnabled(boolean graphsHtmlEnabled) {
        this.graphsHtmlEnabled = graphsHtmlEnabled;
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        throw new UnsupportedOperationException();
    }

    public BaseUnit getBaseUnit() {
        return baseUnit;
    }

    public void setBaseUnit(BaseUnit baseUnit) {
        this.baseUnit = baseUnit;
    }
}
