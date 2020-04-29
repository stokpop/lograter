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
package nl.stokpop.lograter.util.fit;

import nl.stokpop.lograter.util.metric.Point;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class BestFitLineTest {

    private static final double DELTA = 0.001d;

    @Test
    public void testBestFitLine()  {

        // See: http://web.archive.org/web/20150715022401/http://faculty.cs.niu.edu/~hutchins/csci230/best-fit.htm

        double[] xValues = new double[] {1, 2, 3};
        double[] yValues = new double[] {1, 3, 4};

        BestFitLine bestFitLine = new BestFitLine(xValues, yValues);

        assertEquals(1.5, bestFitLine.getSlope(), DELTA);
        assertEquals(-0.333, bestFitLine.getYintersection(), DELTA);
        assertEquals(0.012, bestFitLine.getQualityOfFit(), DELTA);
    }


    @Test
    public void testBestFitLineQualityOfFitPerfect()  {
        double[] xValues = new double[] {1, 2, 4, 5};
        double[] yValues = new double[] {1, 2, 4, 5};

        BestFitLine bestFitLine = new BestFitLine(xValues, yValues);

        assertEquals(1.0, bestFitLine.getSlope(), DELTA);
        assertEquals(0.0, bestFitLine.getYintersection(), DELTA);
        assertEquals(0.0, bestFitLine.getQualityOfFit(), DELTA);
        assertEquals(100.0, bestFitLine.getPercentageChange(1, 2), DELTA);
        assertEquals(1.0, bestFitLine.getAbsoluteChange(1, 2), DELTA);
    }

    @Test
    public void testBestFitLineQualityOfFitPerfectMinimal()  {
        double[] xValues = new double[] {1, 100, 101};
        double[] yValues = new double[] {1, 100, 101};

        BestFitLine bestFitLine = new BestFitLine(xValues, yValues);

        assertEquals(1.0, bestFitLine.getSlope(), DELTA);
        assertEquals(0.0, bestFitLine.getYintersection(), DELTA);
        assertEquals(0.0, bestFitLine.getQualityOfFit(), DELTA);
        assertEquals(bestFitLine.getNumberOfDataPoints(), 3);
        assertEquals(99.0, bestFitLine.getAbsoluteChange(1, 100), DELTA);
        assertEquals(9900.0, bestFitLine.getPercentageChange(1, 100), DELTA);

        assertFalse("No outlier is expected.", bestFitLine.hasOutliers());
    }

    @Test
    public void testBestFitLineWithOutlier()  {
        double[] xValues = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        double[] yValues = new double[] {79, 47, 55, 83, 145, 44, 61, 18, 78, 62};

        BestFitLine bestFitLine = new BestFitLine(xValues, yValues);

        assertEquals(-1.745, bestFitLine.getSlope(), DELTA);
        assertEquals(76.8, bestFitLine.getYintersection(), DELTA);
        assertEquals(0.2201, bestFitLine.getQualityOfFit(), DELTA);
        assertEquals(10, bestFitLine.getNumberOfDataPoints());
        assertTrue("Outlier is expected.", bestFitLine.hasOutliers());
        assertEquals(145, bestFitLine.getOutliers().get(0).getY(), DELTA);

        BestFitLine bestFitLineWithoutOutliers = bestFitLine.createBestFitLineWithoutCurrentOutliers();
        assertFalse("No outlier expected.", bestFitLineWithoutOutliers.hasOutliers());
        assertEquals(9, bestFitLineWithoutOutliers.getNumberOfDataPoints());
        assertEquals(0, bestFitLineWithoutOutliers.getOutliers().size());
        // keep already removed outliers
        List<Point> removedOutliers = bestFitLineWithoutOutliers.getRemovedOutliers();
        assertEquals(1, removedOutliers.size());
        assertEquals(145, removedOutliers.get(0).getY(), DELTA);

    }

    @Test
    public void testUltimateBestFitLineWithOutlier()  {
        double[] xValues = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
        double[] yValues = new double[] {79, 47, 55, 83, 145, 44, 61, 18, 78, 62, 79, 47, 55, 83, 128, 44, 61, 18, 78, 62};

        BestFitLine bestFitLine = new BestFitLine(xValues, yValues).createUltimateBestFitLineWithoutOutliers();

        assertFalse("No outlier expected.", bestFitLine.hasOutliers());
        assertEquals(18, bestFitLine.getNumberOfDataPoints());
        assertEquals(0, bestFitLine.getOutliers().size());
        assertEquals(2, bestFitLine.getRemovedOutliers().size());
    }

    @Test
    public void testBestFitLineQualityOfFitVeryBad()  {
        double[] xValues = new double[] {1, 2, 3};
        double[] yValues = new double[] {1, 100, 3};

        BestFitLine bestFitLine = new BestFitLine(xValues, yValues);

        assertEquals(1.777, bestFitLine.getQualityOfFit(), DELTA);
    }

    @Test
    public void testBestFitLineQualityOfFitMiddle()  {
        double[] xValues = new double[] {1, 2, 3, 4};
        double[] yValues = new double[] {2, 4, 2, 4};

        BestFitLine bestFitLine = new BestFitLine(xValues, yValues);

        assertEquals(0.4, bestFitLine.getSlope(), DELTA);
        assertEquals(2.0, bestFitLine.getYintersection(), DELTA);
        assertEquals(0.091, bestFitLine.getQualityOfFit(), DELTA);
    }

    @Test(expected = BestFitLineException.class)
    public void testOrderedXvalues()  {
        double[] xValues = new double[] {1, 3, 2, 4};
        double[] yValues = new double[] {2, 4, 2, 4};
        new BestFitLine(xValues, yValues);
    }

    @Test
    public void testQualityOfFit()  {
        double[] xValues = new double[] {-2, -1,  1, 4};
        double[] yValues = new double[] {-3, -1, 2, 3};

        BestFitLine bestFitLine = new BestFitLine(xValues, yValues);
        double qualityOfFit = bestFitLine.getQualityOfFit();

        assertEquals(0.78, qualityOfFit, DELTA);
    }

    @Test(expected = BestFitLineException.class)
    public void testOneElementArray()  {
        double[] xValues = new double[] {1};
        double[] yValues = new double[] {100};
        new BestFitLine(xValues, yValues);
    }

    @Test
    public void testTwoElementArray()  {
        double[] xValues = new double[] {1, 2};
        double[] yValues = new double[] {100, 200};

        BestFitLine bestFitLine = new BestFitLine(xValues, yValues);
        assertFalse("No largest outlier expected with two values", bestFitLine.hasOutliers());
        assertSame("Same bestFitLine expected when no outlier present", bestFitLine, bestFitLine.createBestFitLineWithoutCurrentOutliers());

    }

}