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
package nl.stokpop.lograter.clickpath;

import org.junit.Assert;
import org.junit.Test;

public class ClickPathCollectorTest {

    @Test
    public void testInMemoryClickPathCollector() {

        final long startTimestamp = System.currentTimeMillis();
        ClickPath clickPath1 = new ClickPath(startTimestamp, "step1", "sessionId1");
        clickPath1.addToPath("step2", startTimestamp + 100);
        clickPath1.addToPath("step3", startTimestamp + 200);
        clickPath1.addToPath("step4", startTimestamp + 300);
        clickPath1.addToPath("step5", startTimestamp + 400);

        final long startTimestamp1_1 = startTimestamp + 10000;
        ClickPath clickPath1_1 = new ClickPath(startTimestamp1_1, "step1", "sessionId1_1");
        clickPath1_1.addToPath("step2", startTimestamp1_1 + 200);
        clickPath1_1.addToPath("step3", startTimestamp1_1 + 400);
        clickPath1_1.addToPath("step4", startTimestamp1_1 + 600);
        clickPath1_1.addToPath("step5", startTimestamp1_1 + 800);

        final long startTimestamp2 = startTimestamp + 1000;
        ClickPath clickPath2 = new ClickPath(startTimestamp2, "step1", "sessionId2");
        clickPath2.addToPath("step5", startTimestamp2 + 100);
        clickPath2.addToPath("step4", startTimestamp2 + 200);

        final long startTimestamp3 = startTimestamp + 3000;
        ClickPath clickPath3 = new ClickPath(startTimestamp3, "step3", "sessionId3");
        clickPath3.addToPath("step5", startTimestamp3 + 100);
        clickPath3.addToPath("step2", startTimestamp3 + 200);

        InMemoryClickpathCollector collector = new InMemoryClickpathCollector();
        collector.addClickPath(clickPath1);
        collector.addClickPath(clickPath2);
        collector.addClickPath(clickPath3);
        collector.addClickPath(clickPath1_1);

        Assert.assertEquals(400, clickPath1.getSessionDurationInMillis());
        Assert.assertEquals(200, clickPath2.getSessionDurationInMillis());
        Assert.assertEquals(200, clickPath3.getSessionDurationInMillis());

        Assert.assertArrayEquals(new Long[] {100L, 100L, 100L, 100L}, clickPath1.getDurationPerStepInMillis());

        Assert.assertEquals(600, collector.getAvgSessionDurationForClickPath(clickPath1.getPathAsString()));

        Assert.assertEquals(4, collector.getAvgDurationBetweenSteps(clickPath1.getPathAsString()).length);
        Assert.assertArrayEquals(new Long[]{150L, 150L, 150L, 150L}, collector.getAvgDurationBetweenSteps(clickPath1.getPathAsString()));

        Assert.assertTrue(collector.getPathAsStringWithAvgDuration(clickPath1.getPathAsString()).endsWith(ClickPath.CLICKPATH_END));
    }
}
