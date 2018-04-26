package timewsr_cf;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String DB_DRIVER = "org.postgresql.Driver";
    private static final String DB_CONNECTION = "jdbc:postgresql://localhost:5432/WS_recommender_dataset";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "1234";

    public static Connection getDBConnection() {
        Connection dbConnection = null;
        try {
            Class.forName(DB_DRIVER);

        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try {
            dbConnection = DriverManager.getConnection(
                    DB_CONNECTION,
                    DB_USER,
                    DB_PASSWORD);

            return dbConnection;

        } catch (SQLException e) {

            System.out.println(e.getMessage());
        }
        return dbConnection;

    }
}
