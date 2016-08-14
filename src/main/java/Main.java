import factory.ConnectionFactory;
import factory.H2DatabaseFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * dao
 */
public class Main {
    public static void main(String[] args) {
        H2DatabaseFactory.startServer();
        try {
            Connection connection = ConnectionFactory.getInstance().createConnection();
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE USER (ID INT PRIMARY KEY, LOGIN VARCHAR(255), PASS VARCHAR(255))");
            statement.execute("INSERT INTO USER VALUES (1, 'admin', 'admin')");
            statement.execute("INSERT INTO USER VALUES (2, 'user', 'user')");
            statement.execute("INSERT INTO USER VALUES (3, 'test', 'test')");
            statement.execute("SELECT * FROM USER");
            while(statement.getResultSet().next()) {
                System.out.println(statement.getResultSet().getInt(1));
                System.out.println(statement.getResultSet().getString(2));
                System.out.println(statement.getResultSet().getString(2));
            }
            statement.close();
            connection.close();
        } catch (SQLException exc) {
            System.out.println(exc);
        }
        H2DatabaseFactory.stopServer();
    }
}
