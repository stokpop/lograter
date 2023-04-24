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
package nl.stokpop.lograter.pc;

import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import net.jcip.annotations.NotThreadSafe;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteJDBCLoader;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@NotThreadSafe
public class ResultDbTest {

    private final static Logger log = LoggerFactory.getLogger(ResultDbTest.class);

	@Test
    @Ignore // get your own db from Results.zip to test
    public void testRead() throws Exception {

        Class.forName("org.sqlite.JDBC");
        log.info("SqLite running in {} mode", SQLiteJDBCLoader.isNativeMode() ? "native" : "pure-java");

        String databasePath = "/data/results/results.db";
        final String connectionString = "jdbc:sqlite:" + databasePath;
        log.info("Opening database {}", connectionString);


        SQLiteConfig config = new SQLiteConfig();
        config.setReadOnly(true);
        config.setSharedCache(true);
        Connection con = DriverManager.getConnection(connectionString, config.toProperties());
        con.setAutoCommit(false);
        con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

        PreparedStatement preparedStatement = con.prepareStatement("SELECT name FROM sqlite_temp_master WHERE type='table';");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            log.info("Result: {}", resultSet.getString(1));
        }
        resultSet.close();
        preparedStatement.close();
        con.close();

    }

    @Test
    @Ignore // get your own mdb from Results.zip to test
    public void testReadAccessDatabase() throws Exception {

        String databasePath = "/data/results/Results.mdb";
        Table table = DatabaseBuilder.open(new File(databasePath)).getTable("Result");
        for(Row row : table) {
            log.info("Column 'Start Time' has value: {}", row.get("Start Time"));
        }

        Table table2 = DatabaseBuilder.open(new File(databasePath)).getTable("Event_meter");
        for(Row row : table2) {
            log.info("Row: {}", row);
        }

    }
}