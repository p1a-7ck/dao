package com.epam.java.rt.lab.connection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
        ResultObject.randomIterationUpperBound = 500;
        ResultObject.randomSleepUpperBound = 500;

        Callable<ResultObject> task = new Callable<ResultObject>() {
            @Override
            public ResultObject call() throws Exception {
                Random rn = new Random();
                ResultObject result = new ResultObject();
                while (!ResultObject.returnResult.get()) {
                    Thread.sleep(rn.nextInt(ResultObject.randomSleepUpperBound));
                    try {
                        Connection connection = ConnectionPool.getInstance().getConnection();
                        for (int j = 0; j < rn.nextInt(ResultObject.randomIterationUpperBound); j++) {
                            Thread.sleep(rn.nextInt(ResultObject.randomSleepUpperBound));
                        }
                        connection.close();
                        result.countConnections++;
                    } catch (SQLException e) {
                        result.countNoConnection++;
                    }
                }
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

        currentTime = new Date().getTime();
        ResultObject finalResult;
        List<ResultObject> finalResults = new ArrayList<>();
        while (finalResults.size() < countThreads &&
                TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - currentTime) < testDurationMinutes) {
            for (int i = 0; i < results.size(); i++) {
                try {
                    finalResult = (ResultObject) results.get(i).get(10, TimeUnit.MILLISECONDS);
                    if (finalResult != null) finalResults.add(finalResult);
                    results.remove(i);
                    break;
                } catch (Exception e) {
                    finalResult = null;
                }
            }
        }
        int countConnectionsAll = 0;
        int countNoConnectionsAll = 0;
        for (ResultObject result : finalResults) {
            countConnectionsAll = countConnectionsAll + result.countConnections;
            countNoConnectionsAll = countNoConnectionsAll + result.countNoConnection;
        }
        ConnectionPool.getInstance().shutdown();
        System.out.println("Granted connections = " + countConnectionsAll + " / " + (countNoConnectionsAll + countConnectionsAll));
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
        int countNoConnection = 0;
    }
}