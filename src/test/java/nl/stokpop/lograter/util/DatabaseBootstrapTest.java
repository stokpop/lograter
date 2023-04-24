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
package nl.stokpop.lograter.util;

import org.junit.Test;

public class DatabaseBootstrapTest {

    private static final String TEST_DATABASE_PATH = "build/test-lograter.db";

	@Test
	public void testBootstrapDatabase() {
        injectTestDatabasePathIntoSysVars();
        DatabaseBootstrap databaseBootstrap = DatabaseBootstrap.instance();
		databaseBootstrap.bootstrapDatabase(true);
		databaseBootstrap.dropIndexes();
		databaseBootstrap.createIndexes();
		databaseBootstrap.dropIndexes();

	}

    /**
     * Call this in tests using the database, to make sure the same database location is used for all unit tests.
     * Note: this is in the build directory, assuming the unit tests are run with gradle, so it will be removed
     * with the gradle clean action.
     */
    public static void injectTestDatabasePathIntoSysVars() {
        System.setProperty("lograter.db.path", TEST_DATABASE_PATH);
    }
}