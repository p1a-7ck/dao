package factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * dao
 */
public class ConnectionFactory {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionFactory.class);
    public static final Properties properties = new Properties();
    private static ConnectionFactory INSTANCE;

    private ConnectionFactory() {
        try {
            Class.forName(ConnectionFactory.properties.getProperty("DRIVER_CLASS"));
            logger.info("Database driver initiated successfully");
        } catch (ClassNotFoundException exc) {
            logger.error("Database driver class not found", exc);
        }
    }

    public static ConnectionFactory getInstance() {
        if (ConnectionFactory.INSTANCE == null)
            ConnectionFactory.INSTANCE = new ConnectionFactory();
        return ConnectionFactory.INSTANCE;
    }

    public static String getDatabaseProperty(String key) {
        try {
            if (ConnectionFactory.properties.size() == 0)
                ConnectionFactory.properties
                        .load(ConnectionFactory.class.getClassLoader().getResourceAsStream("db.properties"));
            return ConnectionFactory.properties.getProperty(key);
        } catch (IOException exc) {
            logger.error("File 'db.properties' IO error", exc);
        }
        return null;
    }

    public Connection createConnection() {
        try {
            logger.info("Creating connection");
            return DriverManager.getConnection(ConnectionFactory.properties.getProperty("URL"),
                    ConnectionFactory.properties.getProperty("USER"), ConnectionFactory.properties.getProperty("PASS"));
        } catch (SQLException exc) {
            logger.error("Connection create error", exc);
        }
        return null;
    }
}
