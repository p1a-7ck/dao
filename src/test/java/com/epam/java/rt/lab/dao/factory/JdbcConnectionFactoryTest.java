package com.epam.java.rt.lab.dao.factory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Time;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.*;

/**
 * dao
 */
public class JdbcConnectionFactoryTest {
    private static Logger logger = LoggerFactory.getLogger(JdbcConnectionFactoryTest.class);
    private static Lock threadListLock = new ReentrantLock();
    private static List<Thread> threadList = new ArrayList<>();
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
        int randomWaitUpperBound = 25;
        Callable callable = new Callable() {
            @Override
            public Object call() throws Exception {
                int indexThread = -1;
                Connection connection = null;
                try {
                    logger.info("Trying to add thread to thread list");
                    while (indexThread == -1) {
                        if (JdbcConnectionFactoryTest.threadListLock.tryLock(10, TimeUnit.MILLISECONDS)) {
                            JdbcConnectionFactoryTest.threadList.add(Thread.currentThread());
                            indexThread = JdbcConnectionFactoryTest.threadList.size();
                            JdbcConnectionFactoryTest.threadListLock.unlock();
                        }
                    }
                    logger.info("Trying to get connection in thread " + indexThread);
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
                } catch (InterruptedException exc) {
                    logger.info("Thread {} got InteruptExeception", indexThread);
                    return connection;
                } finally {
                    JdbcConnectionFactoryTest.threadList.remove(Thread.currentThread());
                    if (connection != null) connection.close();
                }
            }
        };
        logger.info("Start waiting result");
        int countThreads = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(countThreads);
        List<Future<Connection>> futureConnectionList = new ArrayList<>();
        for (int i = 0; i < countThreads; i++) futureConnectionList.add(executorService.submit(callable));
        long breakTime = System.currentTimeMillis() + randomWaitUpperBound * 100;
        Connection connection;
        while (!futureConnectionList.isEmpty() && System.currentTimeMillis() < breakTime) {
            for (int i = 0; i < futureConnectionList.size(); i++) {
                connection = null;
                try {
                    connection = futureConnectionList.get(i).get(1, TimeUnit.MILLISECONDS);
                    if (connection != null) {
                        futureConnectionList.remove(i);
                        logger.info("(left {} connections)", futureConnectionList.size());
                        break;
                    }
                } catch (TimeoutException exc) {
                    //
                }
            }
        }
        if (!futureConnectionList.isEmpty()) {
            logger.info("Interupt alive threads");
            Thread thread;
            for (int i = 0; i < JdbcConnectionFactoryTest.threadList.size(); i++) {
                thread = JdbcConnectionFactoryTest.threadList.get(i);
                if (thread.isAlive()) {
                    logger.info("Interupt thread with index {}", i);
                    thread.interrupt();
                }
            }
            logger.info("Wait while threads interupting");
            boolean isSomeNotTerminated = true;
            while (isSomeNotTerminated) {
                isSomeNotTerminated = false;
                for (int i = 0; i < JdbcConnectionFactoryTest.threadList.size(); i++)
                    if (JdbcConnectionFactoryTest.threadList.get(i).getState() == Thread.State.TERMINATED)
                        isSomeNotTerminated = true;
            }
        }
        logger.info("Many connections test complete");
    }
}