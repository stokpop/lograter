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
package nl.stokpop.lograter.util;

public final class Calculator {

	public static final double TINY = 1.e-20;

	private Calculator() {}

	public static double sd(long[] values) {
		double variance = variance(values);
		return Math.sqrt( variance ); 
	}

	public static double variance(long[] values) {
		int length = values.length;

		if (length == 0) return 0;
		if (length == 1) return 0;
		double sum = 0;
		double mean = mean(values);
		for (long i : values) sum += Math.pow((i - mean), 2);
		return sum / length;
	}

	public static double variance(double[] values) {
		int length = values.length;

		if (length == 0) return 0;
		if (length == 1) return 0;
		double sum = 0;
		double mean = mean(values);
		for (double i : values) sum += Math.pow((i - mean), 2);
		return sum / length;
	}

	public static double mean(long[] a) {
		double sum = sum(a); 
		return sum / a.length;
	}

	public static double mean(double[] a) {
		double sum = sum(a);
		return sum / a.length;
	}

	public static double sum(long[] values) {
		if (values.length > 0) {
			double sum = 0;
			for (long i : values) {
				sum += i;
			}
			return sum;
		}
		return 0;
	}

	public static double sum(double[] values) {
		if (values.length > 0) {
			double sum = 0;
			for (double i : values) {
				sum = sum + i;
			}
			return sum;
		}
		return 0;
	}

	public static double sumOfSquares(double[] values) {
		if (values.length > 0) {
			double sum = 0;
			for (double i : values) {
				sum = sum + (i * i);
			}
			return sum;
		}
		return 0;
	}

	public static double sumOfProducts(double[] xValues, double[] yValues) {
		if (xValues.length != yValues.length) {
			throw new RuntimeException("Supply two arrays of same size: # xValues: " + xValues.length + " # yValues: " + yValues.length);
		}
		int length = xValues.length;
		if (length > 0) {
			double sum = 0;
			for (int i = 0; i < length; i++) {
				sum = sum + (xValues[i] * yValues[i]);
			}
			return sum;
		}
		return 0;
	}

	public static double sumSquaresProportionalTo(double[] values, double mean) {
		int length = values.length;

		if (length > 0) {
			double sum = 0;
            for (double value : values) {
                double diff = value - mean;
                sum = sum + (diff * diff);
            }
			return sum;
		}
		return 0;
	}

	public static double sumSquaresDiff(double[] values, double[] valuesToSubstract) {
		int size = values.length;
		if (size != valuesToSubstract.length) {
			throw new RuntimeException("Supply two arrays of same size: # fitLineValues: " + size + " # valuesToSubstract: " + valuesToSubstract.length);
		}
		if (size > 0) {
			double sum = 0;
			for (int i = 0; i < size; i++) {
				double diff = values[i] - valuesToSubstract[i];
				sum = sum + (diff * diff);
			}
			return sum;
		}
		return 0;
	}


	/**
	 * Calculate the rSquared to asses how good fit is:
	 * http://en.wikipedia.org/wiki/Coefficient_of_determination
	 * See also the meaning of R-squared: http://www.graphpad.com/guides/prism/6/curve-fitting/index.htm?reg_diagnostics_tab_7_2.htm
	 * Note: r-squared can be negative!
	 * @return a values between 0 and 1. The closer to one, the better the fit.
	 */
	private static double rSquaredBasedOnMean(double[] values, double[] fitValues) {
		int count = values.length;

		double sumY = sum(values);
		double yMean = sumY / count;

		double residualSumOfSquares = sumSquaresDiff(fitValues, values);
		double sumSquaresProportionalToVariance = sumSquaresProportionalTo(values, yMean);

        return 1.0 - (residualSumOfSquares / sumSquaresProportionalToVariance);
	}

	/**
	 * R-squared or coefficient of determination calculation, without y mean correction. Higher is better.
	 * See http://calculator.tutorvista.com/r-squared-calculator.html
	 * @return r-squared value between 0 and 1
	 */
	public static double coefficientOfDetermination(double[] xValues, double[] yValues) {
		int countX = xValues.length;
		int countY = yValues.length;

		if (countX != countY) {
			throw new CalculatorException(String.format("Supply same number of xValues (%s) and yValues (%s).", countX, countY));
		}

		if (countX == 0) {
			throw new CalculatorException("Do not supply empty arrays for this calculation.");
		}

		double sumX = sum(xValues);
		double sumY = sum(yValues);
		double sumXY = sumOfProducts(xValues, yValues);
		double sumOfSquaresX = sumOfSquares(xValues);
		double sumOfSquaresY = sumOfSquares(yValues);

		double part1 = countX * sumOfSquaresX - (sumX * sumX);
		double part2 = countX * sumOfSquaresY - (sumY * sumY);
		double aboveLine = countX * sumXY - sumX * sumY;
		double correlation = aboveLine / Math.sqrt(part1 * part2);

		double coefficientOfDetermination = correlation * correlation;

		if (coefficientOfDetermination < -0.0001 || coefficientOfDetermination > 1.0001) {
			throw new CalculatorException("The R-squared should be between 0 and 1. Now it is: " + coefficientOfDetermination);
		}

		return coefficientOfDetermination;
	}

	/**
	 * Calculate rSquared from the differences between the values and the fit values.
	 */
	public static double rSquared(double[] values, double[] fitValues) {
		int countX = values.length;
		int countY = fitValues.length;

		if (countX != countY) {
			throw new CalculatorException(String.format("Supply same number of values (%s) and fitValues (%s).", countX, countY));
		}

		if (countX == 0) {
			throw new CalculatorException("Do not supply empty arrays for this calculation.");
		}

		double sumN = 0.0;
		double sumD = 0.0;
		for (int i = 0; i < countX; i++) {
			double y = fitValues[i];
			double fitY = fitValues[i];
			double diff = (y - fitY);
			diff *= diff;
			sumN += diff;
			sumD += y * y;
		}

		return sumN / (sumD + TINY);
	}

	/**
	 * Calculate a number that gives an indication if values are close to fit values.
	 * It is a normalized number, so it can be used as a comparison between data sets (lines).
	 *
	 * @return the normalized squared std deviation, the closer to zero, the better the "fit"
	 */
	public static double squaredNormalizedStandardDev(double[] values, double[] fitValues) {
		double totalDiffSqrd = 0;
		int length = values.length;
		for (int i = 0; i < length; i++) {
			double fitValue = fitValues[i];
			double value = values[i];
			double diff = fitValue - value;
			double normalizedDiff = fitValue == 0 ? diff : diff / fitValue;
			double diffSqrd = normalizedDiff * normalizedDiff;
			totalDiffSqrd = totalDiffSqrd + diffSqrd;
		}
		return totalDiffSqrd / length;
	}

	public static double sd(double[] values) {
		double variance = variance(values);
		return Math.sqrt( variance );
	}

    /**
     * Numbers below 8 are returned as is.
     * Examples: 14 -&gt; 10, 154 -&gt; 150, 1544 -&gt; 1500
     *
     * @return number closest to a rounded number, considering number of log10.
     */
    public static long closestRoundedNumberOfLog10(long aNumber) {
	    if (aNumber < 8) return aNumber;
        long numberOfZeros = (long) Math.log10((double) aNumber);
        numberOfZeros = Math.max(1, numberOfZeros - 1);

        long powerOf10 = (long) Math.pow(10, numberOfZeros);

		double dbl = ((double)aNumber) / powerOf10;
		aNumber = Math.round(dbl) * powerOf10;
        return aNumber;
    }
}
