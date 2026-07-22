package com.parking;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection class is used to create a connection with the MySQL database.
 *
 * @author Easha Kadganve
 * @author Vaishnavi Jadhav
 * @version 1.0.0
 * @since 2026-07-14
 */
public class DBConnection {

    /** Database URL */
    private static final String URL = "jdbc:mysql://localhost:3306/smart_parking";

    /** MySQL username */
    private static final String USER = "root";

    /** MySQL password */
    private static final String PASSWORD = "root@1";

    /**
     * Private constructor to prevent creating objects of this utility class.
     */
    private DBConnection() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    /**
     * Creates and returns a connection to the MySQL database.
     *
     * @return Connection object
     * @throws SQLException if a database connection error occurs
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // Display an error if the driver is not found
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        }

        // Return the database connection
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}