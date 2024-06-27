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
package nl.stokpop.lograter.util.fit;

import nl.stokpop.lograter.util.CalculatorException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class FiveNumberSummaryTest {

    public static final double SMALL_DOUBLE = 0.0001d;

    @Test
    public void testCalculate() {
        double[] values = new double[] { 2, 2, 5, 8, 8, 11, 14, 18, 19, 25, 26, 27 };

        FiveNumberSummary fiveNumberSummary = FiveNumberSummary.calculate(values);

        assertEquals(2, fiveNumberSummary.getMinimum(), SMALL_DOUBLE);
        assertEquals(5.75, fiveNumberSummary.getFirstQuartile(), SMALL_DOUBLE);
        assertEquals(12.5, fiveNumberSummary.getMedian(), SMALL_DOUBLE);
        assertEquals(23.5, fiveNumberSummary.getThirdQuartile(), SMALL_DOUBLE);
        assertEquals(27, fiveNumberSummary.getMaximum(), SMALL_DOUBLE);
        assertEquals(17.75, fiveNumberSummary.getInterQuartileRange(), SMALL_DOUBLE);

    }

    @Test
    public void testCalculate2() {
        double[] values = new double[] { 10.2, 14.1, 14.4, 14.4,  14.4,  14.5,  14.5,  14.6,  14.7, 14.7, 14.7, 14.9, 15.1, 15.9, 16.4 };

        FiveNumberSummary fiveNumberSummary = FiveNumberSummary.calculate(values);

        assertEquals(14.1, fiveNumberSummary.getMinimum(), SMALL_DOUBLE);
        assertEquals(14.4, fiveNumberSummary.getFirstQuartile(), SMALL_DOUBLE);
        assertEquals(14.6, fiveNumberSummary.getMedian(), SMALL_DOUBLE);
        assertEquals(14.9, fiveNumberSummary.getThirdQuartile(), SMALL_DOUBLE);
        assertEquals(15.1, fiveNumberSummary.getMaximum(), SMALL_DOUBLE);
        assertEquals(0.5, fiveNumberSummary.getInterQuartileRange(), SMALL_DOUBLE);

    }

    @Test
    public void testCalculate3() {
        // https://www.mathsisfun.com/data/quartiles.html
        double[] values = new double[] { 5, 8, 4, 4, 6, 3, 8 };

        FiveNumberSummary fiveNumberSummary = FiveNumberSummary.calculate(values);

        assertEquals(3, fiveNumberSummary.getMinimum(), SMALL_DOUBLE);
        assertEquals(4, fiveNumberSummary.getFirstQuartile(), SMALL_DOUBLE);
        assertEquals(5, fiveNumberSummary.getMedian(), SMALL_DOUBLE);
        assertEquals(8, fiveNumberSummary.getThirdQuartile(), SMALL_DOUBLE);
        assertEquals(8, fiveNumberSummary.getMaximum(), SMALL_DOUBLE);
        assertEquals(4, fiveNumberSummary.getInterQuartileRange(), SMALL_DOUBLE);

    }

    @Test
    public void testCalculate4() {
        // https://www.mathsisfun.com/data/quartiles.html
        double[] values = new double[] { 4, 17, 7, 14, 18, 12, 3, 16, 10, 4, 4, 11 };

        FiveNumberSummary fiveNumberSummary = FiveNumberSummary.calculate(values);

        assertEquals(3, fiveNumberSummary.getMinimum(), SMALL_DOUBLE);
        assertEquals(4, fiveNumberSummary.getFirstQuartile(), SMALL_DOUBLE);
        assertEquals(10.5, fiveNumberSummary.getMedian(), SMALL_DOUBLE);
        // on website it is 15, but here it is interpolated.
        assertEquals(15.5, fiveNumberSummary.getThirdQuartile(), SMALL_DOUBLE);
        assertEquals(18, fiveNumberSummary.getMaximum(), SMALL_DOUBLE);
        assertEquals(11.5, fiveNumberSummary.getInterQuartileRange(), SMALL_DOUBLE);

    }

    @Test
    public void testCalculateMini() {

        double[] values = new double[] { 4 };

        try {
            FiveNumberSummary fiveNumberSummary = FiveNumberSummary.calculate(values);
        } catch (CalculatorException e) {
            return;
        }
        fail("Expected an exception for too small a data set");

    }

    @Test
    public void testCalculateMini2() {

        double[] values = new double[] { 1, 2, 3, 4 };

        FiveNumberSummary fiveNumberSummary = FiveNumberSummary.calculate(values);

        assertEquals(1, fiveNumberSummary.getMinimum(), SMALL_DOUBLE);
        assertEquals(1.25, fiveNumberSummary.getFirstQuartile(), SMALL_DOUBLE);
        assertEquals(2.5, fiveNumberSummary.getMedian(), SMALL_DOUBLE);
        assertEquals(3.75, fiveNumberSummary.getThirdQuartile(), SMALL_DOUBLE);
        assertEquals(4, fiveNumberSummary.getMaximum(), SMALL_DOUBLE);
        assertEquals(2.5, fiveNumberSummary.getInterQuartileRange(), SMALL_DOUBLE);

    }

    @Test
    public void testCalculateMini3() {

        double[] values = new double[] {1, 100, 101};

        FiveNumberSummary fiveNumberSummary = FiveNumberSummary.calculate(values);

        assertEquals(1, fiveNumberSummary.getMinimum(), SMALL_DOUBLE);
        assertEquals(1, fiveNumberSummary.getFirstQuartile(), SMALL_DOUBLE);
        assertEquals(100, fiveNumberSummary.getMedian(), SMALL_DOUBLE);
        assertEquals(101, fiveNumberSummary.getThirdQuartile(), SMALL_DOUBLE);
        assertEquals(101, fiveNumberSummary.getMaximum(), SMALL_DOUBLE);
        assertEquals(100, fiveNumberSummary.getInterQuartileRange(), SMALL_DOUBLE);
        assertEquals(-149, fiveNumberSummary.getLowerFence(), SMALL_DOUBLE);
        assertEquals(251, fiveNumberSummary.getUpperFence(), SMALL_DOUBLE);

    }

    @Test
    public void testCalculateMini4() {

        // need at least 5 numbers to get an outlier?
        double[] values = new double[] {1, 1, 1, 1, 1, 10000};

        FiveNumberSummary fiveNumberSummary = FiveNumberSummary.calculate(values);

        assertEquals(1, fiveNumberSummary.getMinimum(), SMALL_DOUBLE);
        assertEquals(1, fiveNumberSummary.getFirstQuartile(), SMALL_DOUBLE);
        assertEquals(1, fiveNumberSummary.getMedian(), SMALL_DOUBLE);
        assertEquals(2500.75, fiveNumberSummary.getThirdQuartile(), SMALL_DOUBLE);
        // should the outliers be removed? Yes to draw a proper BoxPlot...
        assertEquals(1, fiveNumberSummary.getMaximum(), SMALL_DOUBLE);
        assertEquals(2499.75, fiveNumberSummary.getInterQuartileRange(), SMALL_DOUBLE);
        assertEquals(-3748.625, fiveNumberSummary.getLowerFence(), SMALL_DOUBLE);
        assertEquals(6250.375, fiveNumberSummary.getUpperFence(), SMALL_DOUBLE);

    }

    @Test
    public void testCalculateFences() {
        // https://www2.southeastern.edu/Academics/Faculty/dgurney/Math241/StatTopics/BoxGen.htm
        double[] values = new double[] {79, 47, 55, 83, 145, 44, 61, 18, 78, 62};

        FiveNumberSummary fiveNumberSummary = FiveNumberSummary.calculate(values);

        // here, the numbers are a bit different than website, maybe due to interpolation?
        // this is more like it: http://www.alcula.com/calculators/statistics/quartiles/
        assertEquals(18, fiveNumberSummary.getMinimum(), SMALL_DOUBLE);
        assertEquals(46.25, fiveNumberSummary.getFirstQuartile(), SMALL_DOUBLE);
        assertEquals(61.5, fiveNumberSummary.getMedian(), SMALL_DOUBLE);
        assertEquals(80, fiveNumberSummary.getThirdQuartile(), SMALL_DOUBLE);
        assertEquals(83, fiveNumberSummary.getMaximum(), SMALL_DOUBLE);

        assertEquals(33.75, fiveNumberSummary.getInterQuartileRange(), SMALL_DOUBLE);
        assertEquals(50.625, fiveNumberSummary.getMaximumWhiskerLength(), SMALL_DOUBLE);
        assertEquals(-4.375, fiveNumberSummary.getLowerFence(), SMALL_DOUBLE);
        assertEquals(130.625, fiveNumberSummary.getUpperFence(), SMALL_DOUBLE);

    }

}