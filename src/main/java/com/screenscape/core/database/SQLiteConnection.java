package com.screenscape.core.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConnection {
    private static final String DB_URL = "jdbc:sqlite::resource:dbt.db";

    private Connection connection;

    public SQLiteConnection(){

    }

    public void connect() throws SQLException {
        this.connection = DriverManager.getConnection(DB_URL);
    }

    public void disconnect() throws SQLException {
        this.connection.close();
    }

    public Connection getConnection() throws SQLException {
        return this.connection;
    }

    @Override
    protected void finalize() throws Throwable {
        this.disconnect();
        super.finalize();
    }
}
