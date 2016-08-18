package com.epam.java.rt.lab.dao.factory;

import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * dao
 */
public class JdbcConnectionFactory implements DataSource {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JdbcConnectionFactory.class);
    private static JdbcConnectionFactory INSTANCE;
    private static Queue<Connection> CONNECTION_POOL;
    private static int CONNECTION_POOL_MAX_SIZE;
    private static String DATABASE_URL;
    private static String DATABASE_USERNAME;
    private static String DATABASE_PASSWORD;
    private static Lock POOL_LOCK;

    static {
        try {
            Properties properties = new Properties();
            properties.load(H2DaoFactory.class.getClassLoader().getResourceAsStream("connection.properties"));
            JdbcConnectionFactory.CONNECTION_POOL_MAX_SIZE = Integer.valueOf(properties.getProperty("db.max.connections"));
            JdbcConnectionFactory.CONNECTION_POOL = new ArrayBlockingQueue<>(JdbcConnectionFactory.CONNECTION_POOL_MAX_SIZE);
            JdbcConnectionFactory.DATABASE_URL = properties.getProperty("db.URL");
            JdbcConnectionFactory.DATABASE_USERNAME = properties.getProperty("db.Username");
            JdbcConnectionFactory.DATABASE_PASSWORD = properties.getProperty("db.Password");
            JdbcConnectionFactory.POOL_LOCK = new ReentrantLock();
        } catch (IOException exc) {
            logger.error("File 'connection.properties' IO error", exc);
        }

    }

    private JdbcConnectionFactory() {
        logger.info("JdbcConnectionFactory instance created");
    }

    public static JdbcConnectionFactory getInstance() {
        if (JdbcConnectionFactory.INSTANCE == null)
            JdbcConnectionFactory.INSTANCE = new JdbcConnectionFactory();
        return JdbcConnectionFactory.INSTANCE;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.getConnection(JdbcConnectionFactory.DATABASE_USERNAME, JdbcConnectionFactory.DATABASE_PASSWORD);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        try {
            if (JdbcConnectionFactory.POOL_LOCK.tryLock(10, TimeUnit.MILLISECONDS)) {
                if (JdbcConnectionFactory.CONNECTION_POOL.size() == JdbcConnectionFactory.CONNECTION_POOL_MAX_SIZE)
                    return null;
                Connection connection = DriverManager.getConnection(JdbcConnectionFactory.DATABASE_URL, username, password);
                JdbcConnectionFactory.CONNECTION_POOL.add(connection);
                JdbcConnectionFactory.POOL_LOCK.unlock();
                logger.info("Connection created and ready to use");
                return connection;
            }
        } catch (InterruptedException exc) {

        }
        return null;
    }

    public boolean releaseConnection(Connection connection) throws SQLException {
        connection.close();
        logger.info("Connection closed and ready to release");
        return JdbcConnectionFactory.CONNECTION_POOL.remove(connection);
    }

    public int releaseConnectionAll() throws SQLException {
        int countConnections = 0;
        Connection connection;
        while (!JdbcConnectionFactory.CONNECTION_POOL.isEmpty()) {
            connection = JdbcConnectionFactory.CONNECTION_POOL.remove();
            connection.close();
            countConnections = countConnections + 1;
        }
        return countConnections;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}
