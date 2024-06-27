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
package nl.stokpop.lograter.processor.performancecenter;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PerformanceCenterCalculatorTest {

    @Test
    public void createDataSet1() {
        List<Integer> numbers = new ArrayList<>();
        numbers.add(1);
        numbers.add(6);
        numbers.add(9);

        check(numbers);
    }

    @Test
    public void createDataSet2() {
        List<Integer> numbers = new ArrayList<>();
        numbers.add(1);
        numbers.add(6);
        numbers.add(4);
        numbers.add(9);
        check(numbers);
    }
    
    @Test
    public void createDataSet3() {

        List<Integer> numbers = new ArrayList<>();
        numbers.add(10);
        numbers.add(1000);
        numbers.add(400);
        numbers.add(500);
        numbers.add(600);
        numbers.add(200);
        numbers.add(400);
        numbers.add(800);
        numbers.add(900);
        numbers.add(400);
        numbers.add(100);
        numbers.add(800);

        check(numbers);

    }

    private void check(List<Integer> numbers) {

        Integer min = numbers.stream().min(Integer::compareTo).orElse(-1);
        Integer max = numbers.stream().max(Integer::compareTo).orElse(-2);

        double avgNumbers = numbers.stream().mapToDouble(Double::valueOf).average().orElse(-1);

        List<Double> dataSet = PerformanceCenterCalculator.createDataSet(numbers.size(), min, max, avgNumbers);

        assertEquals(numbers.size(), dataSet.size());
        
        double avgDataset = dataSet.stream().mapToDouble(Double::doubleValue).average().orElse(-2);

        assertEquals(avgNumbers, avgDataset, 0.0001d);
    }
}