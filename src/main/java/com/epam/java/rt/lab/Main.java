package com.epam.java.rt.lab;

import com.epam.java.rt.lab.dao.Dao;
import com.epam.java.rt.lab.dao.factory.DaoFactory;
import com.epam.java.rt.lab.entity.authentication.User;
import com.epam.java.rt.lab.service.UserService;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * com.epam.java.rt.lab.dao
 */
public class Main {
    public static void main(String[] args) throws Exception {

        Callable callable = Executors.callable(new UserService());
        for (int i = 0; i < 1000000; i ++) callable.call();



//        DaoFactory factory = DaoFactory.createDaoFactory();
//        Dao reflectiveDao = factory.getReflectiveJdbcDao();
//
//        reflectiveDao.insert(new User());
//
//        reflectiveDao = null;
//        factory = null;

/*
        if (factory != null) {
            try {
                Connection connection = factory.createConnection();
                Statement statement = connection.createStatement();
                statement.execute("CREATE TABLE USER (ID INT PRIMARY KEY, LOGIN VARCHAR(255), PASS VARCHAR(255))");
                statement.execute("INSERT INTO USER VALUES (1, 'admin', 'admin')");
                statement.execute("INSERT INTO USER VALUES (2, 'user', 'user')");
                statement.execute("INSERT INTO USER VALUES (3, 'test', 'test')");
                statement.execute("SELECT * FROM USER");
                while (statement.getResultSet().next()) {
                    System.out.println(statement.getResultSet().getInt(1));
                    System.out.println(statement.getResultSet().getString(2));
                    System.out.println(statement.getResultSet().getString(2));
                }
                statement.close();
                connection.close();
            } catch (SQLException exc) {
                exc.printStackTrace();
            }
        }
*/

    }
}
