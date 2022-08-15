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
package nl.stokpop.lograter.util;

import java.util.Random;

/**
 * Generate random sets.
 */
public class RandomGenerator {

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    public double[] generateNormalDistributionSet(final int size, final double stddev, final double mean, final long minimum, final long maximum) {
        final double[] values = new double[size];

        for (int i = 0; i < size; i++) {
            values[i] = generateGaussianValue(stddev, mean, minimum, maximum);
        }

        return values;
    }

    /*
     * This is a possible implementation for stub delays
     */
    private long generateGaussianValue(final double stddev, final double mean, final long minimum, final long maximum) {

        double corrected;

        do {
            final double gaussian = RANDOM.nextGaussian();
            corrected = (gaussian * stddev) + mean;
        } while (corrected < minimum);

        if (corrected > maximum) {
            return maximum;
        }
        return Math.round(corrected);
    }

}
