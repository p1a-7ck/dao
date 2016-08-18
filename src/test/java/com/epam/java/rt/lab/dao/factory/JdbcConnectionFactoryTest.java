package com.epam.java.rt.lab.dao.factory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.*;

/**
 * dao
 */
public class JdbcConnectionFactoryTest {
    private static Logger logger = LoggerFactory.getLogger(JdbcConnectionFactoryTest.class);
    private static Lock counterLock = new ReentrantLock();
    private volatile int countThread = 0;
    private DaoFactory factory = null;

    @Before
    public void setUp() throws Exception {
        this.factory = DaoFactory.createDaoFactory();
        assertNotNull("Creating DAO factory returned null", this.factory);
    }

    @After
    public void tearDown() throws Exception {
        JdbcConnectionFactory.getInstance().releaseConnectionAll();
    }

    @Test
    public void getInstanceTest() throws Exception {
        assertEquals("Getting instance error",
                JdbcConnectionFactory.getInstance(), JdbcConnectionFactory.getInstance());
    }

    @Test
    public void getConnectionTest() throws Exception {
        Connection connection = JdbcConnectionFactory.getInstance().getConnection();
        assertNotNull("Getting connection returned null", connection);
    }

    @Test
    public void getConnectionWithUsernameAndPasswordTest() throws Exception {
        Connection connection = JdbcConnectionFactory.getInstance().getConnection("", "");
        assertNotNull("Getting connection returned null", connection);
    }

    @Test
    public void releaseConnectionTest() throws Exception {
        Connection connection = JdbcConnectionFactory.getInstance().getConnection();
        assertNotNull("Getting connection returned null", connection);
        assertTrue("Releasing connection returned false",
                JdbcConnectionFactory.getInstance().releaseConnection(connection));
    }

    @Test
    public void releaseConnectionAllTest() throws Exception {
        Connection connection1 = JdbcConnectionFactory.getInstance().getConnection();
        assertNotNull("Getting connection returned null", connection1);
        Connection connection2 = JdbcConnectionFactory.getInstance().getConnection();
        assertNotNull("Getting connection returned null", connection2);
        Connection connection3 = JdbcConnectionFactory.getInstance().getConnection();
        assertNotNull("Getting connection returned null", connection3);
        assertTrue("Releasing all connections returned zero",
                JdbcConnectionFactory.getInstance().releaseConnectionAll() > 0);
    }

    @Test
    public void createManyConnectionsTest() throws Exception {
        logger.info("Start many connections test");
        int randomWaitUpperBound = 50;
        Callable callable = new Callable() {
            @Override
            public Object call() throws Exception {
                int indexThread = 0;
                if (JdbcConnectionFactoryTest.counterLock.tryLock()) {
                    countThread = countThread + 1;
                    indexThread = countThread;
                    JdbcConnectionFactoryTest.counterLock.unlock();
                }
                logger.info("Calling method in thread " + indexThread);
                Connection connection = null;
                while (connection == null) {
                    connection = JdbcConnectionFactory.getInstance().getConnection();
                    if (connection == null) {
                        Thread.sleep((new Random()).nextInt(randomWaitUpperBound));
                    }
                }
                logger.info("Working in thread " + indexThread);
                Thread.sleep((new Random()).nextInt(randomWaitUpperBound));
                logger.info("Work done in thread " + indexThread);
                JdbcConnectionFactory.getInstance().releaseConnection(connection);
                return connection;
            }
        };
        logger.info("Start waiting result");
        int countThreads = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(countThreads);
        List<Future<Connection>> futureConnectionList = new ArrayList<>();
        for (int i = 0; i < countThreads; i++) futureConnectionList.add(executorService.submit(callable));
        Connection connection;
        while (!futureConnectionList.isEmpty()) {
            for (int i = 0; i < futureConnectionList.size(); i++) {
                connection = null;
                try {
                    connection = futureConnectionList.get(i).get(randomWaitUpperBound, TimeUnit.MILLISECONDS);
                    if (connection != null) {
                        futureConnectionList.remove(i);
                        logger.info("(left {} connections)", futureConnectionList.size());
                        break;
                    }
                } catch (TimeoutException exc) {

                }
            }
        }
        logger.info("Many connections test complete");
    }
}