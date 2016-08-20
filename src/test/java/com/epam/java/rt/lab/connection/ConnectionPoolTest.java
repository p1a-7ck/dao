package com.epam.java.rt.lab.connection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * dao
 */
public class ConnectionPoolTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void getInstanceTest() throws Exception {
        assertEquals("Connection pool getting instance return different objects",
                ConnectionPool.getInstance(), ConnectionPool.getInstance());
    }

    @Test
    public void reloadPropertiesTest() throws Exception {
        ConnectionPool.reloadProperties
                (ConnectionPool.class.getClassLoader().getResourceAsStream("connection.properties"));
        assertNotNull(ConnectionPool.getInstance());
    }

    @Test
    public void getConnectionTest() throws Exception {
        Connection connection = ConnectionPool.getInstance().getConnection();
        assertNotNull(connection);
        connection.close();
    }

    @Test
    public void getConnectionMultiTest() throws Exception {
        int countThreads = 100;
        long testDurationMinutes = 1;
        List<Future<ResultObject>> results = new ArrayList<>();
        ResultObject.randomIterationUpperBound = 100;
        ResultObject.randomSleepUpperBound = 100;

        ResultObject.returnResult.set(false);
        ResultObject.countThreads.set(0);
        ExecutorService executorService = Executors.newFixedThreadPool(countThreads);
        for (int i = 0; i < countThreads; i++) {
            results.add(executorService.submit(() -> {
                ResultObject result = new ResultObject();
//                Random rn = new Random();
                ResultObject.countThreads.getAndIncrement();
                while (!ResultObject.returnResult.get()) {
//                    Thread.sleep(rn.nextInt(ResultObject.randomSleepUpperBound));
                    Thread.sleep(ResultObject.randomSleepUpperBound);
                    Connection connection = ConnectionPool.getInstance().getConnection();
                    for (int j = 0; j < ResultObject.randomIterationUpperBound; j++) {
//                        Thread.sleep(rn.nextInt(ResultObject.randomSleepUpperBound));
                        Thread.sleep(ResultObject.randomSleepUpperBound);
                    }
                    connection.close();
                    result.countConnections++;
                }
                System.out.println("return (" + ResultObject.countThreads.decrementAndGet() + ")");
                return result;
            }));
        }
        long currentTime = new Date().getTime();
        while (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - currentTime) < testDurationMinutes) {
            Thread.sleep(1000);
        }
        ResultObject.returnResult.set(true);
        Thread.sleep(1000);
        System.out.println("\n\n");
        ResultObject finalResult;
        List<ResultObject> finalResults = new ArrayList<>();
        while (finalResults.size() < countThreads) {
            Thread.sleep(1000);
            for (Future future : results) {
                try {
                    finalResult = (ResultObject) future.get(10, TimeUnit.MILLISECONDS);
                    if (finalResult != null) {
                        finalResults.add(finalResult);
                        results.remove(future);
                        break;
                    }
                } catch (Exception e) {
//                    System.out.println(results.size());
                }
            }
        }
        int countConnectionsAll = 0;
        for (ResultObject result : finalResults) {
            countConnectionsAll = countConnectionsAll + result.countConnections;
            System.out.println("countConnections = " + result.countConnections);
        }
        assertEquals("Number of connections not equal", countConnectionsAll, ConnectionPool.countConnections);
    }

    @Test
    public void shutdown() throws Exception {

    }

    private static class ResultObject {
        static AtomicInteger countThreads = new AtomicInteger();
        static AtomicBoolean returnResult = new AtomicBoolean();
        static int randomIterationUpperBound;
        static int randomSleepUpperBound;
        int countConnections = 0;
        int countInterruptException = 0;
    }
}