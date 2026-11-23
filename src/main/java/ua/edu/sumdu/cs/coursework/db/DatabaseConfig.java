package ua.edu.sumdu.cs.coursework.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private final String jdbcUrl;
    private final String username;
    private final String password;

    public DatabaseConfig(String host, String port, String dbName, String username, String password) {
        this.jdbcUrl = "jdbc:postgresql://" + host.trim() + ":" + port.trim() + "/" + dbName.trim() + "?characterEncoding=UTF-8";
        this.username = username;
        this.password = password;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }
}