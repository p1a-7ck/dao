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
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.concurrent.*;
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

    private PooledConnection grantPooledConnection(PooledConnection pooledConnection) throws SQLException {
        try {
            if (pooledConnection == null) pooledConnection = new PooledConnection(ConnectionPool.PROPERTIES);
            this.grantedConnectionQueue.offer(pooledConnection);
            this.countConnections.getAndIncrement();
            logger.info("Cleared connection granted (total granted: {})", this.grantedConnectionQueue.size());
            return pooledConnection;
        } catch (SQLException e) {
            pooledConnection.setShutdownProcess(true);
            pooledConnection.close();
            logger.error("Grant pooled connection error", e);
            throw e;
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            return grantPooledConnection(this.availableConnectionQueue.remove());
        } catch (NoSuchElementException e) {
//            try {
//                if (this.connectionQueueLock.tryLock(10, TimeUnit.MILLISECONDS)) {
                    if (this.availableConnectionQueue.size() + this.grantedConnectionQueue.size() <
                            Integer.valueOf(ConnectionPool.PROPERTIES.getProperty("db.connections.max.opened"))) {
                        return grantPooledConnection(null);
                    }
//                }
//            } catch (InterruptedException eTimeout) {
//                logger.error("Available connection not found", eTimeout);
//                throw new SQLException(eTimeout.getMessage());
//            } finally {
//                this.connectionQueueLock.unlock();
//            }
        }
        throw new SQLException("Available connection not found");
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new UnsupportedOperationException
                ("Get connection should be requested with predefined in properties username and password");
    }

    void releaseConnection(PooledConnection pooledConnection) throws InterruptedException, SQLException {
        this.grantedConnectionQueue.remove(pooledConnection);
        this.availableConnectionQueue.offer(pooledConnection);
//        if (this.connectionQueueLock.tryLock(10, TimeUnit.MILLISECONDS)) {
//            if (this.lastAvailableQueueSize.get() > this.availableConnectionQueue.size()) {
//                this.availableConnectionQueue.offer(pooledConnection);
//            } else {
//                if (this.lastAvailableQueueSize.get() > this.availableConnectionQueue.size() -
//                        Integer.valueOf(ConnectionPool.PROPERTIES.getProperty("db.connections.min.opened"))) {
//                    this.availableConnectionQueue.offer(pooledConnection);
//                } else {
//                    pooledConnection.setShutdownProcess(true);
//                    pooledConnection.close();
//                }
//            }
//            this.lastAvailableQueueSize.set(this.availableConnectionQueue.size());
//        }
        logger.info("Granted connection released (total available: {})", this.availableConnectionQueue.size());
    }

    public void shutdown() {
        logger.info("Shutdown initiated");
        Executor executor = Executors.newSingleThreadExecutor();
//        executor.execute(new Runnable() {
//            @Override
//            public void run() {
//                for (PooledConnection pooledConnection : this.grantedConnectionQueue)
//                    pooledConnection.setShutdownProcess(true);
//                boolean allClosed = false;
//                PooledConnection pooledConnection = null;
//                while (!allClosed) {
//                    allClosed = true;
//                    try {
//                        for (PooledConnection grantedPooledConnection : this.grantedConnectionQueue) {
//                            pooledConnection = grantedPooledConnection;
//                            if (pooledConnection.isClosed()) {
//                                this.grantedConnectionQueue.remove(pooledConnection);
//                                break;
//                            } else {
//                                allClosed = false;
//                            }
//                        }
//                    } catch (SQLException e) {
//                        logger.error("Connection shutdown error", e);
//                        ConnectionPool.grantedConnectionQueue.remove(pooledConnection);
//                    }
//                }
//            }
//        });
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
