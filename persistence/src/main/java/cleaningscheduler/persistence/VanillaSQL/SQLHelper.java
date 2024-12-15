package cleaningscheduler.persistence.VanillaSQL;

import java.sql.*;

public class SQLHelper {
    private static final String url = "jdbc:mysql://localhost:3306/cleaning_scheduler";
    private static final String username = "bo";
    private static final String password = "password";

    protected static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    protected static <T> T executeQuery(String query, ResultSetProcessor<T> processor) {
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement();
             ResultSet result = stmt.executeQuery(query)
        ) {
           return processor.process(result);
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    @FunctionalInterface
    protected interface ResultSetProcessor <T> {
        T process(ResultSet rs) throws SQLException;
    }
}
