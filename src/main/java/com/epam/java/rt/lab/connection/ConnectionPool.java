package com.epam.java.rt.lab.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * dao
 */
public class ConnectionPool implements DataSource {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
    private static final Properties PROPERTIES = new Properties();
    private final Lock propertiesLock = new ReentrantLock();
    private BlockingQueue<PooledConnection> grantedConnectionQueue;
    private BlockingQueue<PooledConnection> availableConnectionQueue;
    private final Lock connectionQueueLock = new ReentrantLock(true);
    private AtomicInteger lastAvailableQueueSize = new AtomicInteger(0);
    private AtomicBoolean shutdownPool = new AtomicBoolean(false);
    public AtomicInteger countConnections = new AtomicInteger(0);

    private ConnectionPool() {
        try {
            ConnectionPool.PROPERTIES
                    .load(ConnectionPool.class.getClassLoader().getResourceAsStream("connection.properties"));
            initializePool();
        } catch (IOException | SQLException | InterruptedException e) {
            ConnectionPool.PROPERTIES.clear();
            logger.error("Applying properties from 'connection.properties' file error", e);
        }
    }

    public static ConnectionPool getInstance() throws IOException {
        ConnectionPool instance = InstanceHolder.INSTANCE;
        if (ConnectionPool.PROPERTIES.size() == 0) throw new IOException("No connection pool properties found");
        return instance;
    }

    public void reloadProperties(InputStream inputStream) throws IOException, InterruptedException, SQLException {
        try {
            if (this.propertiesLock.tryLock(100, TimeUnit.MILLISECONDS)) {
                ConnectionPool.PROPERTIES.load(inputStream);
                initializePool();
            }
        } catch (IOException | InterruptedException | SQLException e) {
            logger.error("Reload properties error", e);
            throw e;
        } finally {
            this.propertiesLock.unlock();
        }
    }

    private void initializePool() throws NumberFormatException, InterruptedException, SQLException {
        try {
            logger.info("Connection pool initialize start");
            this.grantedConnectionQueue = new ArrayBlockingQueue<>
                    (Integer.valueOf(ConnectionPool.PROPERTIES.getProperty("db.connections.max.opened")));
            this.availableConnectionQueue = new ArrayBlockingQueue<>
                    (Integer.valueOf(ConnectionPool.PROPERTIES.getProperty("db.connections.max.opened")));
            for (int i = 0; i < Integer.valueOf(ConnectionPool.PROPERTIES.getProperty("db.connections.min.opened")); i++)
                this.availableConnectionQueue.offer(new PooledConnection(ConnectionPool.PROPERTIES));
            logger.info("Connection pool initialize complete");
        } catch (NumberFormatException e) {
            logger.error("Initialize pool properties error", e);
            throw e;
        } catch (SQLException e) {
            logger.error("Create new pooled connections error", e);
            throw e;
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            if (this.connectionQueueLock.tryLock(100, TimeUnit.MILLISECONDS)) {
                if (this.availableConnectionQueue.size() <
                        Integer.valueOf(ConnectionPool.PROPERTIES.getProperty("db.connections.min.opened"))
                        && (this.availableConnectionQueue.size() + this.grantedConnectionQueue.size()) <
                        Integer.valueOf(ConnectionPool.PROPERTIES.getProperty("db.connections.max.opened"))) {
                    this.availableConnectionQueue.offer(new PooledConnection(ConnectionPool.PROPERTIES),
                            100, TimeUnit.MILLISECONDS);
                }
                connectionQueueLock.unlock();
            }
            PooledConnection pooledConnection = this.availableConnectionQueue.poll(100, TimeUnit.MILLISECONDS);
            this.grantedConnectionQueue.offer(pooledConnection);
            return pooledConnection;
        } catch (InterruptedException e) {
            logger.error("Get connection interrupted" , e);
            throw new SQLException();
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new UnsupportedOperationException
                ("Get connection should be requested with predefined in properties username and password");
    }

    void releaseConnection(PooledConnection pooledConnection) throws SQLException {
        try {
            this.availableConnectionQueue.offer(pooledConnection, 100, TimeUnit.MILLISECONDS);
            this.grantedConnectionQueue.remove(pooledConnection);
            logger.info("Granted connection released (total granted: {}, total available: {})",
                    this.grantedConnectionQueue.size(), this.availableConnectionQueue.size());

        } catch (InterruptedException e) {
            logger.error("Release connection interrupted" , e);
            throw new SQLException();
        }
    }

    public void shutdown() {
        logger.info("Shutdown initiated");
        this.shutdownPool.set(true);
        PooledConnection pooledConnection;
        do {
            pooledConnection = this.availableConnectionQueue.poll();
        }
        while (this.grantedConnectionQueue.size() > 0);
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
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    private static class InstanceHolder {
        private static final ConnectionPool INSTANCE = new ConnectionPool();
    }
}
