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

import nl.stokpop.lograter.util.Calculator;
import nl.stokpop.lograter.util.metric.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Determine best fit: http://web.archive.org/web/20150715022401/http://faculty.cs.niu.edu/~hutchins/csci230/best-fit.htm
 * Calculate the quality of fit to asses how good fit is with normalized squared std dev.
 *
 * Determine outliers: http://www.purplemath.com/modules/boxwhisk3.htm
 * and: https://web.archive.org/web/20190204112213/http://dsearls.org/other/CalculatingQuartiles/CalculatingQuartiles.htm
 *
 */
public class BestFitLine {

    private final List<Point> outliers = new ArrayList<>();
    private final List<Point> removedOutliers = new ArrayList<>();
    private final List<Integer> outlierIndices = new ArrayList<>();

    private double slope;
    private double yIntersection;
    private double qualityOfFit;
    private double yMean;
    private double[] xValues;
    private double[] yValues;

    /**
     * Create a line fit. Needs at least 2 values for a fit. Needs at least 3 values to determine outliers.
     * @param xValues need to be ordered, and no duplicates expected
     * @param yValues should be the same amount of xValues
     */
    public BestFitLine(double[] xValues, double[] yValues) {
        int countX = xValues.length;
        int countY = yValues.length;

        if (countX != countY) {
            throw new BestFitLineException("Supply same number of x and y values. X values: " + countX + " Y values: " + countY);
        }
        if (countX == 0) {
            throw new BestFitLineException("No values given. Cannot determine fit. X values: " + countX + " Y values: " + countY);
        }
        if (countX < 2) {
            throw new BestFitLineException("No enough values given. Supply at least 2. Cannot determine fit. X values: " + countX + " Y values: " + countY);
        }
        validateIncreasingOrder(xValues);

        this.xValues = new double[countX];
        this.yValues = new double[countY];
        System.arraycopy(xValues, 0, this.xValues, 0, countX );
        System.arraycopy(yValues, 0, this.yValues, 0, countY );

        calculateBestFit(xValues, yValues);

        determineOutliers(xValues, yValues);
    }

    private void determineOutliers(double[] xValues, double[] yValues) {
        if (xValues.length > 2) {
            // determine the outliers relative to the current fit line:
            // use the abs diff from lines
            final double[] diffs = new double[yValues.length];

            for (int i = 0; i < yValues.length; i++) {
                diffs[i] = yValues[i] - calculateY(xValues[i]);
            }

            final FiveNumberSummary fiveNumberSummary = FiveNumberSummary.calculate(diffs);

            double lowerFence = fiveNumberSummary.getLowerFence();
            double upperFence = fiveNumberSummary.getUpperFence();

            for (int i = 0; i < xValues.length; i++) {
                double diff = diffs[i];
                if (diff < lowerFence || diff > upperFence) {
                    double x = xValues[i];
                    double y = yValues[i];
                    outlierIndices.add(i);
                    outliers.add(new Point(x, y));
                }
            }
        }
    }

    /**
     * Used to create a new BestFitLine that remembers previously removed outliers.
     */
    private BestFitLine(double[] filteredXValues, double[] filteredYValues, List<Point> removedOutliers) {
        this(filteredXValues, filteredYValues);
        this.removedOutliers.addAll(removedOutliers);
    }

    public boolean hasOutliers() {
        return !this.outliers.isEmpty();
    }

    private void validateIncreasingOrder(double[] values) {
        if (values.length == 0) return;
        double previous = values[0];
        for (int i = 1; i < values.length; i++) {
            double current =  values[i];
            if (current < previous) {
                String message = String.format(Locale.US, "These values are not ordered: %f and %f on array positions: %s and %s.", previous, current, (i - 1), i);
                throw new BestFitLineException(message);
            }
            previous = current;
        }
    }

    private void calculateBestFit(double[] xValues, double[] yValues) {
        int count = xValues.length;
        double sumX = Calculator.sum(xValues);
        double sumY = Calculator.sum(yValues);
        double sumX2 = Calculator.sumOfSquares(xValues);
        double sumXY = Calculator.sumOfProducts(xValues, yValues);

        double xMean = sumX / count;
        double yMean = sumY / count;

        slope = (sumXY - sumX * yMean) / (sumX2 - sumX * xMean);
        yIntersection = yMean - slope * xMean;
        double[] fitLineValues = createFitLineValues(xValues);
        qualityOfFit = Calculator.squaredNormalizedStandardDev(yValues, fitLineValues);
        this.yMean = yMean;
    }

