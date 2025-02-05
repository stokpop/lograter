/*
 * Copyright (C) 2025 Peter Paul Bakker, Stokpop
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

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class LogRaterExceptionTest {

    @Test
    public void testMessageWithoutCause() {
        final String testMessage = "test message";
        final LogRaterException logRaterException = new LogRaterException(testMessage);

        Assert.assertTrue(logRaterException.toString().contains(testMessage));
        Assert.assertTrue(logRaterException.getMessage().contains(testMessage));
        Assert.assertTrue(logRaterException.getLocalizedMessage().contains(testMessage));
    }

    @Test
    public void testMessageWithCause() {
        final String testMessage = "test message";
        final String ioMessage = "Cannot read file";
        final IOException ioException = new IOException(ioMessage);
        final LogRaterException logRaterException = new LogRaterException(testMessage, ioException);

        final String message = logRaterException.getMessage();
        Assert.assertTrue(containsBoth(message, testMessage, ioMessage));
        final String localizedMessage = logRaterException.getLocalizedMessage();
        Assert.assertTrue(containsBoth(localizedMessage, testMessage, ioMessage));
        final String toString = logRaterException.toString();
        Assert.assertTrue(containsBoth(toString, testMessage, ioMessage));
    }

    private boolean containsBoth(String totalString, String subString1, String subString2) {
        return totalString.contains(subString1) && totalString.contains(subString2);
    }
}
