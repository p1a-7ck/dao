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
        ConnectionPool.getInstance().reloadProperties
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
        ResultObject.randomIterationUpperBound = 10;
        ResultObject.randomSleepUpperBound = 10;

        Callable<ResultObject> task = new Callable<ResultObject>() {
            @Override
            public ResultObject call() throws Exception {
                ResultObject result = new ResultObject();
                int index = ResultObject.countThreads.getAndIncrement();
                while (!ResultObject.returnResult.get()) {
                    Connection connection = ConnectionPool.getInstance().getConnection();
                    for (int j = 0; j < ResultObject.randomIterationUpperBound; j++) {
                        Thread.sleep(ResultObject.randomSleepUpperBound);
                    }
                    connection.close();
                    result.countConnections++;
                }
                System.out.println("return " + index + " (left " + ResultObject.countThreads.decrementAndGet() + ")");
                return result;
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(countThreads);
        for (int i = 0; i < countThreads; i++) results.add(executorService.submit(task));

        long currentTime = new Date().getTime();
        while (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - currentTime) < testDurationMinutes) {
            Thread.sleep(1000);
        }

        ResultObject.returnResult.set(true);
        System.out.println("\n\n");

        ResultObject finalResult;
        List<ResultObject> finalResults = new ArrayList<>();
        while (finalResults.size() < countThreads) {
            for (int i = 0; i < results.size(); i++) {
                try {
                    finalResult = (ResultObject) results.get(i).get(10, TimeUnit.MILLISECONDS);
                    if (finalResult != null) finalResults.add(finalResult);
                    results.remove(i);
                    break;
                } catch (Exception e) {
                    finalResult = null;
//                    System.out.println(results.size());
                }
            }
        }
        int countConnectionsAll = 0;
        for (ResultObject result : finalResults) {
            countConnectionsAll = countConnectionsAll + result.countConnections;
            System.out.println("countConnections = " + result.countConnections);
        }
        assertEquals("Number of connections not equal", countConnectionsAll, ConnectionPool.getInstance().countConnections.get());
    }

    @Test
    public void shutdown() throws Exception {

    }

    private static class ResultObject {
        static AtomicInteger countThreads = new AtomicInteger(0);
        static AtomicBoolean returnResult = new AtomicBoolean(false);
        static int randomIterationUpperBound;
        static int randomSleepUpperBound;
        int countConnections = 0;
        int countInterruptException = 0;
    }
}