    public double[] createFitLineValues(double[] xValues) {
        double[] fitValues = new double[xValues.length];
        for (int i = 0; i < xValues.length; i++) {
            fitValues[i] = calculateY(xValues[i]);
        }
        return fitValues;
    }

    public double getSlope() {
        return slope;
    }

    public double getYintersection() {
        return yIntersection;
    }

    public double getYmean() { return yMean; }

    public double calculateY(double x) {
        return x * slope + yIntersection;
    }

    public double getQualityOfFit() {
        return qualityOfFit;
    }

    public double getPercentageChange(long startX, long endX) {
        double startY = calculateY(startX);
        return (getAbsoluteChange(startX, endX) / startY) * 100d;
    }

    public double getAbsoluteChange(long startX, long endX) {
        double startY = calculateY(startX);
        double endY = calculateY(endX);
        return endY - startY;
    }

    public List<Point> getOutliers() {
        return Collections.unmodifiableList(new ArrayList<>(outliers));
    }

    /**
     * Create a new BestFitLine without outliers.
     * @return the new BestFitLine without the largest outlier, or this BestFitLine if no outliers are present.
     */
    public BestFitLine createBestFitLineWithoutCurrentOutliers() {
        if (hasOutliers()) {
            final double[] filteredXValues = newArrayWithoutElementOnIndex(xValues, outlierIndices);
            final double[] filteredYValues = newArrayWithoutElementOnIndex(yValues, outlierIndices);
            List<Point> allOutliers = new ArrayList<>(outliers);
            allOutliers.addAll(removedOutliers);
            return new BestFitLine(filteredXValues, filteredYValues, allOutliers);
        }
        else {
            return this;
        }
    }

    private static double[] newArrayWithoutElementOnIndex(double[] values, List<Integer> outlierIndices)
    {
        int numberOfOutliers = outlierIndices.size();
        Collections.sort(outlierIndices);

        int[] outlierIndicesArray = new int[numberOfOutliers];
        for (int i = 0; i < numberOfOutliers; i++) {
            outlierIndicesArray[i] = outlierIndices.get(i);
        }

        double[] newValues = new double[values.length - numberOfOutliers];
        int j = 0;
        int currentOutlierIdx = 0;
        int indexOfOutlierToSkip = outlierIndicesArray[currentOutlierIdx];
        for(int i = 0; i < values.length; i++) {
            if(i == indexOfOutlierToSkip) {
                currentOutlierIdx++;
                if (currentOutlierIdx < numberOfOutliers) {
                    indexOfOutlierToSkip = outlierIndicesArray[currentOutlierIdx];
                }
            }
            else {
                newValues[j] = values[i];
                j++;
            }
        }
        return newValues;
    }

    public int getNumberOfDataPoints() {
        return xValues.length;
    }

    public BestFitLine createFitFunctionRelativeX() {
        int size = xValues.length;
        double[] xValuesNew = new double[size];

        double timestampZero = xValues[0];

        for (int i = 0; i < size; i++) {
            xValuesNew[i] = xValues[i] - timestampZero;
        }

        return new BestFitLine(xValuesNew, yValues);
    }

    public List<Point> getRemovedOutliers() {
        return removedOutliers;
    }

    /**
     * Keep removing outliers and making new fits until no more outliers are present.
     * Stores all removed outliers in the removedOutliers member.
     * @return the BestFitLine which has no outliers
     */
    public BestFitLine createUltimateBestFitLineWithoutOutliers() {
        BestFitLine currentBestFitLine = this;
        while (currentBestFitLine.hasOutliers()) {
            currentBestFitLine = currentBestFitLine.createBestFitLineWithoutCurrentOutliers();
        }
        return currentBestFitLine;
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "BestFitLine{" +
                "outliers=" + outliers +
                ", slope=" + slope +
                ", yIntersection=" + yIntersection +
                ", qualityOfFit=" + qualityOfFit +
                ", yMean=" + yMean +
                '}';
    }
}
