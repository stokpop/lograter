/*
 * Copyright (C) 2023 Peter Paul Bakker, Stokpop
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
package nl.stokpop.lograter.util.fit;

import nl.stokpop.lograter.util.CalculatorException;

import java.util.Arrays;

/**
 * Statistical 5 number summary.
 * See: https://web.archive.org/web/20190307200511/http://dsearls.org/other/CalculatingQuartiles/CalculatingQuartiles.htm
 */
public class FiveNumberSummary {

    private double minimum;
    private double firstQuartile;
    private double median;
    private double thirdQuartile;
    private double maximum;

    private FiveNumberSummary() {}

    public static FiveNumberSummary calculate(double[] values) throws CalculatorException {
        if (values.length < 3) {
            throw new CalculatorException("Supply at least 3 values. Now: " + values.length);
        }
        // make defensive copy: leave original matrix in same order
        double[] orderedDoubles = Arrays.copyOf(values, values.length);
        Arrays.sort(orderedDoubles);

        FiveNumberSummary fiveNumberSummary = new FiveNumberSummary();
        fiveNumberSummary.median = calculateMedian(orderedDoubles);
        fiveNumberSummary.firstQuartile = calculateFirstQuartile(orderedDoubles);
        fiveNumberSummary.thirdQuartile = calculateThirdQuartile(orderedDoubles);

        // remove outliers for min and max values
        setMinAndMaxUsingFences(fiveNumberSummary, orderedDoubles);

        return fiveNumberSummary;
    }

    public static void setMinAndMaxUsingFences(FiveNumberSummary fiveNumberSummary, double[] values) {
        fiveNumberSummary.minimum = Double.MAX_VALUE;
        fiveNumberSummary.maximum = Double.MIN_VALUE;
        for (double value : values) {
            if (value < fiveNumberSummary.minimum && value > fiveNumberSummary.getLowerFence()) {
                fiveNumberSummary.minimum = value;
            }
            if (value > fiveNumberSummary.maximum && value < fiveNumberSummary.getUpperFence()) {
                fiveNumberSummary.maximum = value;
            }
        }
    }

    public static double calculateMedian(double[] doubles) {
        boolean isEven = doubles.length % 2 == 0;
        double myMedian;
        if (isEven) {
            int middle1 = doubles.length / 2;
            int middle2 = middle1 + 1;
            myMedian = (doubles[middle1 - 1] + doubles[middle2 - 1]) / 2.0;
        }
        else {
            int middle = (doubles.length + 1) / 2;
            myMedian =  doubles[middle - 1];
        }
        return myMedian;
    }

    private static double calculateFirstQuartile(double[] doubles) {
        double firstQ = (doubles.length + 1)/ 4.0;
        int index1 = (int) Math.floor(firstQ);
        int index2 = (int) Math.ceil(firstQ);
        double fraction = firstQ - index1;
        double value1 = doubles[index1 - 1];
        double value2 = doubles[index2 - 1];
        return interpolate(value1, value2, fraction);
    }

    private static double interpolate(double value1, double value2, double fraction) {
        return value1 + fraction * (value2 - value1);
    }

    private static double calculateThirdQuartile(double[] doubles) {
        double thirdQ = ((doubles.length + 1) / 4.0) * 3;
        int index1 = (int) Math.floor(thirdQ);
        int index2 = (int) Math.ceil(thirdQ);
        double fraction = thirdQ - index1;
        double value1 = doubles[index1 - 1];
        double value2 = doubles[index2 - 1];
        return interpolate(value1, value2, fraction);
    }

    public double getMinimum() {
        return minimum;
    }

    public double getFirstQuartile() {
        return firstQuartile;
    }

    public double getMedian() {
        return median;
    }

    public double getThirdQuartile() {
        return thirdQuartile;
    }

    public double getMaximum() {
        return maximum;
    }

    public double getInterQuartileRange() {
        return thirdQuartile - firstQuartile;
    }

    public double getMaximumWhiskerLength() {
        return getInterQuartileRange() * 1.5;
    }

    public double getLowerFence() {
        return firstQuartile - getMaximumWhiskerLength();
    }

    public double getUpperFence() {
        return thirdQuartile + getMaximumWhiskerLength();
    }
}
