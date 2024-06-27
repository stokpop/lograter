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
package nl.stokpop.lograter.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CalculatorTest {

    @Test
    public void testCalculateCoefficientOfDetermination1()  {
        double[] values = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        double[] fitValues = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        double coefficientOfDetermination = Calculator.coefficientOfDetermination(values, fitValues);
        assertEquals(1.0, coefficientOfDetermination, 0.0001);
    }

    @Test
    public void testCalculateCoefficientOfDetermination2()  {
        double[] values = {1, 2, 3, 4, 5, 6, 7, 9, 9, 10};
        double[] fitValues = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        double coefficientOfDetermination = Calculator.coefficientOfDetermination(values, fitValues);
        assertEquals(0.99068, coefficientOfDetermination, 0.00001);
    }

    @Test
    public void testCalculateCoefficientOfDetermination3()  {
        double[] values = {1, 3, 5, 7};
        double[] fitValues = {3, 5, 7, 10};

        double coefficientOfDetermination = Calculator.coefficientOfDetermination(values, fitValues);
        assertEquals(0.988, coefficientOfDetermination, 0.001);
    }

    @Test
    public void testCalculateCoefficientOfDetermination4()  {
        double[] values = {39, 42, 67, 76};
        double[] fitValues = {44, 40, 60, 84};

        double coefficientOfDetermination = Calculator.coefficientOfDetermination(values, fitValues);
        assertEquals(0.885, coefficientOfDetermination, 0.001);
    }

    @Test
    public void testRsquaredCoefficientOfDeterminationNaN()  {
        double[] xValues = new double[] {-2, -1,  1, 2};
        double[] yValues = new double[] {4, 4, 4, 4};

        double rsquared = Calculator.coefficientOfDetermination(xValues, yValues);
        assertTrue("Flat line gets NaN", Double.isNaN(rsquared));

        double sd = Calculator.sd(yValues);
        assertEquals(0.000, sd, 0.001);
    }

    @Test
    public void testRsquaredOriginalFlatLine()  {
        double[] xValues = new double[] {-2, -1,  1, 2};
        double[] yValues = new double[] {4, 4, 4, 4};
        double rsquared = Calculator.rSquared(xValues, yValues);
        assertEquals(0.000, rsquared, 0.001);

        double sd = Calculator.sd(yValues);
        assertEquals(0.000, sd, 0.001);
    }

    @Test
    public void testRsquaredOriginalAlmostFlatLine()  {
        double[] xValues = new double[] {-2, -1,  1, 2};
        double[] yValues = new double[] {4, 3, 5, 4};
        double rsquared = Calculator.rSquared(xValues, yValues);
        assertEquals(0.000, rsquared, 0.001);

        double sd = Calculator.sd(yValues);
        assertEquals(0.707, sd, 0.001);
    }

    @Test
    public void testRsquaredOriginalFlatLine2()  {
        double[] xValues = new double[] {-2, -1,  1, 2};
        double[] yValues = new double[] {4, 3, 5, 4};
        double rsquared = Calculator.rSquared(xValues, yValues);
        assertEquals(0.000, rsquared, 0.001);

        double sd = Calculator.sd(yValues);
        assertEquals(0.707, sd, 0.001);
    }

    @Test
    public void testRsquaredOriginalFlatLine3()  {
        double[] xValues = new double[] {-2, -1,  1, 2};
        double[] yValues = new double[] {40, 20, 41, 40};
        double rsquared = Calculator.rSquared(xValues, yValues);
        assertEquals(0.000, rsquared, 0.001);

        double sd = Calculator.sd(yValues);
        assertEquals(8.814, sd, 0.001);
    }

    @Test
    public void testRsquaredOriginalFlatLine4()  {
        double[] xValues = new double[] {-2, -1,  1, 2};
        double[] yValues = new double[] {40, -20, 41, 40};
        double rsquared = Calculator.rSquared(xValues, yValues);
        assertEquals(0.000, rsquared, 0.001);

        double sd = Calculator.sd(yValues);
        assertEquals(26.128, sd, 0.001);
    }

    @Test
    public void testRsquaredCoefficientOfDeterminationFlatLine()  {
        double[] xValues = new double[] {-2, -1,  1, 2};
        double[] yValues = new double[] {40, 39, 41, 40};
        double rsquared = Calculator.coefficientOfDetermination(xValues, yValues);
        assertEquals(0.199, rsquared, 0.001);
    }

    @Test
    public void testRsquaredCoefficientOfDeterminationFlatLine2()  {
        double[] xValues = new double[] {-2, -1,  1, 2};
        double[] yValues = new double[] {40, 20, 41, 40};
        double rsquared = Calculator.coefficientOfDetermination(xValues, yValues);
        assertEquals(0.141, rsquared, 0.001);
    }

    @Test
    public void testNormalizedSqrdStdDevStraighLine()  {
        double[] values = new double[] {40, 40,  40, 40};
        double[] fitValues = new double[] {40, 40, 40, 40};
        double fitQuality = Calculator.squaredNormalizedStandardDev(values, fitValues);
        assertEquals(0.0, fitQuality, 0.01);
    }

    @Test
    public void testNormalizedSqrdStdDevStraighLine2()  {
        double[] values = new double[] {35, 40,  50, 40};
        double[] fitValues = new double[] {40, 40, 40, 40};
        double fitQuality = Calculator.squaredNormalizedStandardDev(values, fitValues);
        assertEquals(0.019, fitQuality, 0.01);
    }

    @Test
    public void testNormalizedSqrdStdDevNoGoodFit()  {
        double[] values = new double[] {20, 40, 150, -40};
        double[] fitValues = new double[] {40, 40, 40, 40};
        double fitQuality = Calculator.squaredNormalizedStandardDev(values, fitValues);
        assertEquals(2.95, fitQuality, 0.01);
    }

    @Test
    public void testNormalizedSqrdStdDevNoGoodFitSmall()  {
        double[] values = new double[] {0.20, 0.40, 1.50, -0.40};
        double[] fitValues = new double[] {0.40, 0.40, 0.40, 0.40};
        double fitQuality = Calculator.squaredNormalizedStandardDev(values, fitValues);
        assertEquals(2.95, fitQuality, 0.01);
    }

    @Test
    public void testNormalizedSqrdStdDevNoGoodBig()  {
        double[] values = new double[] {2000, 4000, 15000, -4000};
        double[] fitValues = new double[] {4000, 4000, 4000, 4000};
        double fitQuality = Calculator.squaredNormalizedStandardDev(values, fitValues);
        assertEquals(2.95, fitQuality, 0.01);
    }

    @Test
    public void testNormalizedSqrdStdDevAroundZero()  {
        double[] values = new double[] {-0.1, 0.1, -0.2, -0.13};
        double[] fitValues = new double[] {0, 0, 0, 0};
        double fitQuality = Calculator.squaredNormalizedStandardDev(values, fitValues);
        assertEquals(0.019, fitQuality, 0.001);
    }

    @Test
    public void testNormalizedSqrdStdDevFewPeaks()  {
        double[] values = new double[] {100, 210, 220, 230, 300, 250, 260, 270, 280, 290, 300};
        double[] fitValues = new double[] {200, 210, 220, 230, 240, 250, 260, 270, 280, 290, 300};
        double fitQuality = Calculator.squaredNormalizedStandardDev(values, fitValues);
        assertEquals(0.0284, fitQuality, 0.001);
    }

    @Test
    public void testClosestRoundedNumberOfLog10() {
        assertEquals(1, Calculator.closestRoundedNumberOfLog10(1));
        assertEquals(6, Calculator.closestRoundedNumberOfLog10(6));
        assertEquals(10, Calculator.closestRoundedNumberOfLog10(8));
        assertEquals(10, Calculator.closestRoundedNumberOfLog10(9));
        assertEquals(10, Calculator.closestRoundedNumberOfLog10(14));
        assertEquals(20, Calculator.closestRoundedNumberOfLog10(16));
        assertEquals(150, Calculator.closestRoundedNumberOfLog10(154));
        assertEquals(1500, Calculator.closestRoundedNumberOfLog10(1544));
    }
}