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

import nl.stokpop.lograter.LogRaterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteJDBCLoader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Used to bootstrap a database for lograter usage.
 *
 * Use 'lograter.db.path' system property to change database location and name from default 'lograter.db' in working dir.
 */
public class DatabaseBootstrap {
	
	private static final Logger log = LoggerFactory.getLogger(DatabaseBootstrap.class);
	private static final DatabaseBootstrap INSTANCE = new DatabaseBootstrap();

	private final Connection connection;
    private final String lograterDbPath;

	public static DatabaseBootstrap instance() {
		return INSTANCE;
	}
	
	private DatabaseBootstrap() {
        String lograterDbPath = System.getProperty("lograter.db.path", "lograter.db");
        log.info("Lograter database path: {}", lograterDbPath);
        this.lograterDbPath = lograterDbPath;
        this.connection = getDatabaseConnection(false);
	}

	public void bootstrapDatabase(boolean clearDatabase) {
		 // load the sqlite-JDBC driver using the current class loader
	    try {
			Class.forName("org.sqlite.JDBC");
			log.info("SqLite running in {} mode", SQLiteJDBCLoader.isNativeMode() ? "native" : "pure-java");
			
			log.info("Creating database");

		    Connection connection = getDatabaseConnection();
            try (Statement statement = connection.createStatement()) {
                if (clearDatabase) {
                    log.info("Clearing database");
                    statement.executeUpdate("drop table IF EXISTS counter_store");
                    statement.executeUpdate("drop table IF EXISTS counter");
                    statement.executeUpdate("drop table IF EXISTS measurement");
                }
                statement.executeUpdate("create table IF NOT EXISTS counter_store (id integer primary key AUTOINCREMENT, name string not null)");
                statement.executeUpdate("create table IF NOT EXISTS counter (id integer primary key AUTOINCREMENT, counter_store_id integer not null, name string not null, is_total_counter integer not null)");
                statement.executeUpdate("create table IF NOT EXISTS measurement (counter_id integer not null, timestamp long not null, duration integer not null)");
            } finally {
                connection.commit();
            }

	    } catch (Exception e) {
            throw new LogRaterException("Cannot create database.", e);
	    }

	}

	public void createIndexes() {
		try {
			Connection connection = getDatabaseConnection();
            try (Statement statement = connection.createStatement()) {
                log.info("about to create indexes");
                //statement.executeUpdate("create index IF NOT EXISTS timestamp_index ON measurement (timestamp)");
                statement.executeUpdate("create index IF NOT EXISTS counter_id_index ON measurement (counter_id)");
                log.info("indexes created");
            } finally {
                connection.commit();
            }
		} catch (SQLException e) {
			throw new LogRaterException("Cannot create database indexes.", e);
		}
	}

	public void dropIndexes() {
		try {
			Connection connection = getDatabaseConnection();
            try (Statement statement = connection.createStatement()) {
                log.info("about to drop indexes");
                //statement.executeUpdate("drop index IF EXISTS timestamp_index");
                statement.executeUpdate("drop index IF EXISTS counter_id_index");
                log.info("indexes dropped");
            } finally {
                connection.commit();
            }
		} catch (SQLException e) {
			throw new LogRaterException("Cannot create database indexes.", e);
		}
	}

	private Connection getDatabaseConnection(boolean readOnly) {
        final String databaseName = "jdbc:sqlite:" + this.lograterDbPath;
        try {
            SQLiteConfig config = new SQLiteConfig();
            config.setReadOnly(readOnly);
            config.setSharedCache(true);
            Connection con = DriverManager.getConnection(databaseName, config.toProperties());
			con.setAutoCommit(false);
			con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			return con;
		} catch (SQLException e) {
			throw new LogRaterException("Cannot get database connection to: " + databaseName, e);
		}
	}

	public Connection getDatabaseConnection() {
		return connection;
	}

	@Override
	public boolean equals(final Object o) {
		// singleton!
		return (this == o); 
	}

	@Override
	public int hashCode() {
		// singleton!
		return 0;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("DatabaseBootstrap{");
		sb.append("connection=").append(connection);
		sb.append('}');
		return sb.toString();
	}
}
